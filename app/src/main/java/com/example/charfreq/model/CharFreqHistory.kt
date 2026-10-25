package com.example.charfreq.model

data class CharFreqHistory(
    val id: String,
    val text: String,
    val analyzedAt: Long,
    val countingUnit: String,
    val caseSensitive: Boolean,
    val normalization: String,
    val whitespaceMode: String,
    val includePunctuation: Boolean,
    val includeDigits: Boolean,
    val includeSymbols: Boolean,
    val totalCharacters: Int = 0,
    val uniqueCharacters: Int = 0,
    val mostFrequentSummary: String = ""
)
