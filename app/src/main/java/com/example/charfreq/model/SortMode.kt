package com.example.charfreq.model

enum class SortMode(val label: String) {
    FREQUENCY("Frequency"),
    CHARACTER("Character"),
    CODE_POINT("Unicode Code Point"),
    CATEGORY("Category"),
    PERCENTAGE("Percentage")
}

enum class SortDirection(val label: String) {
    DESCENDING("Highest → Lowest"),
    ASCENDING("Lowest → Highest")
}
