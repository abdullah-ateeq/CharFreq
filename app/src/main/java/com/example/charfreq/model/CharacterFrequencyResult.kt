package com.example.charfreq.model

data class CharacterFrequencyResult(
    val frequencies: List<CharacterFrequency> = emptyList(),
    val totalRawUnits: Int = 0,
    val totalCodePoints: Int = 0,
    val totalGraphemes: Int = 0,
    val analyzedCharacters: Int = 0,
    val excludedCharacters: Int = 0,
    val uniqueCharacters: Int = 0,
    val letterCount: Int = 0,
    val digitCount: Int = 0,
    val whitespaceCount: Int = 0,
    val punctuationCount: Int = 0,
    val symbolCount: Int = 0,
    val markCount: Int = 0,
    val controlCount: Int = 0,
    val otherCount: Int = 0,
    val uppercaseLetterCount: Int = 0,
    val lowercaseLetterCount: Int = 0,
    val asciiCount: Int = 0,
    val nonAsciiCount: Int = 0,
    val printableAsciiCount: Int = 0,
    val asciiControlCount: Int = 0,
    val mostFrequent: List<CharacterFrequency> = emptyList(),
    val leastFrequentCount: Int = 0,
    val frequencyDistribution: Map<String, Int> = emptyMap(),
    val previewItems: List<Pair<String, String>> = emptyList() // Pair(raw, safeDisplay)
) {
    val characterDiversityPercentage: Double
        get() = if (analyzedCharacters > 0) (uniqueCharacters.toDouble() / analyzedCharacters.toDouble()) * 100.0 else 0.0
}
