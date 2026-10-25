package com.example.charfreq.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.charfreq.model.*
import com.example.charfreq.ui.CharFreqViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CharFreqViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showResetConfirmDialog = true }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // APPEARANCE
            item {
                SectionHeader("APPEARANCE")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("System", "Light", "Dark").forEach { themeName ->
                                FilterChip(
                                    selected = uiState.theme == themeName,
                                    onClick = {
                                        val s = createSettingsFromUiState(uiState).copy(theme = themeName)
                                        viewModel.updateSettings(s)
                                    },
                                    label = { Text(themeName) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        SwitchSettingRow(
                            title = "Dynamic Color",
                            subtitle = "Use Android Material You system palette when available",
                            checked = uiState.dynamicColor,
                            onCheckedChange = {
                                val s = createSettingsFromUiState(uiState).copy(dynamicColor = it)
                                viewModel.updateSettings(s)
                            }
                        )
                    }
                }
            }

            // ANALYSIS CONFIGURATION
            item {
                SectionHeader("ANALYSIS SETTINGS")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Counting unit
                        Text("Counting Unit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        CountingUnit.values().forEach { unit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setCountingUnit(unit) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.countingUnit == unit,
                                    onClick = { viewModel.setCountingUnit(unit) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(unit.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(unit.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        SwitchSettingRow(
                            title = "Case Sensitivity",
                            subtitle = if (uiState.caseSensitive) "Respect case (A and a are distinct)" else "Ignore case (A and a are combined)",
                            checked = uiState.caseSensitive,
                            onCheckedChange = { viewModel.setCaseSensitive(it) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Normalization
                        Text("Unicode Normalization", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            NormalizationMode.values().forEach { mode ->
                                FilterChip(
                                    selected = uiState.normalization == mode,
                                    onClick = { viewModel.setNormalization(mode) },
                                    label = { Text(mode.name) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Whitespace
                        Text("Whitespace Handling", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WhitespaceMode.values().forEach { mode ->
                                FilterChip(
                                    selected = uiState.whitespaceMode == mode,
                                    onClick = { viewModel.setWhitespaceMode(mode) },
                                    label = { Text(mode.label) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        SwitchSettingRow(
                            title = "Include Punctuation",
                            subtitle = "Periods, commas, exclamation marks, quotes, brackets",
                            checked = uiState.includePunctuation,
                            onCheckedChange = { viewModel.setIncludePunctuation(it) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SwitchSettingRow(
                            title = "Include Digits",
                            subtitle = "Decimal digits and Unicode numbers",
                            checked = uiState.includeDigits,
                            onCheckedChange = { viewModel.setIncludeDigits(it) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SwitchSettingRow(
                            title = "Include Symbols",
                            subtitle = "Currency, math, and miscellaneous symbols",
                            checked = uiState.includeSymbols,
                            onCheckedChange = { viewModel.setIncludeSymbols(it) }
                        )
                    }
                }
            }

            // RESULTS DISPLAY SETTINGS
            item {
                SectionHeader("RESULTS & VISUALS")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        SwitchSettingRow(
                            title = "Frequency Chart",
                            subtitle = "Show horizontal visual bar chart for top characters",
                            checked = uiState.showFrequencyChart,
                            onCheckedChange = {
                                val s = createSettingsFromUiState(uiState).copy(showFrequencyChart = it)
                                viewModel.updateSettings(s)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SwitchSettingRow(
                            title = "Category Summary",
                            subtitle = "Show breakdown by letters, digits, whitespace, etc.",
                            checked = uiState.showCategorySummary,
                            onCheckedChange = {
                                val s = createSettingsFromUiState(uiState).copy(showCategorySummary = it)
                                viewModel.updateSettings(s)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SwitchSettingRow(
                            title = "Frequency Distribution",
                            subtitle = "Show histogram breakdown (1 occurrence, 2 occurrences, etc.)",
                            checked = uiState.showFrequencyDistribution,
                            onCheckedChange = {
                                val s = createSettingsFromUiState(uiState).copy(showFrequencyDistribution = it)
                                viewModel.updateSettings(s)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SwitchSettingRow(
                            title = "Unicode Details Card",
                            subtitle = "Show ASCII metrics and UTF-16 vs code point comparisons",
                            checked = uiState.showUnicodeDetails,
                            onCheckedChange = {
                                val s = createSettingsFromUiState(uiState).copy(showUnicodeDetails = it)
                                viewModel.updateSettings(s)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SwitchSettingRow(
                            title = "Processing Preview",
                            subtitle = "Show token segmentation preview chips",
                            checked = uiState.showProcessingPreview,
                            onCheckedChange = {
                                val s = createSettingsFromUiState(uiState).copy(showProcessingPreview = it)
                                viewModel.updateSettings(s)
                            }
                        )
                    }
                }
            }

            // BEHAVIOR
            item {
                SectionHeader("BEHAVIOR & PERFORMANCE")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        SwitchSettingRow(
                            title = "Live Analysis",
                            subtitle = "Automatically update character frequencies as you type",
                            checked = uiState.liveAnalysis,
                            onCheckedChange = {
                                val s = createSettingsFromUiState(uiState).copy(liveAnalysis = it)
                                viewModel.updateSettings(s)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SwitchSettingRow(
                            title = "Haptic Feedback",
                            subtitle = "Light tactile feedback when analyzing, sorting, and copying",
                            checked = uiState.hapticFeedback,
                            onCheckedChange = {
                                val s = createSettingsFromUiState(uiState).copy(hapticFeedback = it)
                                viewModel.updateSettings(s)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SwitchSettingRow(
                            title = "Confirm Deletion",
                            subtitle = "Show confirmation dialog before clearing text or history",
                            checked = uiState.confirmDelete,
                            onCheckedChange = {
                                val s = createSettingsFromUiState(uiState).copy(confirmDelete = it)
                                viewModel.updateSettings(s)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SwitchSettingRow(
                            title = "Restore Last Text",
                            subtitle = "Restore previously entered text on app launch",
                            checked = uiState.restoreLastText,
                            onCheckedChange = {
                                val s = createSettingsFromUiState(uiState).copy(restoreLastText = it)
                                viewModel.updateSettings(s)
                            }
                        )
                    }
                }
            }

            // ACTIONS
            item {
                SectionHeader("MANAGEMENT")
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Settings to Defaults")
                    }

                    OutlinedButton(
                        onClick = { showClearHistoryDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear All Saved History")
                    }
                }
            }
        }
    }

    // Reset settings confirmation dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset all settings?") },
            text = { Text("This will restore all default preferences. Your saved history will NOT be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSettingsToDefaults()
                        showResetConfirmDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear history confirmation dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear all saved analyses?") },
            text = { Text("This will delete all saved entries in history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun SwitchSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private fun createSettingsFromUiState(s: CharFreqUiState): CharFreqSettings {
    return CharFreqSettings(
        theme = s.theme,
        dynamicColor = s.dynamicColor,
        countingUnit = s.countingUnit,
        caseSensitive = s.caseSensitive,
        normalization = s.normalization,
        whitespaceMode = s.whitespaceMode,
        includePunctuation = s.includePunctuation,
        includeDigits = s.includeDigits,
        includeSymbols = s.includeSymbols,
        includeLetters = s.includeLetters,
        includeMarks = s.includeMarks,
        sortMode = s.sortMode,
        sortDirection = s.sortDirection,
        resultLimit = s.resultLimit,
        topCharactersCount = s.topCharactersCount,
        showFrequencyChart = s.showFrequencyChart,
        showFrequencyDistribution = s.showFrequencyDistribution,
        showCategorySummary = s.showCategorySummary,
        showUnicodeDetails = s.showUnicodeDetails,
        showProcessingPreview = s.showProcessingPreview,
        liveAnalysis = s.liveAnalysis,
        hapticFeedback = s.hapticFeedback,
        animations = s.animations,
        confirmDelete = s.confirmDelete,
        restoreLastText = s.restoreLastText,
        lastText = s.text
    )
}
