package com.thermoheal.ai.presentation.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.domain.model.AIInsight
import com.thermoheal.ai.domain.model.InsightCategory
import com.thermoheal.ai.domain.repository.AIRepository
import com.thermoheal.ai.ui.components.AIInsightCard
import com.thermoheal.ai.ui.components.EmptyState
import com.thermoheal.ai.ui.responsive.LocalWindowSizeInfo
import com.thermoheal.ai.ui.responsive.WindowWidthClass
import com.thermoheal.ai.ui.responsive.readableContentWidth
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
    val insights by viewModel.insights.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf<InsightCategory?>(null) }

    val windowSize = LocalWindowSizeInfo.current
    val isTablet = windowSize.widthClass != WindowWidthClass.COMPACT

    val filtered = if (selectedCategory == null) insights else insights.filter { it.category == selectedCategory }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .readableContentWidth(maxDp = 1100.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(windowSize.contentPadding)
        ) {
            Text("AI Clinical Intelligence", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Deterministic heuristic engine (v1.0.0) — decision support only, not a medical diagnosis.",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(14.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            } else if (isTablet) {
                // Adaptive 2-column grid for tablets & foldables (Section 38 & 112)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered) { insight -> AIInsightCard(insight) }
                }
            } else {
                // Single column for phones
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered) { insight -> AIInsightCard(insight) }
                }
            }
        }
    }
}
