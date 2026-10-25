package com.example.charfreq.model

enum class NormalizationMode(val label: String, val description: String) {
    NONE("None", "Preserve characters exactly as typed"),
    NFC("NFC", "Canonical Decomposition, followed by Canonical Composition"),
    NFD("NFD", "Canonical Decomposition"),
    NFKC("NFKC", "Compatibility Decomposition, followed by Canonical Composition"),
    NFKD("NFKD", "Compatibility Decomposition")
}
