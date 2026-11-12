package com.example.mobile_dev_project_finding_food.data.repository

import com.example.mobile_dev_project_finding_food.data.AppError
import com.example.mobile_dev_project_finding_food.data.AppResult
import com.example.mobile_dev_project_finding_food.data.SupabaseProvider
import com.example.mobile_dev_project_finding_food.data.model.ProductStore
import com.example.mobile_dev_project_finding_food.data.model.ProductStoreInsert
import com.example.mobile_dev_project_finding_food.data.safeCall
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

/** Links between products and stores ("I saw this product in this store"). */
class ProductStoreRepository(
    private val supabase: SupabaseClient = SupabaseProvider.client,
) {
    /** Requires sign-in. added_by is set to the current user by the database. */
    suspend fun addProductToStore(storeId: Long, reportNumber: String): AppResult<ProductStore> {
        if (supabase.auth.currentUserOrNull() == null) {
            return AppResult.Failure(AppError.NotSignedIn)
        }

        return safeCall {
            supabase.from(TABLE)
                .insert(ProductStoreInsert(storeId, reportNumber)) { select() }
                .decodeSingle<ProductStore>()
        }
    }

    /**
     * Removes a link. RLS only allows deleting links the current user added.
     *
     * RLS does not raise an error on a blocked delete, it just deletes 0 rows.
     * So when nothing was deleted, we check whether the link exists to give a clear error.
     */
    suspend fun removeProductFromStore(storeId: Long, reportNumber: String): AppResult<Unit> {
        if (supabase.auth.currentUserOrNull() == null) {
            return AppResult.Failure(AppError.NotSignedIn)
        }

        val deleted = safeCall {
            supabase.from(TABLE)
                .delete {
                    select() // return the deleted rows
                    filter {
                        eq("store_id", storeId)
                        eq("report_number", reportNumber)
                    }
                }
                .decodeList<ProductStore>()
        }

        return when (deleted) {
            is AppResult.Failure -> deleted
            is AppResult.Success ->
                if (deleted.data.isNotEmpty()) {
                    AppResult.Success(Unit)
                } else {
                    // Nothing deleted: either the link does not exist, or someone else added it
                    when (val exists = safeCall { linkExists(storeId, reportNumber) }) {
                        is AppResult.Failure -> exists
                        is AppResult.Success -> AppResult.Failure(
                            if (exists.data) AppError.PermissionDenied else AppError.NotFound("Link")
                        )
                    }
                }
        }
    }

    private suspend fun linkExists(storeId: Long, reportNumber: String): Boolean =
        supabase.from(TABLE)
            .select {
                filter {
                    eq("store_id", storeId)
                    eq("report_number", reportNumber)
                }
            }
            .decodeList<ProductStore>()
            .isNotEmpty()

    private companion object {
        const val TABLE = "product_stores"
    }
}
