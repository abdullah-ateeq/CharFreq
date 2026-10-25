package com.example.charfreq.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.charfreq.formatter.ResultFormatter
import com.example.charfreq.model.*
import com.example.charfreq.ui.CharFreqViewModel
import com.example.charfreq.ui.components.*
import com.example.charfreq.unicode.UnicodeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CharFreqViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val clipboard = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showCopyOptionsDialog by remember { mutableStateOf(false) }
    var showFiltersExpanded by remember { mutableStateOf(false) }

    val displayList = state.displayResults
    val maxDisplayFreq = remember(displayList) { displayList.maxOfOrNull { it.frequency } ?: 1 }

    fun triggerHaptic() {
        if (state.hapticFeedback) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Handle Toast messages
    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearInfoMessage()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CharFreq",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Show frequency of every character in text.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Text Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TEXT INPUT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.2.sp
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = {
                                        triggerHaptic()
                                        val sample = "Hello world! 👋\nCharFreq: 123 ❤️\nCafé & résumé."
                                        viewModel.onTextChange(sample)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Sample", style = MaterialTheme.typography.labelSmall)
                                }

                                TextButton(
                                    onClick = {
                                        triggerHaptic()
                                        val clip = clipboard.primaryClip
                                        if (clip != null && clip.itemCount > 0) {
                                            val pasted = clip.getItemAt(0).coerceToText(context).toString()
                                            viewModel.onTextChange(pasted)
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Paste", style = MaterialTheme.typography.labelSmall)
                                }

                                if (state.text.isNotEmpty()) {
                                    TextButton(
                                        onClick = {
                                            if (state.confirmDelete) {
                                                showClearConfirmDialog = true
                                            } else {
                                                triggerHaptic()
                                                viewModel.clearText()
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Clear", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.text,
                            onValueChange = { viewModel.onTextChange(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 130.dp, max = 220.dp),
                            placeholder = {
                                Text("Paste or type your text here...")
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Default
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live Text Statistics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LiveStatItem("Characters", state.text.length.toString())
                            LiveStatItem("Letters", state.liveLettersCount.toString())
                            LiveStatItem("Digits", state.liveDigitsCount.toString())
                            LiveStatItem("Spaces", state.liveSpacesCount.toString())
                            LiveStatItem("Lines", state.liveLinesCount.toString())
                        }
                    }
                }
            }

            // Counting Unit Selector
            item {
                Column {
                    Text(
                        text = "COUNTING UNIT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        CountingUnit.values().forEachIndexed { index, unit ->
                            SegmentedButton(
                                selected = state.countingUnit == unit,
                                onClick = {
                                    triggerHaptic()
                                    viewModel.setCountingUnit(unit)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = CountingUnit.values().size)
                            ) {
                                Text(
                                    text = when (unit) {
                                        CountingUnit.GRAPHEME -> "Graphemes"
                                        CountingUnit.CODE_POINT -> "Code Points"
                                        CountingUnit.UTF16_CODE_UNIT -> "UTF-16"
                                    },
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.countingUnit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Prominent Analyze Button
            item {
                Button(
                    onClick = {
                        triggerHaptic()
                        viewModel.analyzeText()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !state.isAnalyzing
                ) {
                    if (state.isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Analyzing...")
                    } else {
                        Icon(Icons.Default.Analytics, contentDescription = "Analyze text for character frequency.")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ANALYZE TEXT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }

            // Quick Filters & Options Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFiltersExpanded = !showFiltersExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ANALYSIS SETTINGS & FILTERS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.2.sp
                            )
                            Icon(
                                imageVector = if (showFiltersExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle filters"
                            )
                        }

                        AnimatedVisibility(visible = showFiltersExpanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                // Case Sensitivity
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Case Sensitivity", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        Text(
                                            if (state.caseSensitive) "Respect Case (A ≠ a)" else "Ignore Case (A = a)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = state.caseSensitive,
                                        onCheckedChange = {
                                            triggerHaptic()
                                            viewModel.setCaseSensitive(it)
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Whitespace Mode
                                Text("Whitespace Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    WhitespaceMode.values().forEach { mode ->
                                        FilterChip(
                                            selected = state.whitespaceMode == mode,
                                            onClick = {
                                                triggerHaptic()
                                                viewModel.setWhitespaceMode(mode)
                                            },
                                            label = { Text(mode.label, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Inclusion Filters Chips
                                Text("Include Categories", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = state.includePunctuation,
                                        onClick = {
                                            triggerHaptic()
                                            viewModel.setIncludePunctuation(!state.includePunctuation)
                                        },
                                        label = { Text("Punctuation", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    FilterChip(
                                        selected = state.includeDigits,
                                        onClick = {
                                            triggerHaptic()
                                            viewModel.setIncludeDigits(!state.includeDigits)
                                        },
                                        label = { Text("Digits", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    FilterChip(
                                        selected = state.includeSymbols,
                                        onClick = {
                                            triggerHaptic()
                                            viewModel.setIncludeSymbols(!state.includeSymbols)
                                        },
                                        label = { Text("Symbols", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Normalization selector
                                Text("Unicode Normalization", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    NormalizationMode.values().forEach { mode ->
                                        FilterChip(
                                            selected = state.normalization == mode,
                                            onClick = {
                                                triggerHaptic()
                                                viewModel.setNormalization(mode)
                                            },
                                            label = { Text(mode.name, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Results Section (Only shown when results are present or text analyzed)
            if (state.analyzedCharacters > 0) {
                item {
                    MainSummaryCard(state = state)
                }

                item {
                    MostFrequentCard(state = state)
                }

                item {
                    LeastFrequentCard(state = state)
                }

                if (state.showCategorySummary) {
                    item {
                        CategorySummaryCard(state = state)
                    }
                }

                if (state.showFrequencyChart) {
                    item {
                        FrequencyBarChart(results = state.results, topCount = state.topCharactersCount)
                    }
                }

                if (state.showFrequencyDistribution) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "FREQUENCY DISTRIBUTION",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                state.frequencyDistribution.forEach { (label, count) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(label, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "$count character(s)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.showUnicodeDetails) {
                    item {
                        AsciiAndUnicodeSummaryCard(state = state)
                    }
                }

                if (state.showProcessingPreview && state.previewItems.isNotEmpty()) {
                    item {
                        ProcessingPreviewCard(previewItems = state.previewItems)
                    }
                }

                item {
                    FilterSummaryCard(state = state)
                }

                // Search & Sort Bar
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search characters (e.g. space, e, U+0020)...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search characters") },
                            trailingIcon = {
                                if (state.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            singleLine = true
                        )

                        // Sort and Result Limit controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sort mode chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SortModeMenu(
                                    currentMode = state.sortMode,
                                    onSelectMode = {
                                        triggerHaptic()
                                        viewModel.setSortMode(it)
                                    }
                                )

                                IconButton(
                                    onClick = {
                                        triggerHaptic()
                                        val newDir = if (state.sortDirection == SortDirection.DESCENDING) {
                                            SortDirection.ASCENDING
                                        } else {
                                            SortDirection.DESCENDING
                                        }
                                        viewModel.setSortDirection(newDir)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (state.sortDirection == SortDirection.DESCENDING) {
                                            Icons.Default.ArrowDownward
                                        } else {
                                            Icons.Default.ArrowUpward
                                        },
                                        contentDescription = "Toggle sort direction"
                                    )
                                }
                            }

                            // Limit selector
                            ResultLimitMenu(
                                currentLimit = state.resultLimit,
                                onSelectLimit = {
                                    triggerHaptic()
                                    viewModel.setResultLimit(it)
                                }
                            )
                        }
                    }
                }

                // Results list items
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CHARACTER FREQUENCY (${displayList.size} displayed / ${state.uniqueCharacters} total)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                items(displayList, key = { it.rank.toString() + "_" + it.rawCharacter }) { item ->
                    CharacterFrequencyRow(
                        item = item,
                        maxFrequency = maxDisplayFreq,
                        onClick = {
                            triggerHaptic()
                            viewModel.selectCharacterForDetail(item)
                        }
                    )
                }

                // Bottom Action Buttons
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                triggerHaptic()
                                showCopyOptionsDialog = true
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy results", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("COPY", style = MaterialTheme.typography.labelMedium)
                        }

                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                triggerHaptic()
                                viewModel.saveToHistory()
                            }
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Save to history", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SAVE", style = MaterialTheme.typography.labelMedium)
                        }

                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                triggerHaptic()
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "CharFreq Character Analysis")
                                    putExtra(Intent.EXTRA_TEXT, viewModel.getShareText())
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Character Frequency"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share results", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SHARE", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                triggerHaptic()
                                val csv = viewModel.getCsvExport()
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "charfreq_export.csv")
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export CSV"))
                            }
                        ) {
                            Text("CSV", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    // Detail Bottom Sheet
    state.selectedCharacterForDetail?.let { selectedItem ->
        CharacterDetailSheet(
            item = selectedItem,
            onDismiss = { viewModel.selectCharacterForDetail(null) }
        )
    }

    // Clear Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear all text?") },
            text = { Text("This will clear the current text input and analysis results.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        triggerHaptic()
                        viewModel.clearText()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Copy Options Dialog
    if (showCopyOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showCopyOptionsDialog = false },
            title = { Text("Copy Results") },
            text = { Text("Choose what to copy to clipboard:") },
            confirmButton = {
                TextButton(
                    onClick = {
                        triggerHaptic()
                        val text = viewModel.getCopyText(full = true)
                        val clip = ClipData.newPlainText("CharFreq All", text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "All results copied to clipboard", Toast.LENGTH_SHORT).show()
                        showCopyOptionsDialog = false
                    }
                ) {
                    Text("Copy All (${state.uniqueCharacters})")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        triggerHaptic()
                        val text = viewModel.getCopyText(full = false)
                        val clip = ClipData.newPlainText("CharFreq Top 10", text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Top results copied to clipboard", Toast.LENGTH_SHORT).show()
                        showCopyOptionsDialog = false
                    }
                ) {
                    Text("Copy Top 10")
                }
            }
        )
    }
}

@Composable
private fun LiveStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CharacterFrequencyRow(
    item: CharacterFrequency,
    maxFrequency: Int,
    onClick: () -> Unit
) {
    val contentDesc = ResultFormatter.getContentDescription(item)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .semantics { contentDescription = contentDesc },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank
                Text(
                    text = String.format("#%02d", item.rank),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp)
                )

                // Character glyph box
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.displayCharacter,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Category
                Column(modifier = Modifier.weight(1f)) {
                    val name = item.unicodeName ?: UnicodeUtils.getDisplayName(item.rawCharacter)
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = "${item.category.displayName} • ${item.codePointsHex}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Count & percentage
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${item.frequency}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = ResultFormatter.formatPercentage(item.percentage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Relative frequency bar
            val ratio = (item.frequency.toFloat() / maxFrequency.toFloat()).coerceIn(0.04f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(ratio)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun SortModeMenu(
    currentMode: SortMode,
    onSelectMode: (SortMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(currentMode.label, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortMode.values().forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.label) },
                    onClick = {
                        onSelectMode(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ResultLimitMenu(
    currentLimit: Int,
    onSelectLimit: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = if (currentLimit == 0) "Show: All" else "Show: Top $currentLimit"

    Box {
        TextButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(0, 10, 25, 50, 100).forEach { limit ->
                DropdownMenuItem(
                    text = { Text(if (limit == 0) "All" else "Top $limit") },
                    onClick = {
                        onSelectLimit(limit)
                        expanded = false
                    }
                )
            }
        }
    }
}
