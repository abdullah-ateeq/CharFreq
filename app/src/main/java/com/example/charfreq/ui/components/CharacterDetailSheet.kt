package com.example.charfreq.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.charfreq.formatter.ResultFormatter
import com.example.charfreq.model.CharacterFrequency
import com.example.charfreq.unicode.UnicodeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailSheet(
    item: CharacterFrequency,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CHARACTER DETAILS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Large character glyph card
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.displayCharacter,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name
            val displayName = item.unicodeName ?: UnicodeUtils.getDisplayName(item.rawCharacter)
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatPill(label = "Frequency", value = item.frequency.toString())
                StatPill(label = "Percentage", value = ResultFormatter.formatPercentage(item.percentage))
                StatPill(
                    label = "Category",
                    value = "${item.category.displayName} (${UnicodeUtils.getCategoryAbbreviation(item.rawCharacter)})"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Unicode details table
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(
                        label = "Safe Representation",
                        value = item.displayCharacter
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(
                        label = "Unicode Code Points",
                        value = item.codePointsHex,
                        isMono = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(
                        label = "Raw Length (UTF-16)",
                        value = "${item.rawCharacter.length} unit(s)"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(
                        label = "Code Point Count",
                        value = "${item.unicodeCodePoints.size}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val clip = ClipData.newPlainText("Character", item.rawCharacter)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Character copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy character",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Character")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val textToCopy = buildString {
                            appendLine("Character: ${item.displayCharacter}")
                            appendLine("Name: $displayName")
                            appendLine("Frequency: ${item.frequency} (${ResultFormatter.formatPercentage(item.percentage)})")
                            appendLine("Category: ${item.category.displayName} (${UnicodeUtils.getCategoryAbbreviation(item.rawCharacter)})")
                            appendLine("Code Points: ${item.codePointsHex}")
                        }
                        val clip = ClipData.newPlainText("Character Details", textToCopy)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Details copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Copy Details")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, isMono: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
