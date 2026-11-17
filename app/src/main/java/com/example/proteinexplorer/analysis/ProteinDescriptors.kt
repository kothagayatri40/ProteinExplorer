package com.example.proteinexplorer.analysis

import kotlin.math.pow

object ProteinDescriptors {

    // Standard amino acid residue molecular weights in Daltons
    private val aaWeights = mapOf(
        'A' to 89.09, 'R' to 174.20, 'N' to 132.12, 'D' to 133.10, 'C' to 121.16,
        'E' to 147.13, 'Q' to 146.15, 'G' to 75.07, 'H' to 155.16, 'I' to 131.17,
        'L' to 131.17, 'K' to 146.19, 'M' to 149.21, 'F' to 165.19, 'P' to 115.13,
        'S' to 105.09, 'T' to 119.12, 'W' to 204.23, 'Y' to 181.19, 'V' to 117.15
    )

    // Kyte-Doolittle Hydropathy values
    private val hydropathyValues = mapOf(
        'A' to 1.8, 'R' to -4.5, 'N' to -3.5, 'D' to -3.5, 'C' to 2.5,
        'E' to -3.5, 'Q' to -3.5, 'G' to -0.4, 'H' to -3.2, 'I' to 4.5,
        'L' to 3.8, 'K' to -3.9, 'M' to 1.9, 'F' to 2.8, 'P' to -1.6,
        'S' to -0.8, 'T' to -0.7, 'W' to -0.9, 'Y' to -1.3, 'V' to 4.2
    )

    // pK values for pI calculation
    private const val PK_N_TERM = 9.69
    private const val PK_C_TERM = 2.34
    private val pKSideChains = mapOf(
        'K' to 10.53, 'R' to 12.48, 'H' to 6.00,
        'D' to 3.86, 'E' to 4.25, 'C' to 8.33, 'Y' to 10.07
    )

    // All standard 20 amino acids
    val standardAminoAcids = listOf(
        'A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L',
        'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y'
    )

    /**
     * Calculates Molecular Weight (Da).
     */
    fun molecularWeight(prot: ProteinSequence): Double {
        val seq = prot.sequence
        if (seq.isEmpty()) return 0.0
        var weight = 18.01524 // Water molecule for N and C termini
        for (aa in seq) {
            weight += aaWeights[aa] ?: 110.0
        }
        return weight
    }

    /**
     * Calculates Isoelectric Point (pI).
     */
    fun isoelectricPoint(prot: ProteinSequence): Double {
        val seq = prot.sequence
        if (seq.isEmpty()) return 7.0

        val counts = aminoAcidComposition(prot)

        var minPh = 0.0
        var maxPh = 14.0
        var pI = 7.0

        while (maxPh - minPh > 0.0001) {
            val midPh = (minPh + maxPh) / 2.0
            val charge = calculateNetCharge(seq, counts, midPh)

            if (charge > 0) {
                minPh = midPh
            } else {
                maxPh = midPh
            }
            pI = midPh
        }
        return pI
    }

    private fun calculateNetCharge(seq: String, counts: Map<Char, Int>, ph: Double): Double {
        // N-term positive charge
        var charge = 1.0 / (1.0 + 10.0.pow(ph - PK_N_TERM))
        // C-term negative charge
        charge -= 1.0 / (1.0 + 10.0.pow(PK_C_TERM - ph))

        // Basic residues (K, R, H) -> Positive
        charge += (counts['K'] ?: 0) / (1.0 + 10.0.pow(ph - (pKSideChains['K'] ?: 10.53)))
        charge += (counts['R'] ?: 0) / (1.0 + 10.0.pow(ph - (pKSideChains['R'] ?: 12.48)))
        charge += (counts['H'] ?: 0) / (1.0 + 10.0.pow(ph - (pKSideChains['H'] ?: 6.00)))

        // Acidic residues (D, E, C, Y) -> Negative
        charge -= (counts['D'] ?: 0) / (1.0 + 10.0.pow((pKSideChains['D'] ?: 3.86) - ph))
        charge -= (counts['E'] ?: 0) / (1.0 + 10.0.pow((pKSideChains['E'] ?: 4.25) - ph))
        charge -= (counts['C'] ?: 0) / (1.0 + 10.0.pow((pKSideChains['C'] ?: 8.33) - ph))
        charge -= (counts['Y'] ?: 0) / (1.0 + 10.0.pow((pKSideChains['Y'] ?: 10.07) - ph))

        return charge
    }

    /**
     * Calculates Instability Index (Guruprasad et al.).
     */
    fun instabilityIndex(prot: ProteinSequence): Double {
        val seq = prot.sequence
        if (seq.length < 2) return 0.0

        var sumDI = 0.0
        for (i in 0 until seq.length - 1) {
            val dipeptide = "${seq[i]}${seq[i + 1]}"
            sumDI += DI_VALUES[dipeptide] ?: 1.0
        }
        return (10.0 / seq.length) * sumDI
    }

    /**
     * Calculates Aromaticity (F + W + Y ratio).
     */
    fun aromaticity(prot: ProteinSequence): Double {
        val seq = prot.sequence
        if (seq.isEmpty()) return 0.0
        val aromaticCount = seq.count { it == 'F' || it == 'W' || it == 'Y' }
        return aromaticCount.toDouble() / seq.length
    }

    /**
     * Calculates GRAVY (Grand Average of Hydropathicity).
     */
    fun gravyIndex(prot: ProteinSequence): Double {
        val seq = prot.sequence
        if (seq.isEmpty()) return 0.0
        var sumHydropathy = 0.0
        for (aa in seq) {
            sumHydropathy += hydropathyValues[aa] ?: 0.0
        }
        return sumHydropathy / seq.length
    }

    /**
     * Calculates Amino Acid Composition (Counts).
     */
    fun aminoAcidComposition(prot: ProteinSequence): Map<Char, Int> {
        val counts = standardAminoAcids.associateWith { 0 }.toMutableMap()
        for (aa in prot.sequence) {
            if (counts.containsKey(aa)) {
                counts[aa] = counts[aa]!! + 1
            }
        }
        return counts
    }

    /**
     * Calculates Amino Acid Composition (Percentages 0..100).
     */
    fun aminoAcidPercentages(prot: ProteinSequence): Map<Char, Double> {
        val counts = aminoAcidComposition(prot)
        val len = prot.length.coerceAtLeast(1)
        return counts.mapValues { (_, count) -> (count.toDouble() / len) * 100.0 }
    }

    // Standard dipeptide instability values (sample subset of Guruprasad table)
    private val DI_VALUES = mapOf(
        "MC" to 44.94, "MA" to 1.0, "MS" to 1.0, "MT" to 1.0, "MK" to 1.0,
        "WL" to 24.68, "WW" to 1.0, "WV" to 1.0, "WI" to 1.0, "WM" to 1.0,
        "YL" to 1.0, "YY" to 1.0, "YV" to 1.0, "YI" to 1.0, "YM" to 1.0,
        "FL" to 1.0, "FW" to 1.0, "FV" to 1.0, "FI" to 1.0, "FM" to 1.0
    )
}
