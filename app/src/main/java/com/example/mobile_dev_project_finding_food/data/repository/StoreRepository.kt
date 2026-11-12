package com.example.mobile_dev_project_finding_food.data.repository

import com.example.mobile_dev_project_finding_food.data.AppResult
import com.example.mobile_dev_project_finding_food.data.SupabaseProvider
import com.example.mobile_dev_project_finding_food.data.model.Store
import com.example.mobile_dev_project_finding_food.data.orNotFound
import com.example.mobile_dev_project_finding_food.data.safeCall
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class StoreRepository(
    private val supabase: SupabaseClient = SupabaseProvider.client,
) {
    /** All stores inside the visible map area (a lat/lng bounding box). */
    suspend fun getStoresInArea(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
    ): AppResult<List<Store>> = safeCall {
        supabase.from(TABLE)
            .select {
                filter {
                    gte("latitude", minLat)
                    lte("latitude", maxLat)
                    gte("longitude", minLng)
                    lte("longitude", maxLng)
                }
                limit(MAX_STORES)
            }
            .decodeList<Store>()
    }

    suspend fun getStore(id: Long): AppResult<Store> = safeCall {
        supabase.from(TABLE)
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<Store>()
    }.orNotFound("Store")

    suspend fun getStoreByKakaoPlaceId(kakaoPlaceId: String): AppResult<Store> = safeCall {
        supabase.from(TABLE)
            .select { filter { eq("kakao_place_id", kakaoPlaceId) } }
            .decodeSingleOrNull<Store>()
    }.orNotFound("Store")

    /** Stores where this product has been seen (product detail screen). */
    suspend fun getStoresForProduct(reportNumber: String): AppResult<List<Store>> = safeCall {
        supabase.from(TABLE)
            .select(Columns.raw("*, product_stores!inner(report_number)")) {
                filter { eq("product_stores.report_number", reportNumber) }
                order("name", Order.ASCENDING)
                limit(MAX_STORES)
            }
            .decodeList<Store>()
    }

    private companion object {
        const val TABLE = "stores"
        const val MAX_STORES = 200L
    }
}
