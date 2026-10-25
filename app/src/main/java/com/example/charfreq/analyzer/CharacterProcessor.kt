package com.example.charfreq.analyzer

import com.example.charfreq.grapheme.GraphemeSegmenter
import com.example.charfreq.model.CharacterCategory
import com.example.charfreq.model.CharacterFrequency
import com.example.charfreq.model.CharacterFrequencyResult
import com.example.charfreq.model.CharFreqSettings
import com.example.charfreq.model.NormalizationMode
import com.example.charfreq.model.SortDirection
import com.example.charfreq.model.SortMode
import com.example.charfreq.model.WhitespaceMode
import com.example.charfreq.unicode.UnicodeUtils
import java.text.Normalizer
import java.util.Locale

object CharacterProcessor {

    fun process(
        text: String,
        settings: CharFreqSettings
    ): CharacterFrequencyResult {
        if (text.isEmpty()) {
            return CharacterFrequencyResult()
        }

        // 1. Raw text statistics
        val totalRawUnits = text.length
        val totalCodePoints = text.codePoints().count().toInt()
        val totalGraphemes = GraphemeSegmenter.segment(text, com.example.charfreq.model.CountingUnit.GRAPHEME).size

        // 2. Unicode Normalization (applied to analysis representation only)
        val normalizedText = when (settings.normalization) {
            NormalizationMode.NONE -> text
            NormalizationMode.NFC -> Normalizer.normalize(text, Normalizer.Form.NFC)
            NormalizationMode.NFD -> Normalizer.normalize(text, Normalizer.Form.NFD)
            NormalizationMode.NFKC -> Normalizer.normalize(text, Normalizer.Form.NFKC)
            NormalizationMode.NFKD -> Normalizer.normalize(text, Normalizer.Form.NFKD)
        }

        // 3. Case Handling (Unicode-aware lowercase)
        val workingText = if (!settings.caseSensitive) {
            normalizedText.lowercase(Locale.ROOT)
        } else {
            normalizedText
        }

        // 4. Segmentation by Counting Unit
        val tokens = GraphemeSegmenter.segment(workingText, settings.countingUnit)
        val totalTokensCount = tokens.size

        // 5. Categorize and Filter
        val includedTokens = ArrayList<String>(tokens.size)
        val previewItems = if (settings.showProcessingPreview) {
            tokens.take(100).map { Pair(it, UnicodeUtils.getSafeDisplay(it)) }
        } else {
            emptyList()
        }

        for (token in tokens) {
            val cat = UnicodeUtils.getCategory(token)

            // Whitespace filtering
            val passWhitespace = when (settings.whitespaceMode) {
                WhitespaceMode.INCLUDE_ALL -> true
                WhitespaceMode.EXCLUDE_ALL -> cat != CharacterCategory.WHITESPACE
                WhitespaceMode.SPACES_ONLY -> {
                    if (cat == CharacterCategory.WHITESPACE) {
                        UnicodeUtils.isSpaceOnly(token)
                    } else {
                        true
                    }
                }
            }
            if (!passWhitespace) continue

            // Punctuation filtering
            if (!settings.includePunctuation && cat == CharacterCategory.PUNCTUATION) continue

            // Digit filtering
            if (!settings.includeDigits && cat == CharacterCategory.DIGIT) continue

            // Symbol filtering
            if (!settings.includeSymbols && cat == CharacterCategory.SYMBOL) continue

            // Letter filtering
            if (!settings.includeLetters && cat == CharacterCategory.LETTER) continue

            // Combining mark filtering (applicable in code point / utf16 mode or unattached marks)
            if (!settings.includeMarks && cat == CharacterCategory.MARK) continue

            includedTokens.add(token)
        }

        val analyzedCharacters = includedTokens.size
        val excludedCharacters = totalTokensCount - analyzedCharacters

        if (analyzedCharacters == 0) {
            return CharacterFrequencyResult(
                totalRawUnits = totalRawUnits,
                totalCodePoints = totalCodePoints,
                totalGraphemes = totalGraphemes,
                analyzedCharacters = 0,
                excludedCharacters = excludedCharacters,
                previewItems = previewItems
            )
        }

        // 6. Frequency Counting
        val freqMap = LinkedHashMap<String, Int>()
        for (token in includedTokens) {
            freqMap[token] = (freqMap[token] ?: 0) + 1
        }

        val uniqueCharacters = freqMap.size

        // 7. Aggregate Detailed Category & ASCII counts
        var letterCount = 0
        var digitCount = 0
        var whitespaceCount = 0
        var punctuationCount = 0
        var symbolCount = 0
        var markCount = 0
        var controlCount = 0
        var otherCount = 0
        var uppercaseLetterCount = 0
        var lowercaseLetterCount = 0
        var asciiCount = 0
        var nonAsciiCount = 0
        var printableAsciiCount = 0
        var asciiControlCount = 0

        for ((token, count) in freqMap) {
            val cat = UnicodeUtils.getCategory(token)
            when (cat) {
                CharacterCategory.LETTER -> letterCount += count
                CharacterCategory.DIGIT -> digitCount += count
                CharacterCategory.WHITESPACE -> whitespaceCount += count
                CharacterCategory.PUNCTUATION -> punctuationCount += count
                CharacterCategory.SYMBOL -> symbolCount += count
                CharacterCategory.MARK -> markCount += count
                CharacterCategory.CONTROL -> controlCount += count
                CharacterCategory.OTHER -> otherCount += count
            }

            // Case metrics
            val codePoints = UnicodeUtils.getCodePoints(token)
            for (cp in codePoints) {
                if (Character.isUpperCase(cp)) uppercaseLetterCount += count
                if (Character.isLowerCase(cp)) lowercaseLetterCount += count
            }

            // ASCII metrics
            if (UnicodeUtils.isAscii(token)) {
                asciiCount += count
                if (UnicodeUtils.isPrintableAscii(token)) {
                    printableAsciiCount += count
                }
                if (UnicodeUtils.isAsciiControl(token)) {
                    asciiControlCount += count
                }
            } else {
                nonAsciiCount += count
            }
        }

        // 8. Build List with Percentages
        val frequencyList = ArrayList<CharacterFrequency>(freqMap.size)
        for ((token, freq) in freqMap) {
            val percentage = (freq.toDouble() / analyzedCharacters.toDouble()) * 100.0
            val cat = UnicodeUtils.getCategory(token)
            val cps = UnicodeUtils.getCodePoints(token)
            val name = UnicodeUtils.getUnicodeName(token)
            val safeDisp = UnicodeUtils.getSafeDisplay(token)

            frequencyList.add(
                CharacterFrequency(
                    displayCharacter = safeDisp,
                    rawCharacter = token,
                    frequency = freq,
                    percentage = percentage,
                    category = cat,
                    unicodeCodePoints = cps,
                    unicodeName = name,
                    rank = 0 // Will assign after sort
                )
            )
        }

        // 9. Sorting
        val sortedList = sortFrequencies(frequencyList, settings.sortMode, settings.sortDirection)

        // Assign ranks (1-based)
        val rankedList = sortedList.mapIndexed { index, item ->
            item.copy(rank = index + 1)
        }

        // 10. Most & Least Frequent
        val maxFreq = rankedList.maxOfOrNull { it.frequency } ?: 0
        val mostFrequent = if (maxFreq > 0) {
            rankedList.filter { it.frequency == maxFreq }
        } else {
            emptyList()
        }

        val leastFrequentCount = rankedList.count { it.frequency == 1 }

        // 11. Frequency Distribution
        val distribution = linkedMapOf(
            "1 occurrence" to rankedList.count { it.frequency == 1 },
            "2 occurrences" to rankedList.count { it.frequency == 2 },
            "3 occurrences" to rankedList.count { it.frequency == 3 },
            "4 occurrences" to rankedList.count { it.frequency == 4 },
            "5+ occurrences" to rankedList.count { it.frequency >= 5 }
        )

        return CharacterFrequencyResult(
            frequencies = rankedList,
            totalRawUnits = totalRawUnits,
            totalCodePoints = totalCodePoints,
            totalGraphemes = totalGraphemes,
            analyzedCharacters = analyzedCharacters,
            excludedCharacters = excludedCharacters,
            uniqueCharacters = uniqueCharacters,
            letterCount = letterCount,
            digitCount = digitCount,
            whitespaceCount = whitespaceCount,
            punctuationCount = punctuationCount,
            symbolCount = symbolCount,
            markCount = markCount,
            controlCount = controlCount,
            otherCount = otherCount,
            uppercaseLetterCount = uppercaseLetterCount,
            lowercaseLetterCount = lowercaseLetterCount,
            asciiCount = asciiCount,
            nonAsciiCount = nonAsciiCount,
            printableAsciiCount = printableAsciiCount,
            asciiControlCount = asciiControlCount,
            mostFrequent = mostFrequent,
            leastFrequentCount = leastFrequentCount,
            frequencyDistribution = distribution,
            previewItems = previewItems
        )
    }

