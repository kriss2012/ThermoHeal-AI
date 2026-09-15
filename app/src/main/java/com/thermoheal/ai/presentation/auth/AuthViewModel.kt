package com.thermoheal.ai.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.domain.model.*
import com.thermoheal.ai.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Please enter both email and password.")
            return
        }
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            val result = userRepository.login(email.trim(), password)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState(success = true) },
                onFailure = { AuthUiState(errorMessage = it.message ?: "Login failed.") }
            )
        }
    }

    fun signUp(
        name: String, email: String, password: String, confirmPassword: String,
        ageYears: Int?, heightCm: Double?, weightKg: Double?, footSizeEu: Double?,
        dominantFoot: DominantFoot, activityLevel: ActivityLevel, occupation: String?
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "Please fill in all required fields.")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState(errorMessage = "Passwords do not match.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState(errorMessage = "Password must be at least 6 characters.")
            return
        }
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            val profile = UserProfile(
                id = "", name = name.trim(), email = email.trim(), ageYears = ageYears,
                heightCm = heightCm, weightKg = weightKg, footSizeEu = footSizeEu,
                dominantFoot = dominantFoot, activityLevel = activityLevel, occupation = occupation,
                createdAt = System.currentTimeMillis()
            )
            val result = userRepository.signUp(profile, password)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState(success = true) },
                onFailure = { AuthUiState(errorMessage = it.message ?: "Sign up failed.") }
            )
        }
    }

    fun continueAsDemo() {
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            val result = userRepository.continueAsDemoUser()
            _uiState.value = result.fold(
                onSuccess = { AuthUiState(success = true) },
                onFailure = { AuthUiState(errorMessage = it.message ?: "Could not start demo mode.") }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
