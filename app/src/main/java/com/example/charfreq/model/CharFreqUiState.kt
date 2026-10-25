package com.example.charfreq.model

data class CharFreqUiState(
    val text: String = "",
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
    val resultLimit: Int = 0,
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
    val theme: String = "System",
    val dynamicColor: Boolean = true,

    // Analysis results
    val results: List<CharacterFrequency> = emptyList(),
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
    val previewItems: List<Pair<String, String>> = emptyList(),

    // UI state
    val searchQuery: String = "",
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val selectedCharacterForDetail: CharacterFrequency? = null
) {
    val liveLettersCount: Int
        get() = text.codePoints().filter { Character.isLetter(it) }.count().toInt()

    val liveDigitsCount: Int
        get() = text.codePoints().filter { Character.isDigit(it) }.count().toInt()

    val liveSpacesCount: Int
        get() = text.codePoints().filter { Character.isWhitespace(it) || Character.isSpaceChar(it) }.count().toInt()

    val liveLinesCount: Int
        get() = if (text.isEmpty()) 0 else text.split(Regex("\r\n|\r|\n")).size

    val characterDiversityPercentage: Double
        get() = if (analyzedCharacters > 0) (uniqueCharacters.toDouble() / analyzedCharacters.toDouble()) * 100.0 else 0.0

    /**
     * Filtered results based on search query and result limit
     */
    val displayResults: List<CharacterFrequency>
        get() {
            val filtered = if (searchQuery.isBlank()) {
                results
            } else {
                val q = searchQuery.trim().lowercase()
                results.filter { item ->
                    item.rawCharacter.lowercase().contains(q) ||
                    item.displayCharacter.lowercase().contains(q) ||
                    (item.unicodeName?.lowercase()?.contains(q) == true) ||
                    item.category.displayName.lowercase().contains(q) ||
                    item.codePointsHex.lowercase().contains(q)
                }
            }
            return if (resultLimit > 0) filtered.take(resultLimit) else filtered
        }
}
