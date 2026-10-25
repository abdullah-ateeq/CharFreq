package com.example.charfreq.unicode

import com.example.charfreq.model.CharacterCategory
import java.util.Locale

object UnicodeUtils {

    private val ASCII_CONTROL_NAMES = mapOf(
        0x00 to "NUL",
        0x01 to "SOH",
        0x02 to "STX",
        0x03 to "ETX",
        0x04 to "EOT",
        0x05 to "ENQ",
        0x06 to "ACK",
        0x07 to "BEL",
        0x08 to "BS",
        0x09 to "TAB",
        0x0A to "LF",
        0x0B to "VT",
        0x0C to "FF",
        0x0D to "CR",
        0x0E to "SO",
        0x0F to "SI",
        0x10 to "DLE",
        0x11 to "DC1",
        0x12 to "DC2",
        0x13 to "DC3",
        0x14 to "DC4",
        0x15 to "NAK",
        0x16 to "SYN",
        0x17 to "ETB",
        0x18 to "CAN",
        0x19 to "EM",
        0x1A to "SUB",
        0x1B to "ESC",
        0x1C to "FS",
        0x1D to "GS",
        0x1E to "RS",
        0x1F to "US",
        0x7F to "DEL"
    )

    private val SPECIAL_SAFE_DISPLAYS = mapOf(
        " " to "␠",
        "\t" to "⇥",
        "\n" to "↵",
        "\r\n" to "↵",
        "\r" to "CR",
        "\u00A0" to "NBSP",
        "\u2000" to "NQSP", // En quad
        "\u2001" to "MQSP", // Em quad
        "\u2002" to "ENSP", // En space
        "\u2003" to "EMSP", // Em space
        "\u2004" to "3/MSP", // Three-per-em space
        "\u2005" to "4/MSP", // Four-per-em space
        "\u2006" to "6/MSP", // Six-per-em space
        "\u2007" to "FSP",  // Figure space
        "\u2008" to "PSP",  // Punctuation space
        "\u2009" to "THSP", // Thin space
        "\u200A" to "HSP",  // Hair space
        "\u200B" to "ZWSP", // Zero width space
        "\u200C" to "ZWNJ", // Zero width non-joiner
        "\u200D" to "ZWJ",  // Zero width joiner
        "\u200E" to "LRM",  // Left-to-right mark
        "\u200F" to "RLM",  // Right-to-left mark
        "\u2028" to "LSEP", // Line separator
        "\u2029" to "PSEP", // Paragraph separator
        "\u202F" to "NNBSP", // Narrow no-break space
        "\u205F" to "MMSP", // Medium mathematical space
        "\u3000" to "IDSP", // Ideographic space
        "\uFEFF" to "ZWNBSP" // Byte order mark / zero width no-break space
    )

