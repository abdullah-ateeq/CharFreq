package com.example.charfreq.formatter

import com.example.charfreq.model.CharacterFrequency
import java.util.Locale

object ResultFormatter {

    fun formatPercentage(percentage: Double): String {
        return String.format(Locale.US, "%.2f%%", percentage)
    }

    fun formatOccurrence(frequency: Int): String {
        return if (frequency == 1) "1 occurrence" else "$frequency occurrences"
    }

    fun getContentDescription(item: CharacterFrequency): String {
        val charDesc = when (item.rawCharacter) {
            " " -> "Space character"
            "\t" -> "Tab character"
            "\n" -> "Newline character"
            "\r\n" -> "Newline CRLF character"
            "\r" -> "Carriage return character"
            else -> "Character ${item.displayCharacter}"
        }
        return "$charDesc appears ${item.frequency} times, representing ${formatPercentage(item.percentage)} of analyzed characters."
    }

    fun getRelativeBarRatio(frequency: Int, maxFrequency: Int): Float {
        if (maxFrequency <= 0) return 0f
        return (frequency.toFloat() / maxFrequency.toFloat()).coerceIn(0.05f, 1f)
    }
}
