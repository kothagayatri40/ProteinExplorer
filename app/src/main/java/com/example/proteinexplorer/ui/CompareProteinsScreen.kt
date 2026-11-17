package com.example.proteinexplorer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proteinexplorer.analysis.AlignmentResult
import com.example.proteinexplorer.analysis.PairwiseAligner
import com.example.proteinexplorer.analysis.ScoringMatrices
import com.example.proteinexplorer.data.ProteinEntity
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareProteinsScreen(
    savedProteins: List<ProteinEntity>,
    onBackClick: () -> Unit
) {
    var selectedProtein1 by remember { mutableStateOf<ProteinEntity?>(savedProteins.firstOrNull()) }
    var selectedProtein2 by remember { mutableStateOf<ProteinEntity?>(savedProteins.getOrNull(1) ?: savedProteins.firstOrNull()) }

    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }

    var alignmentResult by remember { mutableStateOf<AlignmentResult?>(null) }
    var alignmentErrorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pairwise Sequence Alignment",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "GLOBAL Alignment (BLOSUM62)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (savedProteins.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "No saved proteins available in database. Please fetch and save at least two proteins first.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // Protein 1 Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded1,
                    onExpandedChange = { expanded1 = !expanded1 }
                ) {
                    OutlinedTextField(
                        value = selectedProtein1?.let { "${it.accession} - ${it.proteinName}" } ?: "Select Protein 1",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select First Protein") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded1,
                        onDismissRequest = { expanded1 = false }
                    ) {
                        savedProteins.forEach { protein ->
                            DropdownMenuItem(
                                text = { Text("${protein.accession} - ${protein.proteinName} (${protein.organism})") },
                                onClick = {
                                    selectedProtein1 = protein
                                    expanded1 = false
                                }
                            )
                        }
                    }
                }

                // Protein 2 Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded2,
                    onExpandedChange = { expanded2 = !expanded2 }
                ) {
                    OutlinedTextField(
                        value = selectedProtein2?.let { "${it.accession} - ${it.proteinName}" } ?: "Select Protein 2",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Second Protein") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded2,
                        onDismissRequest = { expanded2 = false }
                    ) {
                        savedProteins.forEach { protein ->
                            DropdownMenuItem(
                                text = { Text("${protein.accession} - ${protein.proteinName} (${protein.organism})") },
                                onClick = {
                                    selectedProtein2 = protein
                                    expanded2 = false
                                }
                            )
                        }
                    }
                }

                // Align Button
                Button(
                    onClick = {
                        val p1 = selectedProtein1
                        val p2 = selectedProtein2
                        if (p1 == null || p2 == null) {
                            alignmentErrorMessage = "Please select two proteins to align."
                            alignmentResult = null
                        } else if (p1.sequence.isEmpty() || p2.sequence.isEmpty()) {
                            alignmentErrorMessage = "One of the selected proteins has an empty sequence."
                            alignmentResult = null
                        } else {
                            alignmentErrorMessage = null
                            val aligner = PairwiseAligner(
                                mode = PairwiseAligner.Mode.GLOBAL,
                                substitutionMatrix = ScoringMatrices.BLOSUM62,
                                gapOpen = -11.0,
                                gapExtend = -1.0
                            )
                            alignmentResult = aligner.align(p1.sequence, p2.sequence)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(imageVector = Icons.Default.CompareArrows, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Align Proteins (bioKT BLOSUM62)", fontSize = 16.sp)
                }

                if (alignmentErrorMessage != null) {
                    Text(
                        text = alignmentErrorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Alignment Result Display Card
                alignmentResult?.let { res ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Alignment Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = String.format(Locale.US, "Score: %.1f", res.score),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = String.format(Locale.US, "Identity: %.2f%%", res.identity),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "Pairwise Alignment Output (prettyPrint):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Monospace alignment scrollable area
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .horizontalScroll(rememberScrollState())
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = res.prettyPrint(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
