package com.example.charfreq

import com.example.charfreq.analyzer.CharacterProcessor
import com.example.charfreq.export.ResultExporter
import com.example.charfreq.model.*
import org.junit.Assert.*
import org.junit.Test

class CharFreqProcessorTest {

    private val defaultSettings = CharFreqSettings()

    @Test
    fun testBasicFrequency() {
        val result = CharacterProcessor.process("aabbc", defaultSettings)
        assertEquals(5, result.analyzedCharacters)
        assertEquals(3, result.uniqueCharacters)

        val map = result.frequencies.associate { it.rawCharacter to it.frequency }
        assertEquals(2, map["a"])
        assertEquals(2, map["b"])
        assertEquals(1, map["c"])
    }

    @Test
    fun testCaseInsensitive() {
        val settings = defaultSettings.copy(caseSensitive = false)
        val result = CharacterProcessor.process("AaAaB", settings)
        assertEquals(5, result.analyzedCharacters)
        assertEquals(2, result.uniqueCharacters)

        val map = result.frequencies.associate { it.rawCharacter to it.frequency }
        assertEquals(4, map["a"])
        assertEquals(1, map["b"])
    }

    @Test
    fun testCaseSensitive() {
        val settings = defaultSettings.copy(caseSensitive = true)
        val result = CharacterProcessor.process("AaAaB", settings)
        assertEquals(5, result.analyzedCharacters)
        assertEquals(3, result.uniqueCharacters)

        val map = result.frequencies.associate { it.rawCharacter to it.frequency }
        assertEquals(2, map["A"])
        assertEquals(2, map["a"])
        assertEquals(1, map["B"])
    }

    @Test
    fun testSpace() {
        val result = CharacterProcessor.process("a b c", defaultSettings)
        val map = result.frequencies.associate { it.rawCharacter to it.frequency }
        assertEquals(2, map[" "])
        assertEquals(1, map["a"])
        assertEquals(1, map["b"])
        assertEquals(1, map["c"])

        val spaceItem = result.frequencies.first { it.rawCharacter == " " }
        assertEquals("␠", spaceItem.displayCharacter)
        assertEquals(CharacterCategory.WHITESPACE, spaceItem.category)
    }

    @Test
    fun testNewline() {
        val result = CharacterProcessor.process("a\nb", defaultSettings)
        val map = result.frequencies.associate { it.rawCharacter to it.frequency }
        assertEquals(1, map["a"])
        assertEquals(1, map["b"])
        assertEquals(1, map["\n"])

        val newlineItem = result.frequencies.first { it.rawCharacter == "\n" }
        assertEquals("↵", newlineItem.displayCharacter)
        assertEquals(CharacterCategory.WHITESPACE, newlineItem.category)
    }

    @Test
    fun testTab() {
        val result = CharacterProcessor.process("a\tb", defaultSettings)
        val map = result.frequencies.associate { it.rawCharacter to it.frequency }
        assertEquals(1, map["a"])
        assertEquals(1, map["b"])
        assertEquals(1, map["\t"])

        val tabItem = result.frequencies.first { it.rawCharacter == "\t" }
        assertEquals("⇥", tabItem.displayCharacter)
        assertEquals(CharacterCategory.WHITESPACE, tabItem.category)
    }

    @Test
    fun testCrlfOneNewline() {
        // CRLF in Grapheme mode should be analyzed as ONE newline character
        val result = CharacterProcessor.process("a\r\nb", defaultSettings)
        assertEquals(3, result.analyzedCharacters)

        val map = result.frequencies.associate { it.rawCharacter to it.frequency }
        assertEquals(1, map["a"])
        assertEquals(1, map["b"])
        assertEquals(1, map["\r\n"])

        val crlfItem = result.frequencies.first { it.rawCharacter == "\r\n" }
        assertEquals("↵", crlfItem.displayCharacter)
    }

    @Test
    fun testPunctuation() {
        val result = CharacterProcessor.process("Hello!!!...", defaultSettings)
        val map = result.frequencies.associate { it.rawCharacter to it.frequency }
        assertEquals(3, map["!"])
        assertEquals(3, map["."])
    }

    @Test
    fun testDigits() {
        val result = CharacterProcessor.process("112233", defaultSettings)
        val map = result.frequencies.associate { it.rawCharacter to it.frequency }
        assertEquals(2, map["1"])
        assertEquals(2, map["2"])
        assertEquals(2, map["3"])
    }

    @Test
    fun testUnicodeDigits() {
        val result = CharacterProcessor.process("١١٢", defaultSettings)
        val map = result.frequencies.associate { it.rawCharacter to it.frequency }
        assertEquals(2, map["١"])
        assertEquals(1, map["٢"])
    }

    @Test
    fun testEmoji() {
        val result = CharacterProcessor.process("😀😀😂", defaultSettings)
        val map = result.frequencies.associate { it.rawCharacter to it.frequency }
        assertEquals(2, map["😀"])
        assertEquals(1, map["😂"])
    }

