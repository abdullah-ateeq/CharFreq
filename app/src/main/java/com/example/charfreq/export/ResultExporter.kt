package com.example.charfreq.export

import com.example.charfreq.formatter.ResultFormatter
import com.example.charfreq.model.CharacterFrequencyResult
import com.example.charfreq.unicode.UnicodeUtils
import java.util.Locale

object ResultExporter {

    fun generateCopyText(result: CharacterFrequencyResult, full: Boolean = true): String {
        val sb = StringBuilder()
        sb.append("CharFreq\n\n")
        sb.append("Total analyzed characters: ${result.analyzedCharacters}\n")
        sb.append("Unique characters: ${result.uniqueCharacters}\n\n")
        sb.append("Character Frequency\n\n")

        val list = if (full) result.frequencies else result.frequencies.take(10)
        for (item in list) {
            val name = item.unicodeName ?: UnicodeUtils.getDisplayName(item.rawCharacter)
            sb.append("${item.rank}. ${item.displayCharacter} — $name — ${item.frequency} — ${ResultFormatter.formatPercentage(item.percentage)}\n")
        }
        return sb.toString().trimEnd()
    }

    fun generateShareText(result: CharacterFrequencyResult): String {
        val sb = StringBuilder()
        sb.append("CharFreq\n\n")
        sb.append("Character statistics:\n")
        sb.append("Total analyzed: ${result.analyzedCharacters}\n")
        sb.append("Unique: ${result.uniqueCharacters}\n\n")

        sb.append("Letters: ${result.letterCount}\n")
        sb.append("Digits: ${result.digitCount}\n")
        sb.append("Whitespace: ${result.whitespaceCount}\n")
        sb.append("Punctuation: ${result.punctuationCount}\n")
        sb.append("Symbols: ${result.symbolCount}\n\n")

        if (result.mostFrequent.isNotEmpty()) {
            val most = result.mostFrequent.joinToString(", ") {
                "${UnicodeUtils.getDisplayName(it.rawCharacter)} — ${it.frequency}"
            }
            sb.append("Most frequent:\n$most\n\n")
        }

        sb.append("Top characters:\n\n")
        val top = result.frequencies.take(5)
        for (item in top) {
            val name = UnicodeUtils.getDisplayName(item.rawCharacter)
            sb.append("${item.rank}. $name — ${item.frequency} — ${ResultFormatter.formatPercentage(item.percentage)}\n")
        }

        return sb.toString().trimEnd()
    }

    fun generateCsv(result: CharacterFrequencyResult): String {
        val sb = StringBuilder()
        // Header
        sb.append("Rank,Character,Display,Frequency,Percentage,Category,Unicode,Unicode Name\n")

        for (item in result.frequencies) {
            val rankStr = item.rank.toString()
            val rawCharEscaped = escapeCsvField(item.rawCharacter)
            val displayEscaped = escapeCsvField(item.displayCharacter)
            val freqStr = item.frequency.toString()
            val pctStr = String.format(Locale.US, "%.2f", item.percentage)
            val catEscaped = escapeCsvField(item.category.displayName)
            val unicodeHexEscaped = escapeCsvField(item.codePointsHex)
            val unicodeNameEscaped = escapeCsvField(item.unicodeName ?: UnicodeUtils.getDisplayName(item.rawCharacter))

            sb.append("$rankStr,$rawCharEscaped,$displayEscaped,$freqStr,$pctStr,$catEscaped,$unicodeHexEscaped,$unicodeNameEscaped\n")
        }

        return sb.toString()
    }

    private fun escapeCsvField(field: String): String {
        // Enclose in quotes and double internal quotes
        val escaped = field.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
