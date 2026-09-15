package com.thermoheal.ai.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thermoheal.ai.domain.model.ActivityLevel
import com.thermoheal.ai.domain.model.DominantFoot
import com.thermoheal.ai.ui.components.ThermoHealTopBar

import com.thermoheal.ai.ui.responsive.readableContentWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var footSize by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var dominantFoot by remember { mutableStateOf(DominantFoot.RIGHT) }
    var activityLevel by remember { mutableStateOf(ActivityLevel.MODERATE) }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.success) { if (uiState.success) onSignupSuccess() }

    Scaffold(topBar = { ThermoHealTopBar(title = "Create Account", onBack = onBack) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .readableContentWidth(maxDp = 680.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
            Text("Tell us about your feet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "We only collect what's needed to personalize your baseline. See our Privacy Notice for details.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))

            LabeledField("Full Name *", name) { name = it }
            LabeledField("Email *", email, keyboardType = KeyboardType.Email) { email = it }
            LabeledField("Password *", password, isPassword = true) { password = it }
            LabeledField("Confirm Password *", confirmPassword, isPassword = true) { confirmPassword = it }

            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledField("Age", age, keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f)) { age = it }
                LabeledField("Height (cm)", height, keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f)) { height = it }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledField("Weight (kg)", weight, keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f)) { weight = it }
                LabeledField("Foot Size (EU)", footSize, keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f)) { footSize = it }
            }

            Spacer(Modifier.height(8.dp))
            Text("Dominant Foot", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            SingleChoiceRow(
                options = DominantFoot.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                selectedIndex = DominantFoot.entries.indexOf(dominantFoot)
            ) { dominantFoot = DominantFoot.entries[it] }

            Spacer(Modifier.height(12.dp))
            Text("Activity Level", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            SingleChoiceRow(
                options = ActivityLevel.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                selectedIndex = ActivityLevel.entries.indexOf(activityLevel)
            ) { activityLevel = ActivityLevel.entries[it] }

            Spacer(Modifier.height(12.dp))
            LabeledField("Occupation (optional)", occupation) { occupation = it }

            if (uiState.errorMessage != null) {
                Spacer(Modifier.height(10.dp))
                Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.signUp(
                        name = name, email = email, password = password, confirmPassword = confirmPassword,
                        ageYears = age.toIntOrNull(), heightCm = height.toDoubleOrNull(), weightKg = weight.toDoubleOrNull(),
                        footSizeEu = footSize.toDoubleOrNull(), dominantFoot = dominantFoot, activityLevel = activityLevel,
                        occupation = occupation.ifBlank { null }
                    )
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Create Account")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LabeledField(
    label: String, value: String, isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)
    )
}

@Composable
private fun SingleChoiceRow(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                label = { Text(label, style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}
