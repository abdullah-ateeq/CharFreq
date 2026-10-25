# CharFreq 📊🔤

**CharFreq** is a modern, feature-rich Android text analysis and character frequency application built with **Jetpack Compose** and **Material 3**. It provides deep insights into text composition, Unicode properties, character distributions, word counts, and grapheme clusters.

---

## ✨ Key Features

### 📈 Comprehensive Text Statistics
- **Counts:** Characters (with/without spaces), words, sentences, paragraphs, lines.
- **Advanced Metrics:** UTF-16 code units, Unicode code points, grapheme clusters (fully emoji-safe), byte size (UTF-8 / UTF-16), and estimated reading time.

### 🔍 Character Frequency Analysis
- **Frequency Distribution:** Detailed frequency breakdown for every character/grapheme in the text.
- **Categorization:** Automatic classification into Letters, Digits, Punctuation, Symbols, Whitespace, and Combining Marks.
- **Filtering & Customization:** Filter by category (show/hide punctuation, digits, whitespace, symbols, etc.) and case sensitivity.
- **Sorting Options:** Sort by frequency (ascending/descending), alphabetical order, or character category.

### 🌐 Unicode & Grapheme Precision
- Built on robust Unicode segmentation standards ensuring multi-byte characters and complex emojis are treated as single logical units.
- **Normalization Modes:** Support for NFC, NFD, NFKC, and NFKD Unicode normalization forms.

### 💾 History & Export
- **Session History:** Automatically save and review past text analyses.
- **Export Formats:** Export analysis results and statistics to **JSON**, **CSV**, or plain **TXT**, and share directly to other apps.

### 🎨 Modern UI & Material 3
- **Jetpack Compose:** Fully declarative UI built with Material 3 design guidelines.
- **Dynamic Theming:** Light/Dark theme support with Dynamic Color capabilities.
- **Interactive Visualizations:** Frequency bar charts, summary cards, and expandable character detail sheets.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose, Material 3
- **Architecture:** MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF) using Kotlin Coroutines & `StateFlow`
- **Build System:** Gradle (Kotlin DSL)

---

## 🚀 Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/abdullah-ateeq/CharFreq.git
   ```
2. **Open in Android Studio:**
   - Open Android Studio and select **Open**, then choose the `CharFreq` folder.
3. **Build & Run:**
   - Sync Gradle and run the app on an Android emulator or physical device (requires minSdk 26+).

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
