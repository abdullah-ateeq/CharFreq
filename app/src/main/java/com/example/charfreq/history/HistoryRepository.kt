package com.example.charfreq.history

import android.content.Context
import android.content.SharedPreferences
import com.example.charfreq.model.CharFreqHistory
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class HistoryRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("charfreq_history_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HISTORY = "saved_history"
        private const val MAX_HISTORY = 10
    }

    fun getHistory(): List<CharFreqHistory> {
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<CharFreqHistory>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CharFreqHistory(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        text = obj.optString("text", ""),
                        analyzedAt = obj.optLong("analyzedAt", System.currentTimeMillis()),
                        countingUnit = obj.optString("countingUnit", "GRAPHEME"),
                        caseSensitive = obj.optBoolean("caseSensitive", false),
                        normalization = obj.optString("normalization", "NONE"),
                        whitespaceMode = obj.optString("whitespaceMode", "INCLUDE_ALL"),
                        includePunctuation = obj.optBoolean("includePunctuation", true),
                        includeDigits = obj.optBoolean("includeDigits", true),
                        includeSymbols = obj.optBoolean("includeSymbols", true),
                        totalCharacters = obj.optInt("totalCharacters", 0),
                        uniqueCharacters = obj.optInt("uniqueCharacters", 0),
                        mostFrequentSummary = obj.optString("mostFrequentSummary", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    /**
     * Saves a new entry.
     * Prevents consecutive duplicates with identical text and configuration.
     * Keeps maximum 10 entries (removes oldest when 11th is added).
     */
    fun saveEntry(entry: CharFreqHistory): Boolean {
        val currentList = getHistory().toMutableList()

        // Duplicate check against top entry
        if (currentList.isNotEmpty()) {
            val top = currentList.first()
            if (top.text == entry.text &&
                top.countingUnit == entry.countingUnit &&
                top.caseSensitive == entry.caseSensitive &&
                top.normalization == entry.normalization &&
                top.whitespaceMode == entry.whitespaceMode &&
                top.includePunctuation == entry.includePunctuation &&
                top.includeDigits == entry.includeDigits &&
                top.includeSymbols == entry.includeSymbols
            ) {
                return false // Duplicate ignored
            }
        }

        // Add to front
        currentList.add(0, entry)

        // Trim to max 10
        val trimmed = if (currentList.size > MAX_HISTORY) {
            currentList.subList(0, MAX_HISTORY)
        } else {
            currentList
        }

        saveList(trimmed)
        return true
    }

    fun deleteEntry(id: String) {
        val currentList = getHistory().filterNot { it.id == id }
        saveList(currentList)
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveList(list: List<CharFreqHistory>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("text", item.text)
            obj.put("analyzedAt", item.analyzedAt)
            obj.put("countingUnit", item.countingUnit)
            obj.put("caseSensitive", item.caseSensitive)
            obj.put("normalization", item.normalization)
            obj.put("whitespaceMode", item.whitespaceMode)
            obj.put("includePunctuation", item.includePunctuation)
            obj.put("includeDigits", item.includeDigits)
            obj.put("includeSymbols", item.includeSymbols)
            obj.put("totalCharacters", item.totalCharacters)
            obj.put("uniqueCharacters", item.uniqueCharacters)
            obj.put("mostFrequentSummary", item.mostFrequentSummary)
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }
}
