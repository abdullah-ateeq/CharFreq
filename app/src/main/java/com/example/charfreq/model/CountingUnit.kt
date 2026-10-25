package com.example.charfreq.model

enum class CountingUnit(val label: String, val description: String) {
    GRAPHEME(
        label = "Grapheme Clusters",
        description = "A user-perceived character, such as an emoji or accented character. (Recommended)"
    ),
    CODE_POINT(
        label = "Unicode Code Points",
        description = "A Unicode character value (code point)."
    ),
    UTF16_CODE_UNIT(
        label = "UTF-16 Code Units",
        description = "The internal Java/Kotlin string storage unit (16-bit char)."
    )
}
