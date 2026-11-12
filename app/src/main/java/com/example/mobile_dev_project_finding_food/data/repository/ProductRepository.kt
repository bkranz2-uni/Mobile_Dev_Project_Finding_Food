package com.example.mobile_dev_project_finding_food.data.repository

import com.example.mobile_dev_project_finding_food.data.AppResult
import com.example.mobile_dev_project_finding_food.data.SupabaseProvider
import com.example.mobile_dev_project_finding_food.data.model.Product
import com.example.mobile_dev_project_finding_food.data.orNotFound
import com.example.mobile_dev_project_finding_food.data.safeCall
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class ProductRepository(
    private val supabase: SupabaseClient = SupabaseProvider.client,
) {
    /**
     * Products available in a store, filtered by the diet chips.
     *
     * Same as this SQL:
     *   select p.* from products p
     *   join product_stores ps on ps.report_number = p.report_number
     *   where ps.store_id = :storeId and (:vegan = false or p.is_vegan) ...
     *
     * "product_stores!inner(store_id)" makes PostgREST do an inner join, so only
     * products that are linked to the store are returned.
     */
    suspend fun getProductsForStore(
        storeId: Long,
        vegan: Boolean = false,
        vegetarian: Boolean = false,
        glutenFree: Boolean = false,
    ): AppResult<List<Product>> = safeCall {
        supabase.from(TABLE)
            .select(Columns.raw("*, product_stores!inner(store_id)")) {
                filter {
                    eq("product_stores.store_id", storeId)
                    if (vegan) eq("is_vegan", true)
                    if (vegetarian) eq("is_vegetarian", true)
                    if (glutenFree) eq("is_gluten_free", true)
                }
                order("product_name_kr", Order.ASCENDING)
            }
            .decodeList<Product>()
    }

    /** Case-insensitive search in both the Korean and the English product name. */
    suspend fun searchProducts(query: String): AppResult<List<Product>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return AppResult.Success(emptyList())

        return safeCall {
            supabase.from(TABLE)
                .select {
                    filter {
                        or {
                            ilike("product_name_kr", "%$trimmed%")
                            ilike("product_name_en", "%$trimmed%")
                        }
                    }
                    limit(MAX_SEARCH_RESULTS)
                }
                .decodeList<Product>()
        }
    }

    suspend fun getProduct(reportNumber: String): AppResult<Product> = safeCall {
        supabase.from(TABLE)
            .select { filter { eq("report_number", reportNumber) } }
            .decodeSingleOrNull<Product>()
    }.orNotFound("Product")

    private companion object {
        const val TABLE = "products"
        const val MAX_SEARCH_RESULTS = 50L
    }
}
