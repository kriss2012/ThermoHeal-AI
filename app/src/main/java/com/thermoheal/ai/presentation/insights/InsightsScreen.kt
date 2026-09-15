package com.thermoheal.ai.presentation.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.domain.model.AIInsight
import com.thermoheal.ai.domain.model.InsightCategory
import com.thermoheal.ai.domain.repository.AIRepository
import com.thermoheal.ai.ui.components.AIInsightCard
import com.thermoheal.ai.ui.components.EmptyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    aiRepository: AIRepository
) : ViewModel() {
    val insights: StateFlow<List<AIInsight>> = aiRepository.observeInsights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

private val categories: List<InsightCategory?> = listOf(null) + InsightCategory.entries

@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val insights by viewModel.insights.collectAsState()
    var selectedCategory by remember { mutableStateOf<InsightCategory?>(null) }

    val filtered = if (selectedCategory == null) insights else insights.filter { it.category == selectedCategory }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("AI Insights", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Prototype AI / Rule-based demonstration — wellness insights only.",
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(14.dp))

        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "All") }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        if (filtered.isEmpty()) {
            EmptyState(
                title = "No insights yet",
                subtitle = "Continue monitoring to generate personalized wellness insights."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 90.dp)) {
                items(filtered) { insight -> AIInsightCard(insight) }
            }
        }
    }
}