    private fun sortFrequencies(
        list: List<CharacterFrequency>,
        mode: SortMode,
        direction: SortDirection
    ): List<CharacterFrequency> {
        val comparator = when (mode) {
            SortMode.FREQUENCY -> {
                Comparator<CharacterFrequency> { a, b ->
                    val primary = if (direction == SortDirection.DESCENDING) {
                        b.frequency.compareTo(a.frequency)
                    } else {
                        a.frequency.compareTo(b.frequency)
                    }
                    if (primary != 0) primary else a.rawCharacter.compareTo(b.rawCharacter)
                }
            }
            SortMode.PERCENTAGE -> {
                Comparator<CharacterFrequency> { a, b ->
                    val primary = if (direction == SortDirection.DESCENDING) {
                        b.percentage.compareTo(a.percentage)
                    } else {
                        a.percentage.compareTo(b.percentage)
                    }
                    if (primary != 0) primary else a.rawCharacter.compareTo(b.rawCharacter)
                }
            }
            SortMode.CHARACTER -> {
                Comparator<CharacterFrequency> { a, b ->
                    val primary = if (direction == SortDirection.ASCENDING) {
                        a.rawCharacter.compareTo(b.rawCharacter)
                    } else {
                        b.rawCharacter.compareTo(a.rawCharacter)
                    }
                    primary
                }
            }
            SortMode.CODE_POINT -> {
                Comparator<CharacterFrequency> { a, b ->
                    val cpA = a.unicodeCodePoints.firstOrNull() ?: 0
                    val cpB = b.unicodeCodePoints.firstOrNull() ?: 0
                    val primary = if (direction == SortDirection.ASCENDING) {
                        cpA.compareTo(cpB)
                    } else {
                        cpB.compareTo(cpA)
                    }
                    if (primary != 0) primary else a.rawCharacter.compareTo(b.rawCharacter)
                }
            }
            SortMode.CATEGORY -> {
                Comparator<CharacterFrequency> { a, b ->
                    val catOrderA = a.category.sortOrder
                    val catOrderB = b.category.sortOrder
                    val primary = if (direction == SortDirection.ASCENDING) {
                        catOrderA.compareTo(catOrderB)
                    } else {
                        catOrderB.compareTo(catOrderA)
                    }
                    if (primary != 0) {
                        primary
                    } else {
                        // Secondary sort inside category by frequency descending
                        val sec = b.frequency.compareTo(a.frequency)
                        if (sec != 0) sec else a.rawCharacter.compareTo(b.rawCharacter)
                    }
                }
            }
        }

        return list.sortedWith(comparator)
    }
}
