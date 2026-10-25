package com.example.charfreq.model

enum class CharacterCategory(val displayName: String, val sortOrder: Int) {
    LETTER("Letter", 1),
    DIGIT("Number", 2),
    PUNCTUATION("Punctuation", 3),
    SYMBOL("Symbol", 4),
    WHITESPACE("Whitespace", 5),
    MARK("Mark", 6),
    CONTROL("Control", 7),
    OTHER("Other", 8)
}
