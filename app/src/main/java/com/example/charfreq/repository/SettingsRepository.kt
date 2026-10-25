package com.example.charfreq.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.charfreq.model.CharFreqSettings
import com.example.charfreq.model.CountingUnit
import com.example.charfreq.model.NormalizationMode
import com.example.charfreq.model.SortDirection
import com.example.charfreq.model.SortMode
import com.example.charfreq.model.WhitespaceMode

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("charfreq_settings_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "pref_theme"
        private const val KEY_DYNAMIC_COLOR = "pref_dynamic_color"
        private const val KEY_COUNTING_UNIT = "pref_counting_unit"
        private const val KEY_CASE_SENSITIVE = "pref_case_sensitive"
        private const val KEY_NORMALIZATION = "pref_normalization"
        private const val KEY_WHITESPACE_MODE = "pref_whitespace_mode"
        private const val KEY_INCLUDE_PUNCTUATION = "pref_include_punctuation"
        private const val KEY_INCLUDE_DIGITS = "pref_include_digits"
        private const val KEY_INCLUDE_SYMBOLS = "pref_include_symbols"
        private const val KEY_INCLUDE_LETTERS = "pref_include_letters"
        private const val KEY_INCLUDE_MARKS = "pref_include_marks"
        private const val KEY_SORT_MODE = "pref_sort_mode"
        private const val KEY_SORT_DIRECTION = "pref_sort_direction"
        private const val KEY_RESULT_LIMIT = "pref_result_limit"
        private const val KEY_TOP_CHARACTERS_COUNT = "pref_top_characters_count"
        private const val KEY_SHOW_FREQUENCY_CHART = "pref_show_frequency_chart"
        private const val KEY_SHOW_FREQUENCY_DISTRIBUTION = "pref_show_frequency_distribution"
        private const val KEY_SHOW_CATEGORY_SUMMARY = "pref_show_category_summary"
        private const val KEY_SHOW_UNICODE_DETAILS = "pref_show_unicode_details"
        private const val KEY_SHOW_PROCESSING_PREVIEW = "pref_show_processing_preview"
        private const val KEY_LIVE_ANALYSIS = "pref_live_analysis"
        private const val KEY_HAPTIC_FEEDBACK = "pref_haptic_feedback"
        private const val KEY_ANIMATIONS = "pref_animations"
        private const val KEY_CONFIRM_DELETE = "pref_confirm_delete"
        private const val KEY_RESTORE_LAST_TEXT = "pref_restore_last_text"
        private const val KEY_LAST_TEXT = "pref_last_text"
    }

    fun loadSettings(): CharFreqSettings {
        return CharFreqSettings(
            theme = prefs.getString(KEY_THEME, "System") ?: "System",
            dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true),
            countingUnit = try {
                CountingUnit.valueOf(prefs.getString(KEY_COUNTING_UNIT, CountingUnit.GRAPHEME.name) ?: CountingUnit.GRAPHEME.name)
            } catch (_: Exception) { CountingUnit.GRAPHEME },
            caseSensitive = prefs.getBoolean(KEY_CASE_SENSITIVE, false),
            normalization = try {
                NormalizationMode.valueOf(prefs.getString(KEY_NORMALIZATION, NormalizationMode.NONE.name) ?: NormalizationMode.NONE.name)
            } catch (_: Exception) { NormalizationMode.NONE },
            whitespaceMode = try {
                WhitespaceMode.valueOf(prefs.getString(KEY_WHITESPACE_MODE, WhitespaceMode.INCLUDE_ALL.name) ?: WhitespaceMode.INCLUDE_ALL.name)
            } catch (_: Exception) { WhitespaceMode.INCLUDE_ALL },
            includePunctuation = prefs.getBoolean(KEY_INCLUDE_PUNCTUATION, true),
            includeDigits = prefs.getBoolean(KEY_INCLUDE_DIGITS, true),
            includeSymbols = prefs.getBoolean(KEY_INCLUDE_SYMBOLS, true),
            includeLetters = prefs.getBoolean(KEY_INCLUDE_LETTERS, true),
            includeMarks = prefs.getBoolean(KEY_INCLUDE_MARKS, true),
            sortMode = try {
                SortMode.valueOf(prefs.getString(KEY_SORT_MODE, SortMode.FREQUENCY.name) ?: SortMode.FREQUENCY.name)
            } catch (_: Exception) { SortMode.FREQUENCY },
            sortDirection = try {
                SortDirection.valueOf(prefs.getString(KEY_SORT_DIRECTION, SortDirection.DESCENDING.name) ?: SortDirection.DESCENDING.name)
            } catch (_: Exception) { SortDirection.DESCENDING },
            resultLimit = prefs.getInt(KEY_RESULT_LIMIT, 0),
            topCharactersCount = prefs.getInt(KEY_TOP_CHARACTERS_COUNT, 5),
            showFrequencyChart = prefs.getBoolean(KEY_SHOW_FREQUENCY_CHART, true),
            showFrequencyDistribution = prefs.getBoolean(KEY_SHOW_FREQUENCY_DISTRIBUTION, false),
            showCategorySummary = prefs.getBoolean(KEY_SHOW_CATEGORY_SUMMARY, true),
            showUnicodeDetails = prefs.getBoolean(KEY_SHOW_UNICODE_DETAILS, false),
            showProcessingPreview = prefs.getBoolean(KEY_SHOW_PROCESSING_PREVIEW, false),
            liveAnalysis = prefs.getBoolean(KEY_LIVE_ANALYSIS, true),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true),
            animations = prefs.getBoolean(KEY_ANIMATIONS, true),
            confirmDelete = prefs.getBoolean(KEY_CONFIRM_DELETE, true),
            restoreLastText = prefs.getBoolean(KEY_RESTORE_LAST_TEXT, true),
            lastText = prefs.getString(KEY_LAST_TEXT, "") ?: ""
        )
    }

    fun saveSettings(settings: CharFreqSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.theme)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .putString(KEY_COUNTING_UNIT, settings.countingUnit.name)
            .putBoolean(KEY_CASE_SENSITIVE, settings.caseSensitive)
            .putString(KEY_NORMALIZATION, settings.normalization.name)
            .putString(KEY_WHITESPACE_MODE, settings.whitespaceMode.name)
            .putBoolean(KEY_INCLUDE_PUNCTUATION, settings.includePunctuation)
            .putBoolean(KEY_INCLUDE_DIGITS, settings.includeDigits)
            .putBoolean(KEY_INCLUDE_SYMBOLS, settings.includeSymbols)
            .putBoolean(KEY_INCLUDE_LETTERS, settings.includeLetters)
            .putBoolean(KEY_INCLUDE_MARKS, settings.includeMarks)
            .putString(KEY_SORT_MODE, settings.sortMode.name)
            .putString(KEY_SORT_DIRECTION, settings.sortDirection.name)
            .putInt(KEY_RESULT_LIMIT, settings.resultLimit)
            .putInt(KEY_TOP_CHARACTERS_COUNT, settings.topCharactersCount)
            .putBoolean(KEY_SHOW_FREQUENCY_CHART, settings.showFrequencyChart)
            .putBoolean(KEY_SHOW_FREQUENCY_DISTRIBUTION, settings.showFrequencyDistribution)
            .putBoolean(KEY_SHOW_CATEGORY_SUMMARY, settings.showCategorySummary)
            .putBoolean(KEY_SHOW_UNICODE_DETAILS, settings.showUnicodeDetails)
            .putBoolean(KEY_SHOW_PROCESSING_PREVIEW, settings.showProcessingPreview)
            .putBoolean(KEY_LIVE_ANALYSIS, settings.liveAnalysis)
            .putBoolean(KEY_HAPTIC_FEEDBACK, settings.hapticFeedback)
            .putBoolean(KEY_ANIMATIONS, settings.animations)
            .putBoolean(KEY_CONFIRM_DELETE, settings.confirmDelete)
            .putBoolean(KEY_RESTORE_LAST_TEXT, settings.restoreLastText)
            .putString(KEY_LAST_TEXT, settings.lastText)
            .apply()
    }

    fun saveLastText(text: String) {
        prefs.edit().putString(KEY_LAST_TEXT, text).apply()
    }

    fun resetSettings(): CharFreqSettings {
        val lastText = prefs.getString(KEY_LAST_TEXT, "") ?: ""
        val defaultSettings = CharFreqSettings(lastText = lastText)
        saveSettings(defaultSettings)
        return defaultSettings
    }
}