    @Test
    fun testHeartEmoji() {
        // Red heart often has variation selector \uFE0F
        val heart = "❤️"
        val result = CharacterProcessor.process("❤️❤️", defaultSettings)
        assertEquals(2, result.analyzedCharacters)
        assertEquals(1, result.uniqueCharacters)
        assertEquals(2, result.frequencies.first().frequency)
    }

    @Test
    fun testZwjEmoji() {
        val technologist = "👨\u200D💻"
        val result = CharacterProcessor.process("$technologist$technologist", defaultSettings)
        assertEquals(2, result.analyzedCharacters)
        assertEquals(1, result.uniqueCharacters)
        assertEquals(2, result.frequencies.first().frequency)
    }

    @Test
    fun testFamilyEmoji() {
        val family = "👨\u200D👩\u200D👧\u200D👦"
        val result = CharacterProcessor.process(family, defaultSettings)
        assertEquals(1, result.analyzedCharacters)
        assertEquals(1, result.uniqueCharacters)
        assertEquals(1, result.frequencies.first().frequency)
    }

    @Test
    fun testSkinTone() {
        val s1 = "👍"
        val s2 = "👍🏽" // skin tone modifier
        val result = CharacterProcessor.process("$s1$s2", defaultSettings)
        assertEquals(2, result.analyzedCharacters)
        assertEquals(2, result.uniqueCharacters)

        val map = result.frequencies.associate { it.rawCharacter to it.frequency }
        assertEquals(1, map[s1])
        assertEquals(1, map[s2])
    }

    @Test
    fun testCombiningMark() {
        val text = "e\u0301" // e + combining acute accent

        // Grapheme mode: treated as 1 perceived character
        val graphemeRes = CharacterProcessor.process(text, defaultSettings.copy(countingUnit = CountingUnit.GRAPHEME))
        assertEquals(1, graphemeRes.analyzedCharacters)

        // Code point mode: treated as 2 code points
        val cpRes = CharacterProcessor.process(text, defaultSettings.copy(countingUnit = CountingUnit.CODE_POINT))
        assertEquals(2, cpRes.analyzedCharacters)
    }

    @Test
    fun testUnicodeNormalization() {
        val decomposed = "e\u0301" // NFD
        val composed = "\u00E9"   // NFC

        // With NONE: decomposed has length 2, composed has length 1 in CodePoint mode
        val resNone1 = CharacterProcessor.process(decomposed, defaultSettings.copy(normalization = NormalizationMode.NONE, countingUnit = CountingUnit.CODE_POINT))
        val resNone2 = CharacterProcessor.process(composed, defaultSettings.copy(normalization = NormalizationMode.NONE, countingUnit = CountingUnit.CODE_POINT))
        assertEquals(2, resNone1.analyzedCharacters)
        assertEquals(1, resNone2.analyzedCharacters)

        // With NFC: both become composed
        val resNfc1 = CharacterProcessor.process(decomposed, defaultSettings.copy(normalization = NormalizationMode.NFC, countingUnit = CountingUnit.CODE_POINT))
        val resNfc2 = CharacterProcessor.process(composed, defaultSettings.copy(normalization = NormalizationMode.NFC, countingUnit = CountingUnit.CODE_POINT))
        assertEquals(1, resNfc1.analyzedCharacters)
        assertEquals(1, resNfc2.analyzedCharacters)
        assertEquals(resNfc1.frequencies.first().rawCharacter, resNfc2.frequencies.first().rawCharacter)
    }

    @Test
    fun testWhitespaceExclusion() {
        val result = CharacterProcessor.process(
            "a b c",
            defaultSettings.copy(whitespaceMode = WhitespaceMode.EXCLUDE_ALL)
        )
        assertEquals(3, result.analyzedCharacters)
        assertEquals(2, result.excludedCharacters)
        assertFalse(result.frequencies.any { it.category == CharacterCategory.WHITESPACE })
    }

    @Test
    fun testPunctuationExclusion() {
        val result = CharacterProcessor.process(
            "hello!!!",
            defaultSettings.copy(includePunctuation = false)
        )
        assertEquals(5, result.analyzedCharacters)
        assertFalse(result.frequencies.any { it.rawCharacter == "!" })
    }

    @Test
    fun testDigitExclusion() {
        val result = CharacterProcessor.process(
            "abc123",
            defaultSettings.copy(includeDigits = false)
        )
        assertEquals(3, result.analyzedCharacters)
        assertFalse(result.frequencies.any { it.category == CharacterCategory.DIGIT })
    }

    @Test
    fun testSymbolExclusion() {
        val result = CharacterProcessor.process(
            "$ + = ©",
            defaultSettings.copy(includeSymbols = false, whitespaceMode = WhitespaceMode.EXCLUDE_ALL)
        )
        assertEquals(0, result.analyzedCharacters)
    }

    @Test
    fun testCategorySort() {
        val text = "a1!$"
        val result = CharacterProcessor.process(
            text,
            defaultSettings.copy(sortMode = SortMode.CATEGORY, sortDirection = SortDirection.ASCENDING)
        )
        val categories = result.frequencies.map { it.category }
        assertEquals(CharacterCategory.LETTER, categories[0])
        assertEquals(CharacterCategory.DIGIT, categories[1])
        assertEquals(CharacterCategory.PUNCTUATION, categories[2])
        assertEquals(CharacterCategory.SYMBOL, categories[3])
    }

