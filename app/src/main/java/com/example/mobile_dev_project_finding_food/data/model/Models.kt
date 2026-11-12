package com.example.mobile_dev_project_finding_food.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Data classes that match the Supabase tables.
// Database columns are snake_case, Kotlin properties are camelCase.

@Serializable
data class Product(
    @SerialName("report_number") val reportNumber: String,
    @SerialName("product_name_kr") val productNameKr: String,
    @SerialName("product_name_en") val productNameEn: String? = null,
    @SerialName("product_kind") val productKind: String? = null,
    @SerialName("raw_materials") val rawMaterials: String? = null,
    /** HACCP allergy text in Korean. The value "없음" means "none". */
    @SerialName("allergy_kr") val allergyKr: String? = null,
    @SerialName("allergy_en") val allergyEn: String? = null,
    val manufacturer: String? = null,
    /** HACCP image URL, usually http:// (allowed in network_security_config.xml). */
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("is_vegan") val isVegan: Boolean = false,
    @SerialName("is_vegetarian") val isVegetarian: Boolean = false,
    @SerialName("is_gluten_free") val isGlutenFree: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    /** Name to show in the UI: English if we have it, otherwise Korean. */
    val displayName: String
        get() = productNameEn?.takeIf { it.isNotBlank() } ?: productNameKr

    /** Allergy text to show in the UI: English if we have it, otherwise Korean. */
    val displayAllergy: String?
        get() = allergyEn?.takeIf { it.isNotBlank() } ?: allergyKr
}

@Serializable
data class Profile(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    val vegan: Boolean = false,
    val vegetarian: Boolean = false,
    @SerialName("gluten_free") val glutenFree: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

/** The fields a user is allowed to change on their own profile. */
@Serializable
data class ProfileUpdate(
    @SerialName("display_name") val displayName: String?,
    val vegan: Boolean,
    val vegetarian: Boolean,
    @SerialName("gluten_free") val glutenFree: Boolean,
)

@Serializable
data class Store(
    val id: Long,
    @SerialName("kakao_place_id") val kakaoPlaceId: String,
    val name: String,
    /** One of "CU", "GS25", "7-Eleven", "emart24", "other". */
    val chain: String = "other",
    val address: String? = null,
    val latitude: Double,
    val longitude: Double,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class ProductStore(
    @SerialName("store_id") val storeId: Long,
    @SerialName("report_number") val reportNumber: String,
    /** null = added by the team. */
    @SerialName("added_by") val addedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

/**
 * What the app sends when linking a product to a store.
 * added_by is NOT sent: the database fills it with auth.uid().
 */
@Serializable
data class ProductStoreInsert(
    @SerialName("store_id") val storeId: Long,
    @SerialName("report_number") val reportNumber: String,
)
