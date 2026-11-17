package com.example.proteinexplorer.analysis

data class ProteinAnalysisResult(
    val accession: String,
    val proteinName: String,
    val organism: String,
    val sequence: String,
    val sequenceLength: Int,
    val molecularWeight: Double,
    val isoelectricPoint: Double,
    val instabilityIndex: Double,
    val aromaticity: Double,
    val gravy: Double,
    val aminoAcidCounts: Map<Char, Int>,
    val aminoAcidPercentages: Map<Char, Double>
)

object ProteinAnalyzer {
    fun analyze(
        accession: String,
        proteinName: String,
        organism: String,
        rawSequence: String
    ): ProteinAnalysisResult {
        val protSeq = ProteinSequence(rawSequence, id = accession)

        val mw = ProteinDescriptors.molecularWeight(protSeq)
        val pi = ProteinDescriptors.isoelectricPoint(protSeq)
        val instability = ProteinDescriptors.instabilityIndex(protSeq)
        val arom = ProteinDescriptors.aromaticity(protSeq)
        val gravyVal = ProteinDescriptors.gravyIndex(protSeq)
        val counts = ProteinDescriptors.aminoAcidComposition(protSeq)
        val pcts = ProteinDescriptors.aminoAcidPercentages(protSeq)

        return ProteinAnalysisResult(
            accession = accession.trim().uppercase(),
            proteinName = proteinName,
            organism = organism,
            sequence = protSeq.sequence,
            sequenceLength = protSeq.length,
            molecularWeight = mw,
            isoelectricPoint = pi,
            instabilityIndex = instability,
            aromaticity = arom,
            gravy = gravyVal,
            aminoAcidCounts = counts,
            aminoAcidPercentages = pcts
        )
    }
}
