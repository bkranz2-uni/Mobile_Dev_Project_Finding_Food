package com.example.mobile_dev_project_finding_food.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_dev_project_finding_food.data.AppResult
import com.example.mobile_dev_project_finding_food.data.model.Product
import com.example.mobile_dev_project_finding_food.data.repository.AuthRepository
import com.example.mobile_dev_project_finding_food.data.repository.ProductRepository
import com.example.mobile_dev_project_finding_food.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DebugUiState(
    val loading: Boolean = false,
    val status: String = "Not started",
    val products: List<Product> = emptyList(),
)

/**
 * Proves the Supabase connection end to end:
 * sign in -> find store "test-1" -> load its vegan products.
 */
class SupabaseDebugViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val storeRepository: StoreRepository = StoreRepository(),
    private val productRepository: ProductRepository = ProductRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(DebugUiState())
    val state: StateFlow<DebugUiState> = _state.asStateFlow()

    val signedInEmail: StateFlow<String?> = authRepository.currentUser
        .map { it?.email }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun runTest(email: String, password: String) = viewModelScope.launch {
        _state.update { it.copy(loading = true, status = "Signing in…", products = emptyList()) }

        if (authRepository.currentUserOrNull() == null) {
            val signIn = authRepository.signIn(email.trim(), password)
            if (signIn is AppResult.Failure) return@launch fail("Sign in failed: ${signIn.error.message}")
        }

        _state.update { it.copy(status = "Finding store test-1…") }
        val store = when (val result = storeRepository.getStoreByKakaoPlaceId("test-1")) {
            is AppResult.Success -> result.data
            is AppResult.Failure -> return@launch fail("Store lookup failed: ${result.error.message}")
        }

        _state.update { it.copy(status = "Loading vegan products for ${store.name}…") }
        when (val result = productRepository.getProductsForStore(store.id, vegan = true)) {
            is AppResult.Success -> _state.update {
                it.copy(
                    loading = false,
                    status = "OK: ${result.data.size} vegan product(s) in ${store.name}",
                    products = result.data,
                )
            }
            is AppResult.Failure -> fail("Loading products failed: ${result.error.message}")
        }
    }

    fun signOut() = viewModelScope.launch {
        authRepository.signOut()
        _state.value = DebugUiState(status = "Signed out")
    }

    private fun fail(message: String) {
        _state.update { it.copy(loading = false, status = message) }
    }
}