    @Test
    fun testFrequencySort() {
        val result = CharacterProcessor.process(
            "aaabbc",
            defaultSettings.copy(sortMode = SortMode.FREQUENCY, sortDirection = SortDirection.DESCENDING)
        )
        assertEquals("a", result.frequencies[0].rawCharacter)
        assertEquals(3, result.frequencies[0].frequency)
        assertEquals("b", result.frequencies[1].rawCharacter)
        assertEquals(2, result.frequencies[1].frequency)
        assertEquals("c", result.frequencies[2].rawCharacter)
        assertEquals(1, result.frequencies[2].frequency)
    }

    @Test
    fun testFrequencyTieDeterministicOrdering() {
        val result = CharacterProcessor.process(
            "cba",
            defaultSettings.copy(sortMode = SortMode.FREQUENCY, sortDirection = SortDirection.DESCENDING)
        )
        // All have frequency 1; secondary sort must be deterministic alphabetical
        assertEquals("a", result.frequencies[0].rawCharacter)
        assertEquals("b", result.frequencies[1].rawCharacter)
        assertEquals("c", result.frequencies[2].rawCharacter)
    }

    @Test
    fun testPercentages() {
        val result = CharacterProcessor.process(
            "AAAB",
            defaultSettings.copy(caseSensitive = true)
        )
        val aItem = result.frequencies.first { it.rawCharacter == "A" }
        val bItem = result.frequencies.first { it.rawCharacter == "B" }
        assertEquals(75.0, aItem.percentage, 0.001)
        assertEquals(25.0, bItem.percentage, 0.001)
    }

    @Test
    fun testTotalsConsistency() {
        val text = "The quick brown fox jumps over the lazy dog! 12345 🦊"
        val result = CharacterProcessor.process(text, defaultSettings)
        val sumFreq = result.frequencies.sumOf { it.frequency }
        assertEquals(result.analyzedCharacters, sumFreq)
    }

    @Test
    fun testUniqueCount() {
        val result = CharacterProcessor.process("AAAAABBBCC", defaultSettings)
        assertEquals(10, result.analyzedCharacters)
        assertEquals(3, result.uniqueCharacters)
    }

    @Test
    fun testSearchFunctionality() {
        val state = CharFreqUiState(
            text = "abcABC123",
            results = CharacterProcessor.process("abcABC123", defaultSettings.copy(caseSensitive = true)).frequencies,
            searchQuery = "U+0061"
        )
        val found = state.displayResults
        assertEquals(1, found.size)
        assertEquals("a", found[0].rawCharacter)
    }

    @Test
    fun testInvisibleSearch() {
        val state = CharFreqUiState(
            text = "a b",
            results = CharacterProcessor.process("a b", defaultSettings).frequencies,
            searchQuery = "space"
        )
        val found = state.displayResults
        assertEquals(1, found.size)
        assertEquals(" ", found[0].rawCharacter)
        assertEquals("␠", found[0].displayCharacter)
    }

    @Test
    fun testResultLimitDoesNotAlterTotals() {
        // String with 20 distinct characters
        val alphabet = "abcdefghijklmnopqrst"
        val state = CharFreqUiState(
            text = alphabet,
            results = CharacterProcessor.process(alphabet, defaultSettings).frequencies,
            analyzedCharacters = 20,
            uniqueCharacters = 20,
            resultLimit = 5
        )
        assertEquals(5, state.displayResults.size)
        assertEquals(20, state.uniqueCharacters)
        assertEquals(20, state.analyzedCharacters)
    }

    @Test
    fun testLargeTextInput() {
        // 100,000 characters
        val sample = "The quick brown fox jumps over 123 lazy dogs! abc " // 50 chars
        val sb = StringBuilder(100_000)
        repeat(2000) {
            sb.append(sample)
        }
        val text = sb.toString()
        assertEquals(100_000, text.length)

        val startTime = System.currentTimeMillis()
        val result = CharacterProcessor.process(text, defaultSettings)
        val elapsed = System.currentTimeMillis() - startTime

        assertEquals(100_000, result.analyzedCharacters)
        assertTrue(result.uniqueCharacters > 0)
        // Processing 100,000 characters should finish reasonably fast
        assertTrue("Processing took ${elapsed}ms", elapsed < 5000)
    }

    @Test
    fun testCsvExportSafety() {
        val text = "a,b\n\"c\""
        val result = CharacterProcessor.process(text, defaultSettings)
        val csv = ResultExporter.generateCsv(result)

        assertTrue(csv.contains("Rank,Character,Display,Frequency,Percentage,Category,Unicode,Unicode Name"))
        assertTrue(csv.contains("\"\"\"\"")) // Escaped quotes
        assertTrue(csv.contains("\",\"")) // Comma escaped in quotes
    }
}
