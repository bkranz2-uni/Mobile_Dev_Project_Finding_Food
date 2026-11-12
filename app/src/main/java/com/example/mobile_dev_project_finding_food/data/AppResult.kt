package com.example.mobile_dev_project_finding_food.data

import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Result of every repository call. The UI either gets data, or a clear [AppError].
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

/** Turns a successful-but-empty (null) result into [AppError.NotFound]. */
fun <T : Any> AppResult<T?>.orNotFound(what: String): AppResult<T> = when (this) {
    is AppResult.Success -> data?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.NotFound(what))
    is AppResult.Failure -> this
}

/** All errors the UI needs to handle, independent of Supabase internals. */
sealed class AppError(val message: String) {
    data object NotSignedIn : AppError("You need to sign in first")
    data class NotFound(val what: String) : AppError("$what not found")
    data object PermissionDenied : AppError("You are not allowed to do this")
    data object AlreadyExists : AppError("This already exists")
    data object InvalidCredentials : AppError("Wrong email or password")
    data object EmailNotConfirmed : AppError("Please confirm your email first")
    data object Network : AppError("No connection to the server")
    data class Unknown(val details: String?) : AppError(details ?: "Something went wrong")
}

/**
 * Runs a Supabase call and turns exceptions into [AppError]s,
 * so repositories never throw to the UI.
 */
suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e // never swallow coroutine cancellation
    } catch (e: PostgrestRestException) {
        AppResult.Failure(mapPostgresError(e))
    } catch (e: AuthRestException) {
        AppResult.Failure(mapAuthError(e))
    } catch (e: HttpRequestException) {
        AppResult.Failure(AppError.Network)
    } catch (e: Exception) {
        AppResult.Failure(AppError.Unknown(e.message))
    }

// Postgres error codes: https://www.postgresql.org/docs/current/errcodes-appendix.html
private fun mapPostgresError(e: PostgrestRestException): AppError = when (e.code) {
    "42501" -> AppError.PermissionDenied      // insufficient_privilege (RLS policy violated)
    "23505" -> AppError.AlreadyExists         // unique_violation
    "23503" -> AppError.NotFound("Store or product") // foreign_key_violation
    "PGRST116" -> AppError.NotFound("Row")    // .single() returned no rows
    else -> AppError.Unknown(e.message)
}

// Supabase Auth error codes: https://supabase.com/docs/guides/auth/debugging/error-codes
private fun mapAuthError(e: AuthRestException): AppError = when (e.error) {
    "invalid_credentials" -> AppError.InvalidCredentials
    "email_not_confirmed" -> AppError.EmailNotConfirmed
    "user_already_exists", "email_exists" -> AppError.AlreadyExists
    else -> AppError.Unknown(e.message)
}
