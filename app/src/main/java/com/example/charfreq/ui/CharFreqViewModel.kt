package com.example.charfreq.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.charfreq.analyzer.CharacterProcessor
import com.example.charfreq.export.ResultExporter
import com.example.charfreq.history.HistoryRepository
import com.example.charfreq.model.CharFreqHistory
import com.example.charfreq.model.CharFreqSettings
import com.example.charfreq.model.CharFreqUiState
import com.example.charfreq.model.CharacterFrequency
import com.example.charfreq.model.CharacterFrequencyResult
import com.example.charfreq.model.CountingUnit
import com.example.charfreq.model.NormalizationMode
import com.example.charfreq.model.SortDirection
import com.example.charfreq.model.SortMode
import com.example.charfreq.model.WhitespaceMode
import com.example.charfreq.repository.SettingsRepository
import com.example.charfreq.unicode.UnicodeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class CharFreqViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)
    private val historyRepo = HistoryRepository(application)

    private val _uiState = MutableStateFlow(CharFreqUiState())
    val uiState: StateFlow<CharFreqUiState> = _uiState.asStateFlow()

    private val _historyList = MutableStateFlow<List<CharFreqHistory>>(emptyList())
    val historyList: StateFlow<List<CharFreqHistory>> = _historyList.asStateFlow()

    private var analysisJob: Job? = null
    private var liveDebounceJob: Job? = null

    init {
        loadInitialState()
        refreshHistory()
    }

    private fun loadInitialState() {
        val s = settingsRepo.loadSettings()
        val initialText = if (s.restoreLastText) s.lastText else ""
        _uiState.update {
            it.copy(
                text = initialText,
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
                theme = s.theme,
                dynamicColor = s.dynamicColor
            )
        }

        if (initialText.isNotEmpty()) {
            performAnalysis(immediate = true)
        }
    }

    fun onTextChange(newText: String) {
        _uiState.update { it.copy(text = newText, errorMessage = null, infoMessage = null) }
        settingsRepo.saveLastText(newText)

        if (_uiState.value.liveAnalysis) {
            liveDebounceJob?.cancel()
            liveDebounceJob = viewModelScope.launch {
                delay(300) // 250-400 ms debounce
                performAnalysis(immediate = false)
            }
        }
    }

    fun analyzeText() {
        if (_uiState.value.text.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Enter some text to analyze.") }
            return
        }
        performAnalysis(immediate = true)
    }

    private fun performAnalysis(immediate: Boolean) {
        analysisJob?.cancel()
        val text = _uiState.value.text
        if (text.isEmpty()) {
            _uiState.update {
                it.copy(
                    results = emptyList(),
                    totalRawUnits = 0,
                    totalCodePoints = 0,
                    totalGraphemes = 0,
                    analyzedCharacters = 0,
                    excludedCharacters = 0,
                    uniqueCharacters = 0,
                    letterCount = 0,
                    digitCount = 0,
                    whitespaceCount = 0,
                    punctuationCount = 0,
                    symbolCount = 0,
                    markCount = 0,
                    controlCount = 0,
                    otherCount = 0,
                    uppercaseLetterCount = 0,
                    lowercaseLetterCount = 0,
                    asciiCount = 0,
                    nonAsciiCount = 0,
                    printableAsciiCount = 0,
                    asciiControlCount = 0,
                    mostFrequent = emptyList(),
                    leastFrequentCount = 0,
                    frequencyDistribution = emptyMap(),
                    previewItems = emptyList(),
                    isAnalyzing = false
                )
            }
            return
        }

        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null) }

        analysisJob = viewModelScope.launch {
            val currentState = _uiState.value
            val settings = CharFreqSettings(
                countingUnit = currentState.countingUnit,
                caseSensitive = currentState.caseSensitive,
                normalization = currentState.normalization,
                whitespaceMode = currentState.whitespaceMode,
                includePunctuation = currentState.includePunctuation,
                includeDigits = currentState.includeDigits,
                includeSymbols = currentState.includeSymbols,
                includeLetters = currentState.includeLetters,
                includeMarks = currentState.includeMarks,
                sortMode = currentState.sortMode,
                sortDirection = currentState.sortDirection,
                resultLimit = currentState.resultLimit,
                topCharactersCount = currentState.topCharactersCount,
                showFrequencyChart = currentState.showFrequencyChart,
                showFrequencyDistribution = currentState.showFrequencyDistribution,
                showCategorySummary = currentState.showCategorySummary,
                showUnicodeDetails = currentState.showUnicodeDetails,
                showProcessingPreview = currentState.showProcessingPreview
            )

            val res = withContext(Dispatchers.Default) {
                CharacterProcessor.process(text, settings)
            }

            _uiState.update {
                it.copy(
                    results = res.frequencies,
                    totalRawUnits = res.totalRawUnits,
                    totalCodePoints = res.totalCodePoints,
                    totalGraphemes = res.totalGraphemes,
                    analyzedCharacters = res.analyzedCharacters,
                    excludedCharacters = res.excludedCharacters,
                    uniqueCharacters = res.uniqueCharacters,
                    letterCount = res.letterCount,
                    digitCount = res.digitCount,
                    whitespaceCount = res.whitespaceCount,
                    punctuationCount = res.punctuationCount,
                    symbolCount = res.symbolCount,
                    markCount = res.markCount,
                    controlCount = res.controlCount,
                    otherCount = res.otherCount,
                    uppercaseLetterCount = res.uppercaseLetterCount,
                    lowercaseLetterCount = res.lowercaseLetterCount,
                    asciiCount = res.asciiCount,
                    nonAsciiCount = res.nonAsciiCount,
                    printableAsciiCount = res.printableAsciiCount,
                    asciiControlCount = res.asciiControlCount,
                    mostFrequent = res.mostFrequent,
                    leastFrequentCount = res.leastFrequentCount,
                    frequencyDistribution = res.frequencyDistribution,
                    previewItems = res.previewItems,
                    isAnalyzing = false
                )
            }
        }
    }

    fun clearText() {
        analysisJob?.cancel()
        liveDebounceJob?.cancel()
        _uiState.update {
            it.copy(
                text = "",
                results = emptyList(),
                totalRawUnits = 0,
                totalCodePoints = 0,
                totalGraphemes = 0,
                analyzedCharacters = 0,
                excludedCharacters = 0,
                uniqueCharacters = 0,
                letterCount = 0,
                digitCount = 0,
                whitespaceCount = 0,
                punctuationCount = 0,
                symbolCount = 0,
                markCount = 0,
                controlCount = 0,
                otherCount = 0,
                uppercaseLetterCount = 0,
                lowercaseLetterCount = 0,
                asciiCount = 0,
                nonAsciiCount = 0,
                printableAsciiCount = 0,
                asciiControlCount = 0,
                mostFrequent = emptyList(),
                leastFrequentCount = 0,
                frequencyDistribution = emptyMap(),
                previewItems = emptyList(),
                searchQuery = "",
                errorMessage = null,
                infoMessage = null
            )
        }
        settingsRepo.saveLastText("")
    }

    fun setCountingUnit(unit: CountingUnit) {
        _uiState.update { it.copy(countingUnit = unit) }
        persistCurrentSettings()
        performAnalysis(immediate = true)
    }

    fun setCaseSensitive(sensitive: Boolean) {
        _uiState.update { it.copy(caseSensitive = sensitive) }
        persistCurrentSettings()
        performAnalysis(immediate = true)
    }

    fun setNormalization(mode: NormalizationMode) {
        _uiState.update { it.copy(normalization = mode) }
        persistCurrentSettings()
        performAnalysis(immediate = true)
    }

    fun setWhitespaceMode(mode: WhitespaceMode) {
        _uiState.update { it.copy(whitespaceMode = mode) }
        persistCurrentSettings()
        performAnalysis(immediate = true)
    }

    fun setIncludePunctuation(include: Boolean) {
        _uiState.update { it.copy(includePunctuation = include) }
        persistCurrentSettings()
        performAnalysis(immediate = true)
    }

    fun setIncludeDigits(include: Boolean) {
        _uiState.update { it.copy(includeDigits = include) }
        persistCurrentSettings()
        performAnalysis(immediate = true)
    }

    fun setIncludeSymbols(include: Boolean) {
        _uiState.update { it.copy(includeSymbols = include) }
        persistCurrentSettings()
        performAnalysis(immediate = true)
    }

    fun setSortMode(mode: SortMode) {
        _uiState.update { it.copy(sortMode = mode) }
        persistCurrentSettings()
        performAnalysis(immediate = true)
    }

    fun setSortDirection(direction: SortDirection) {
        _uiState.update { it.copy(sortDirection = direction) }
        persistCurrentSettings()
        performAnalysis(immediate = true)
    }

    fun setResultLimit(limit: Int) {
        _uiState.update { it.copy(resultLimit = limit) }
        persistCurrentSettings()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCharacterForDetail(item: CharacterFrequency?) {
        _uiState.update { it.copy(selectedCharacterForDetail = item) }
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // History operations
    fun saveToHistory(): Boolean {
        val s = _uiState.value
        if (s.text.isBlank() || s.analyzedCharacters == 0) {
            _uiState.update { it.copy(errorMessage = "Analyze some text before saving to history.") }
            return false
        }

        val mostFrequentDesc = s.mostFrequent.firstOrNull()?.let {
            "${UnicodeUtils.getDisplayName(it.rawCharacter)} — ${it.frequency}"
        } ?: ""

        val entry = CharFreqHistory(
            id = UUID.randomUUID().toString(),
            text = s.text,
            analyzedAt = System.currentTimeMillis(),
            countingUnit = s.countingUnit.name,
            caseSensitive = s.caseSensitive,
            normalization = s.normalization.name,
            whitespaceMode = s.whitespaceMode.name,
            includePunctuation = s.includePunctuation,
            includeDigits = s.includeDigits,
            includeSymbols = s.includeSymbols,
            totalCharacters = s.analyzedCharacters,
            uniqueCharacters = s.uniqueCharacters,
            mostFrequentSummary = mostFrequentDesc
        )

        val saved = historyRepo.saveEntry(entry)
        refreshHistory()
        if (saved) {
            _uiState.update { it.copy(infoMessage = "Saved to history") }
        } else {
            _uiState.update { it.copy(infoMessage = "Already saved to history") }
        }
        return saved
    }

    fun restoreHistoryEntry(entry: CharFreqHistory) {
        val unit = try { CountingUnit.valueOf(entry.countingUnit) } catch (_: Exception) { CountingUnit.GRAPHEME }
        val norm = try { NormalizationMode.valueOf(entry.normalization) } catch (_: Exception) { NormalizationMode.NONE }
        val ws = try { WhitespaceMode.valueOf(entry.whitespaceMode) } catch (_: Exception) { WhitespaceMode.INCLUDE_ALL }

        _uiState.update {
            it.copy(
                text = entry.text,
                countingUnit = unit,
                caseSensitive = entry.caseSensitive,
                normalization = norm,
                whitespaceMode = ws,
                includePunctuation = entry.includePunctuation,
                includeDigits = entry.includeDigits,
                includeSymbols = entry.includeSymbols,
                searchQuery = "",
                infoMessage = "Restored analysis from history"
            )
        }
        settingsRepo.saveLastText(entry.text)
        persistCurrentSettings()
        performAnalysis(immediate = true)
    }

    fun deleteHistoryEntry(id: String) {
        historyRepo.deleteEntry(id)
        refreshHistory()
    }

    fun clearAllHistory() {
        historyRepo.clearHistory()
        refreshHistory()
        _uiState.update { it.copy(infoMessage = "History cleared") }
    }

    private fun refreshHistory() {
        _historyList.value = historyRepo.getHistory()
    }

    // Settings operations
    fun updateSettings(newSettings: CharFreqSettings) {
        settingsRepo.saveSettings(newSettings)
        _uiState.update {
            it.copy(
                theme = newSettings.theme,
                dynamicColor = newSettings.dynamicColor,
                countingUnit = newSettings.countingUnit,
                caseSensitive = newSettings.caseSensitive,
                normalization = newSettings.normalization,
                whitespaceMode = newSettings.whitespaceMode,
                includePunctuation = newSettings.includePunctuation,
                includeDigits = newSettings.includeDigits,
                includeSymbols = newSettings.includeSymbols,
                includeLetters = newSettings.includeLetters,
                includeMarks = newSettings.includeMarks,
                sortMode = newSettings.sortMode,
                sortDirection = newSettings.sortDirection,
                resultLimit = newSettings.resultLimit,
                topCharactersCount = newSettings.topCharactersCount,
                showFrequencyChart = newSettings.showFrequencyChart,
                showFrequencyDistribution = newSettings.showFrequencyDistribution,
                showCategorySummary = newSettings.showCategorySummary,
                showUnicodeDetails = newSettings.showUnicodeDetails,
                showProcessingPreview = newSettings.showProcessingPreview,
                liveAnalysis = newSettings.liveAnalysis,
                hapticFeedback = newSettings.hapticFeedback,
                animations = newSettings.animations,
                confirmDelete = newSettings.confirmDelete,
                restoreLastText = newSettings.restoreLastText
            )
        }
        performAnalysis(immediate = true)
    }

    fun resetSettingsToDefaults() {
        val s = settingsRepo.resetSettings()
        _uiState.update {
            it.copy(
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
                infoMessage = "Settings restored to defaults"
            )
        }
        performAnalysis(immediate = true)
    }

    private fun persistCurrentSettings() {
        val s = _uiState.value
        val settings = CharFreqSettings(
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
        settingsRepo.saveSettings(settings)
    }

    // Export helpers
    fun getCopyText(full: Boolean): String {
        val s = _uiState.value
        val res = CharacterFrequencyResult(
            frequencies = s.results,
            analyzedCharacters = s.analyzedCharacters,
            uniqueCharacters = s.uniqueCharacters
        )
        return ResultExporter.generateCopyText(res, full)
    }

    fun getShareText(): String {
        val s = _uiState.value
        val res = CharacterFrequencyResult(
            frequencies = s.results,
            analyzedCharacters = s.analyzedCharacters,
            uniqueCharacters = s.uniqueCharacters,
            letterCount = s.letterCount,
            digitCount = s.digitCount,
            whitespaceCount = s.whitespaceCount,
            punctuationCount = s.punctuationCount,
            symbolCount = s.symbolCount,
            mostFrequent = s.mostFrequent
        )
        return ResultExporter.generateShareText(res)
    }

    fun getCsvExport(): String {
        val s = _uiState.value
        val res = CharacterFrequencyResult(
            frequencies = s.results,
            analyzedCharacters = s.analyzedCharacters,
            uniqueCharacters = s.uniqueCharacters
        )
        return ResultExporter.generateCsv(res)
    }
}
