package com.example.charfreq.model

data class CharFreqSettings(
    val countingUnit: CountingUnit = CountingUnit.GRAPHEME,
    val caseSensitive: Boolean = false,
    val normalization: NormalizationMode = NormalizationMode.NONE,
    val whitespaceMode: WhitespaceMode = WhitespaceMode.INCLUDE_ALL,
    val includePunctuation: Boolean = true,
    val includeDigits: Boolean = true,
    val includeSymbols: Boolean = true,
    val includeLetters: Boolean = true,
    val includeMarks: Boolean = true,
    val sortMode: SortMode = SortMode.FREQUENCY,
    val sortDirection: SortDirection = SortDirection.DESCENDING,
    val resultLimit: Int = 0, // 0 means All, or 10, 25, 50, 100
    val topCharactersCount: Int = 5,
    val showFrequencyChart: Boolean = true,
    val showFrequencyDistribution: Boolean = false,
    val showCategorySummary: Boolean = true,
    val showUnicodeDetails: Boolean = false,
    val showProcessingPreview: Boolean = false,
    val liveAnalysis: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animations: Boolean = true,
    val confirmDelete: Boolean = true,
    val restoreLastText: Boolean = true,
    val theme: String = "System", // System, Light, Dark
    val dynamicColor: Boolean = true,
    val lastText: String = ""
)