    fun getCodePoints(text: String): List<Int> {
        val list = mutableListOf<Int>()
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            list.add(cp)
            i += Character.charCount(cp)
        }
        return list
    }

    /**
     * Converts a character sequence into a safe display string.
     * Invisible characters and control characters are replaced with clean glyphs or tags.
     */
    fun getSafeDisplay(character: String): String {
        SPECIAL_SAFE_DISPLAYS[character]?.let { return it }

        if (character.isEmpty()) return "(empty)"

        // Check if single code point control character
        val codePoints = getCodePoints(character)
        if (codePoints.size == 1) {
            val cp = codePoints[0]
            ASCII_CONTROL_NAMES[cp]?.let { return it }
            if (Character.isISOControl(cp)) {
                return String.format(Locale.US, "U+%04X", cp)
            }
        }

        return character
    }

    /**
     * Human-friendly name or description for the character.
     */
    fun getDisplayName(character: String): String {
        when (character) {
            " " -> return "SPACE"
            "\t" -> return "TAB"
            "\n" -> return "NEWLINE"
            "\r\n" -> return "NEWLINE (CRLF)"
            "\r" -> return "CARRIAGE RETURN"
            "\u00A0" -> return "NO-BREAK SPACE"
            "\u200B" -> return "ZERO WIDTH SPACE"
            "\u200C" -> return "ZERO WIDTH NON-JOINER"
            "\u200D" -> return "ZERO WIDTH JOINER"
        }

        val codePoints = getCodePoints(character)
        if (codePoints.size == 1) {
            val cp = codePoints[0]
            ASCII_CONTROL_NAMES[cp]?.let { return "CONTROL $it" }
            try {
                val name = Character.getName(cp)
                if (!name.isNullOrBlank()) {
                    return name
                }
            } catch (_: Exception) {}
        } else if (codePoints.size > 1) {
            // Composite name
            val names = codePoints.mapNotNull { cp ->
                try {
                    Character.getName(cp)
                } catch (_: Exception) {
                    null
                }
            }
            if (names.isNotEmpty()) {
                return names.joinToString(" + ")
            }
        }

        return getSafeDisplay(character)
    }

    /**
     * Official Unicode Name or composite names.
     */
    fun getUnicodeName(character: String): String? {
        val codePoints = getCodePoints(character)
        if (codePoints.isEmpty()) return null
        val names = codePoints.map { cp ->
            try {
                Character.getName(cp) ?: String.format(Locale.US, "U+%04X", cp)
            } catch (_: Exception) {
                String.format(Locale.US, "U+%04X", cp)
            }
        }
        return names.joinToString(" + ")
    }

    /**
     * Unicode 2-letter Category Abbreviation (e.g. Lu, Ll, Nd, Zs, Po, etc.)
     */
    fun getCategoryAbbreviation(character: String): String {
        val codePoints = getCodePoints(character)
        if (codePoints.isEmpty()) return "Cn"
        val firstCp = codePoints.first()
        return when (Character.getType(firstCp).toByte()) {
            Character.UPPERCASE_LETTER -> "Lu"
            Character.LOWERCASE_LETTER -> "Ll"
            Character.TITLECASE_LETTER -> "Lt"
            Character.MODIFIER_LETTER -> "Lm"
            Character.OTHER_LETTER -> "Lo"
            Character.NON_SPACING_MARK -> "Mn"
            Character.ENCLOSING_MARK -> "Me"
            Character.COMBINING_SPACING_MARK -> "Mc"
            Character.DECIMAL_DIGIT_NUMBER -> "Nd"
            Character.LETTER_NUMBER -> "Nl"
            Character.OTHER_NUMBER -> "No"
            Character.SPACE_SEPARATOR -> "Zs"
            Character.LINE_SEPARATOR -> "Zl"
            Character.PARAGRAPH_SEPARATOR -> "Zp"
            Character.CONTROL -> "Cc"
            Character.FORMAT -> "Cf"
            Character.PRIVATE_USE -> "Co"
            Character.SURROGATE -> "Cs"
            Character.DASH_PUNCTUATION -> "Pd"
            Character.START_PUNCTUATION -> "Ps"
            Character.END_PUNCTUATION -> "Pe"
            Character.CONNECTOR_PUNCTUATION -> "Pc"
            Character.OTHER_PUNCTUATION -> "Po"
            Character.MATH_SYMBOL -> "Sm"
            Character.CURRENCY_SYMBOL -> "Sc"
            Character.MODIFIER_SYMBOL -> "Sk"
            Character.OTHER_SYMBOL -> "So"
            Character.INITIAL_QUOTE_PUNCTUATION -> "Pi"
            Character.FINAL_QUOTE_PUNCTUATION -> "Pf"
            else -> "Cn"
        }
    }

    /**
     * Maps character to CharacterCategory
     */
    fun getCategory(character: String): CharacterCategory {
        // Handle common whitespace sequences directly
        if (character == " " || character == "\t" || character == "\n" ||
            character == "\r" || character == "\r\n" || character == "\u00A0" ||
            character == "\u2028" || character == "\u2029"
        ) {
            return CharacterCategory.WHITESPACE
        }

        val codePoints = getCodePoints(character)
        if (codePoints.isEmpty()) return CharacterCategory.OTHER

        // Find primary code point (non-modifier)
        val primaryCp = codePoints.first()
        val type = Character.getType(primaryCp).toByte()

        return when (type) {
            Character.UPPERCASE_LETTER,
            Character.LOWERCASE_LETTER,
            Character.TITLECASE_LETTER,
            Character.MODIFIER_LETTER,
            Character.OTHER_LETTER -> CharacterCategory.LETTER

            Character.DECIMAL_DIGIT_NUMBER,
            Character.LETTER_NUMBER,
            Character.OTHER_NUMBER -> CharacterCategory.DIGIT

            Character.SPACE_SEPARATOR,
            Character.LINE_SEPARATOR,
            Character.PARAGRAPH_SEPARATOR -> CharacterCategory.WHITESPACE

            Character.CONNECTOR_PUNCTUATION,
            Character.DASH_PUNCTUATION,
            Character.START_PUNCTUATION,
            Character.END_PUNCTUATION,
            Character.INITIAL_QUOTE_PUNCTUATION,
            Character.FINAL_QUOTE_PUNCTUATION,
            Character.OTHER_PUNCTUATION -> CharacterCategory.PUNCTUATION

            Character.MATH_SYMBOL,
            Character.CURRENCY_SYMBOL,
            Character.MODIFIER_SYMBOL,
            Character.OTHER_SYMBOL -> CharacterCategory.SYMBOL

            Character.NON_SPACING_MARK,
            Character.COMBINING_SPACING_MARK,
            Character.ENCLOSING_MARK -> CharacterCategory.MARK

            Character.CONTROL -> {
                // If it's tab, LF, CR, etc.
                if (primaryCp == 0x09 || primaryCp == 0x0A || primaryCp == 0x0D || primaryCp == 0x0C) {
                    CharacterCategory.WHITESPACE
                } else {
                    CharacterCategory.CONTROL
                }
            }

            Character.FORMAT -> {
                // ZWJ, ZWNJ, LRM, RLM etc.
                CharacterCategory.CONTROL
            }

            else -> CharacterCategory.OTHER
        }
    }

    fun isAscii(character: String): Boolean {
        return character.all { it.code in 0..127 }
    }

    fun isPrintableAscii(character: String): Boolean {
        return character.all { it.code in 32..126 }
    }

    fun isAsciiControl(character: String): Boolean {
        return character.all { it.code in 0..31 || it.code == 127 }
    }

    fun isSpaceOnly(character: String): Boolean {
        return character == " " || character == "\u00A0" ||
               (character.length == 1 && Character.getType(character[0].code).toByte() == Character.SPACE_SEPARATOR)
    }
}
