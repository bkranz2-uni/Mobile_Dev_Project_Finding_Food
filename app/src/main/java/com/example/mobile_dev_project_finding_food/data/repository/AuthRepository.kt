package com.example.mobile_dev_project_finding_food.data.repository

import com.example.mobile_dev_project_finding_food.data.AppResult
import com.example.mobile_dev_project_finding_food.data.SupabaseProvider
import com.example.mobile_dev_project_finding_food.data.safeCall
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class SignUpOutcome {
    /** The user is signed in right away. */
    SignedIn,

    /** Email confirmation is ON in Supabase: the user must click the link in the email first. */
    ConfirmationRequired,
}

class AuthRepository(
    private val supabase: SupabaseClient = SupabaseProvider.client,
) {
    private val auth get() = supabase.auth

    /** Emits the signed-in user, or null when signed out. Updates automatically. */
    val currentUser: Flow<UserInfo?> = auth.sessionStatus.map { status ->
        (status as? SessionStatus.Authenticated)?.session?.user
    }

    /** Raw session state (Initializing / Authenticated / NotAuthenticated / RefreshFailure). */
    val sessionStatus get() = auth.sessionStatus

    fun currentUserOrNull(): UserInfo? = auth.currentUserOrNull()

    suspend fun signUp(email: String, password: String): AppResult<SignUpOutcome> = safeCall {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        if (auth.currentSessionOrNull() != null) SignUpOutcome.SignedIn
        else SignUpOutcome.ConfirmationRequired
    }

    suspend fun signIn(email: String, password: String): AppResult<Unit> = safeCall {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut(): AppResult<Unit> = safeCall {
        auth.signOut()
    }
}
