package com.thermoheal.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.thermoheal.ai.navigation.RootViewModel
import com.thermoheal.ai.navigation.ThermoHealNavGraph
import com.thermoheal.ai.ui.theme.ThermoHealTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val rootViewModel: RootViewModel = hiltViewModel()
            val themeMode by rootViewModel.themeMode.collectAsState()

            ThermoHealTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ThermoHealNavGraph(rootViewModel = rootViewModel)
                }
            }
        }
    }
}
