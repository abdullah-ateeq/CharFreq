package com.example.charfreq.grapheme

import com.example.charfreq.model.CountingUnit
import java.text.BreakIterator
import java.util.regex.Pattern

object GraphemeSegmenter {

    private val GRAPHEME_PATTERN = Pattern.compile("\\X")

    /**
     * Segments text according to the specified CountingUnit.
     */
    fun segment(text: String, unit: CountingUnit): List<String> {
        if (text.isEmpty()) return emptyList()

        return when (unit) {
            CountingUnit.UTF16_CODE_UNIT -> segmentUtf16(text)
            CountingUnit.CODE_POINT -> segmentCodePoints(text)
            CountingUnit.GRAPHEME -> segmentGraphemes(text)
        }
    }

    private fun segmentUtf16(text: String): List<String> {
        val list = ArrayList<String>(text.length)
        for (i in 0 until text.length) {
            list.add(text[i].toString())
        }
        return list
    }

    private fun segmentCodePoints(text: String): List<String> {
        val list = ArrayList<String>()
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            list.add(String(Character.toChars(cp)))
            i += Character.charCount(cp)
        }
        return list
    }

    private fun segmentGraphemes(text: String): List<String> {
        val rawClusters = ArrayList<String>()

        try {
            val matcher = GRAPHEME_PATTERN.matcher(text)
            while (matcher.find()) {
                rawClusters.add(matcher.group())
            }
        } catch (_: Exception) {
            // Fallback to BreakIterator
            val bi = BreakIterator.getCharacterInstance()
            bi.setText(text)
            var start = bi.first()
            var end = bi.next()
            while (end != BreakIterator.DONE) {
                rawClusters.add(text.substring(start, end))
                start = end
                end = bi.next()
            }
        }

        if (rawClusters.isEmpty()) {
            return segmentCodePoints(text)
        }

        // Post-process to ensure ZWJ sequences, CRLF, and combining marks are fully glued
        val merged = ArrayList<String>(rawClusters.size)
        for (cluster in rawClusters) {
            if (merged.isNotEmpty()) {
                val last = merged[merged.size - 1]

                // CRLF check
                if (last == "\r" && cluster == "\n") {
                    merged[merged.size - 1] = "\r\n"
                    continue
                }

                // ZWJ glue
                val lastEndsWithZwj = last.endsWith("\u200D")
                val clusterStartsWithZwj = cluster.startsWith("\u200D")
                val isVariationSelector = cluster == "\uFE0F" || cluster == "\uFE0E"
                val isIsolatedSkinToneModifier = cluster.codePoints().count() == 1L && cluster.codePoints().allMatch { it in 0x1F3FB..0x1F3FF }

                if (lastEndsWithZwj || clusterStartsWithZwj || isVariationSelector || isIsolatedSkinToneModifier) {
                    merged[merged.size - 1] = last + cluster
                    continue
                }
            }
            merged.add(cluster)
        }

        return merged
    }
}
