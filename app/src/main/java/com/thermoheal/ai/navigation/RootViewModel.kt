package com.thermoheal.ai.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.domain.repository.UserRepository
import com.thermoheal.ai.ui.theme.AppThemeMode
import com.thermoheal.ai.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val userRepository: UserRepository
) : ViewModel() {

    val themeMode: StateFlow<AppThemeMode> = prefs.themeMode
        .map { runCatching { AppThemeMode.valueOf(it) }.getOrDefault(AppThemeMode.SYSTEM) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.SYSTEM)

    val onboardingComplete: StateFlow<Boolean> = prefs.onboardingComplete
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun markOnboardingComplete() = viewModelScope.launch { prefs.setOnboardingComplete(true) }

    suspend fun isLoggedIn(): Boolean = userRepository.isLoggedIn()
}
