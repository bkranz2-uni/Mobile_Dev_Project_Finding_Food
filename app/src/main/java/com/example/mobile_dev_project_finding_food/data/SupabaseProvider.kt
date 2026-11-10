package com.example.mobile_dev_project_finding_food.data

import com.example.mobile_dev_project_finding_food.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

/**
 * The one and only Supabase client in the app.
 *
 * It is created lazily the first time it is used. The URL and anon key come from
 * local.properties via BuildConfig, so they are never committed to Git.
 */
object SupabaseProvider {

    val client: SupabaseClient by lazy {
        require(BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
            "Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties"
        }

        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            // Ignore columns we have not modelled (e.g. embedded join results)
            defaultSerializer = KotlinXSerializer(Json { ignoreUnknownKeys = true })

            install(Auth)       // sign up / sign in, session is stored on the device
            install(Postgrest)  // database access through the REST API (protected by RLS)
        }
    }
}
