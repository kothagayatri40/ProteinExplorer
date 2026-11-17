package com.example.proteinexplorer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

val aaFullNames = mapOf(
    'A' to "Alanine", 'C' to "Cysteine", 'D' to "Aspartic Acid", 'E' to "Glutamic Acid",
    'F' to "Phenylalanine", 'G' to "Glycine", 'H' to "Histidine", 'I' to "Isoleucine",
    'K' to "Lysine", 'L' to "Leucine", 'M' to "Methionine", 'N' to "Asparagine",
    'P' to "Proline", 'Q' to "Glutamine", 'R' to "Arginine", 'S' to "Serine",
    'T' to "Threonine", 'V' to "Valine", 'W' to "Tryptophan", 'Y' to "Tyrosine"
)

@Composable
fun AminoAcidProgressBars(
    counts: Map<Char, Int>,
    percentages: Map<Char, Double>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Amino Acid Composition",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            val maxPct = percentages.values.maxOrNull()?.coerceAtLeast(1.0) ?: 100.0

            percentages.entries.sortedByDescending { it.value }.forEach { (aa, pct) ->
                val count = counts[aa] ?: 0
                val fullName = aaFullNames[aa] ?: ""

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$aa",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp)
                    )

                    Text(
                        text = "($fullName)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(100.dp)
                    )

                    LinearProgressIndicator(
                        progress = (pct / maxPct).toFloat().coerceIn(0f, 1f),
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.secondaryContainer
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = String.format(Locale.US, "%d (%.1f%%)", count, pct),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(85.dp)
                    )
                }
            }
        }
    }
}
