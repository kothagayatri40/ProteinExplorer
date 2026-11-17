package com.example.proteinexplorer.analysis

/**
 * bioKT ProteinSequence representation.
 */
data class ProteinSequence(
    val rawSequence: String,
    val id: String = ""
) {
    val sequence: String = rawSequence.uppercase().filter { it in 'A'..'Z' }
    val length: Int get() = sequence.length

    fun molecularWeight(): Double {
        return ProteinDescriptors.molecularWeight(this)
    }

    fun isoelectricPoint(): Double {
        return ProteinDescriptors.isoelectricPoint(this)
    }

    fun aminoAcidComposition(): Map<Char, Int> {
        return ProteinDescriptors.aminoAcidComposition(this)
    }
}
