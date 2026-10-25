package com.example.charfreq.model

data class CharacterFrequency(
    val displayCharacter: String,
    val rawCharacter: String,
    val frequency: Int,
    val percentage: Double,
    val category: CharacterCategory,
    val unicodeCodePoints: List<Int>,
    val unicodeName: String?,
    val rank: Int
) {
    val codePointsHex: String
        get() = unicodeCodePoints.joinToString(" ") { String.format("U+%04X", it) }
}
