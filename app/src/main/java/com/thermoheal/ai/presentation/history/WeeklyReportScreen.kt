package com.thermoheal.ai.presentation.history

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thermoheal.ai.domain.model.UserProfile
import com.thermoheal.ai.domain.model.WeeklySummary
import com.thermoheal.ai.domain.repository.SummaryRepository
import com.thermoheal.ai.domain.repository.UserRepository
import com.thermoheal.ai.ui.components.EmptyState
import com.thermoheal.ai.ui.components.SectionHeader
import com.thermoheal.ai.ui.components.ThermoCard
import com.thermoheal.ai.ui.components.ThermoHealTopBar
import com.thermoheal.ai.utils.ReportGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WeeklyReportViewModel @Inject constructor(
    summaryRepository: SummaryRepository,
    userRepository: UserRepository
) : ViewModel() {
    val summaries: StateFlow<List<WeeklySummary>> = summaryRepository.observeWeeklySummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val user: StateFlow<UserProfile?> = userRepository.observeUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

@Composable
fun WeeklyReportScreen(onBack: () -> Unit, viewModel: WeeklyReportViewModel = hiltViewModel()) {
    val summaries by viewModel.summaries.collectAsState()
    val user by viewModel.user.collectAsState()
    val context = LocalContext.current

    Scaffold(topBar = { ThermoHealTopBar("Weekly Trends", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {

            if (summaries.isEmpty()) {
                ThermoCard(modifier = Modifier.fillMaxWidth()) {
                    EmptyState(
                        title = "No weekly data yet",
                        subtitle = "Weekly trends will appear here once you've completed at least one monitoring week."
                    )
                }
            } else {
                SectionHeader("This Week")
                summaries.forEach { s ->
                    ThermoCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Text("Week of ${s.weekStartDate}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text("Average Wellness Index: ${s.avgWellnessIndex}", style = MaterialTheme.typography.bodyMedium)
                        Text("Best day: ${s.bestDay ?: "—"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Most active day: ${s.mostActiveDay ?: "—"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Most stable day: ${s.mostStableDay ?: "—"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        s.notableTrend?.let {
                            Spacer(Modifier.height(6.dp))
                            Text("Notable trend: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { generateAndOpenReport(context, user, summaries) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Download Report") }

            Spacer(Modifier.height(90.dp))
        }
    }
}

private fun generateAndOpenReport(context: Context, user: UserProfile?, summaries: List<WeeklySummary>) {
    try {
        val file = ReportGenerator.generateWeeklyReport(context, user, summaries, isDemoData = user?.isDemoUser ?: true)
        Toast.makeText(context, "Report saved: ${file.name}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Could not generate report: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
