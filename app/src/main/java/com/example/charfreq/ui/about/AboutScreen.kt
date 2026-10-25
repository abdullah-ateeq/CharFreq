package com.example.charfreq.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CharFreq",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Show frequency of every character in text.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "An offline utility for analyzing how frequently characters appear in text.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Offline & Privacy Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SignalWifiOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "100% Offline & Private",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "All character analysis happens on your device. Your text is never uploaded, tracked, or shared. Fully functional in Airplane Mode.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Features Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "KEY FEATURES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FeatureBullet("Character frequency analysis & percentages")
                        FeatureBullet("Unicode-aware Grapheme cluster segmentation")
                        FeatureBullet("Unicode code-point & UTF-16 code unit modes")
                        FeatureBullet("Emoji sequences, ZWJ, skin tones, & flags support")
                        FeatureBullet("Combining characters & Unicode normalization (NFC, NFD, NFKC, NFKD)")
                        FeatureBullet("Whitespace analysis (space, tabs, newlines, CRLF)")
                        FeatureBullet("Case handling (Unicode-aware case folding)")
                        FeatureBullet("Flexible inclusion filters (punctuation, digits, symbols)")
                        FeatureBullet("Multi-criteria sorting (frequency, character, code point, category)")
                        FeatureBullet("Instant character search & Unicode code point lookup")
                        FeatureBullet("Visual horizontal frequency bar chart")
                        FeatureBullet("Copy, share sheet, and Unicode-safe CSV export")
                        FeatureBullet("Local history of up to 10 saved analyses")
                    }
                }
            }

            // Definitions Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DEFINITIONS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        DefinitionItem("Character", "A unit displayed or interpreted according to the selected counting mode.")
                        DefinitionItem("Grapheme Cluster", "A user-perceived character that may contain multiple Unicode code points (e.g., emojis or accented letters).")
                        DefinitionItem("Code Point", "A Unicode value representing a character or part of a character sequence (e.g., U+0041).")
                        DefinitionItem("UTF-16 Code Unit", "The 16-bit storage unit used by Java/Kotlin strings.")
                        DefinitionItem("Frequency", "The number of times a character occurs in the text.")
                        DefinitionItem("Percentage", "A character's frequency divided by the total analyzed characters.")
                        DefinitionItem("Unicode Category", "A classification such as Letter, Number, Punctuation, Symbol, or Separator.")
                        DefinitionItem("Normalization", "A Unicode transformation used to represent equivalent text consistently.")
                    }
                }
            }

            // Version info
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CharFreq v1.0 • Minimum SDK 26",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureBullet(text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun DefinitionItem(term: String, definition: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = term,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = definition,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}
