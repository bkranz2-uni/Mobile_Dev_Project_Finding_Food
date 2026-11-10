package com.example.mobile_dev_project_finding_food.data.repository

import com.example.mobile_dev_project_finding_food.data.AppError
import com.example.mobile_dev_project_finding_food.data.AppResult
import com.example.mobile_dev_project_finding_food.data.SupabaseProvider
import com.example.mobile_dev_project_finding_food.data.model.Profile
import com.example.mobile_dev_project_finding_food.data.model.ProfileUpdate
import com.example.mobile_dev_project_finding_food.data.orNotFound
import com.example.mobile_dev_project_finding_food.data.safeCall
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

class ProfileRepository(
    private val supabase: SupabaseClient = SupabaseProvider.client,
) {
    /** The profile of the signed-in user (created automatically by a database trigger). */
    suspend fun getMyProfile(): AppResult<Profile> {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: return AppResult.Failure(AppError.NotSignedIn)

        return safeCall {
            supabase.from(TABLE)
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<Profile>()
        }.orNotFound("Profile")
    }

    suspend fun updateMyProfile(
        displayName: String?,
        vegan: Boolean,
        vegetarian: Boolean,
        glutenFree: Boolean,
    ): AppResult<Profile> {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: return AppResult.Failure(AppError.NotSignedIn)

        return safeCall {
            supabase.from(TABLE)
                .update(ProfileUpdate(displayName, vegan, vegetarian, glutenFree)) {
                    select() // return the updated row
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<Profile>()
        }.orNotFound("Profile")
    }

    private companion object {
        const val TABLE = "profiles"
    }
}
