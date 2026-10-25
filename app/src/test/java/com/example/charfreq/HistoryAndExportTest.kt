package com.example.charfreq

import com.example.charfreq.analyzer.CharacterProcessor
import com.example.charfreq.export.ResultExporter
import com.example.charfreq.model.CharFreqHistory
import com.example.charfreq.model.CharFreqSettings
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class HistoryAndExportTest {

    @Test
    fun testHistoryLimitAndOldestRemoval() {
        val maxHistory = 10
        val historyList = mutableListOf<CharFreqHistory>()

        // Simulate saving 10 distinct entries
        for (i in 1..10) {
            val entry = CharFreqHistory(
                id = UUID.randomUUID().toString(),
                text = "Text #$i",
                analyzedAt = System.currentTimeMillis() + i * 1000,
                countingUnit = "GRAPHEME",
                caseSensitive = false,
                normalization = "NONE",
                whitespaceMode = "INCLUDE_ALL",
                includePunctuation = true,
                includeDigits = true,
                includeSymbols = true
            )
            historyList.add(0, entry)
        }

        assertEquals(10, historyList.size)
        assertEquals("Text #10", historyList.first().text)
        assertEquals("Text #1", historyList.last().text)

        // Add 11th entry
        val eleventh = CharFreqHistory(
            id = UUID.randomUUID().toString(),
            text = "Text #11",
            analyzedAt = System.currentTimeMillis() + 11_000,
            countingUnit = "GRAPHEME",
            caseSensitive = false,
            normalization = "NONE",
            whitespaceMode = "INCLUDE_ALL",
            includePunctuation = true,
            includeDigits = true,
            includeSymbols = true
        )
        historyList.add(0, eleventh)
        val trimmed = if (historyList.size > maxHistory) historyList.subList(0, maxHistory) else historyList

        assertEquals(10, trimmed.size)
        assertEquals("Text #11", trimmed.first().text)
        // Oldest ("Text #1") must have been removed
        assertFalse(trimmed.any { it.text == "Text #1" })
        assertEquals("Text #2", trimmed.last().text)
    }

    @Test
    fun testHistoryDuplicateAvoidance() {
        val historyList = mutableListOf<CharFreqHistory>()
        val entry1 = CharFreqHistory(
            id = "1",
            text = "Hello world",
            analyzedAt = 1000L,
            countingUnit = "GRAPHEME",
            caseSensitive = false,
            normalization = "NONE",
            whitespaceMode = "INCLUDE_ALL",
            includePunctuation = true,
            includeDigits = true,
            includeSymbols = true
        )
        historyList.add(0, entry1)

        val entry2 = CharFreqHistory(
            id = "2",
            text = "Hello world",
            analyzedAt = 2000L,
            countingUnit = "GRAPHEME",
            caseSensitive = false,
            normalization = "NONE",
            whitespaceMode = "INCLUDE_ALL",
            includePunctuation = true,
            includeDigits = true,
            includeSymbols = true
        )

        // Check duplicate condition
        val top = historyList.first()
        val isDuplicate = top.text == entry2.text &&
                top.countingUnit == entry2.countingUnit &&
                top.caseSensitive == entry2.caseSensitive &&
                top.normalization == entry2.normalization &&
                top.whitespaceMode == entry2.whitespaceMode &&
                top.includePunctuation == entry2.includePunctuation &&
                top.includeDigits == entry2.includeDigits &&
                top.includeSymbols == entry2.includeSymbols

        assertTrue(isDuplicate)
    }

    @Test
    fun testCopyAndShareFormat() {
        val text = "Hello hello!"
        val res = CharacterProcessor.process(text, CharFreqSettings())

        val copyText = ResultExporter.generateCopyText(res, full = true)
        assertTrue(copyText.contains("CharFreq"))
        assertTrue(copyText.contains("Total analyzed characters: 12"))
        assertTrue(copyText.contains("Unique characters: 6"))

        val shareText = ResultExporter.generateShareText(res)
        assertTrue(shareText.contains("CharFreq"))
        assertTrue(shareText.contains("Letters: 10"))
        assertTrue(shareText.contains("Whitespace: 1"))
        assertTrue(shareText.contains("Punctuation: 1"))
    }

    @Test
    fun testJsonSerializationIntegrity() {
        val obj = JSONObject()
        obj.put("id", "test-uuid")
        obj.put("text", "Sample text with emojis 👋 and symbols $")
        obj.put("analyzedAt", 123456789L)
        obj.put("countingUnit", "GRAPHEME")
        obj.put("caseSensitive", true)

        val jsonStr = obj.toString()
        val parsed = JSONObject(jsonStr)

        assertEquals("test-uuid", parsed.getString("id"))
        assertEquals("Sample text with emojis 👋 and symbols $", parsed.getString("text"))
        assertEquals(123456789L, parsed.getLong("analyzedAt"))
        assertEquals("GRAPHEME", parsed.getString("countingUnit"))
        assertTrue(parsed.getBoolean("caseSensitive"))
    }
}
