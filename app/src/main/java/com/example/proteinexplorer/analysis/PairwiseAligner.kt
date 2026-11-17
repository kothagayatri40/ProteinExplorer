package com.example.proteinexplorer.analysis

import kotlin.math.max

object ScoringMatrices {
    private val aaList = listOf(
        'A', 'R', 'N', 'D', 'C', 'Q', 'E', 'G', 'H', 'I',
        'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V'
    )

    // Standard BLOSUM62 matrix values
    val BLOSUM62: Map<Pair<Char, Char>, Int> by lazy {
        val matrix = mutableMapOf<Pair<Char, Char>, Int>()
        val rawMatrix = arrayOf(
            intArrayOf( 4, -1, -2, -2,  0, -1, -1,  0, -2, -1, -1, -1, -1, -2, -1,  1,  0, -3, -2,  0), // A
            intArrayOf(-1,  5,  0, -2, -3,  1,  0, -2,  0, -3, -2,  2, -1, -3, -2, -1, -1, -3, -2, -3), // R
            intArrayOf(-2,  0,  6,  1, -3,  0,  0,  0,  1, -3, -3,  0, -2, -3, -2,  1,  0, -4, -2, -3), // N
            intArrayOf(-2, -2,  1,  6, -3,  0,  2, -1, -1, -3, -4, -1, -3, -3, -1,  0, -1, -4, -3, -3), // D
            intArrayOf( 0, -3, -3, -3,  9, -3, -4, -3, -3, -1, -1, -3, -1, -2, -3, -1, -1, -2, -2, -1), // C
            intArrayOf(-1,  1,  0,  0, -3,  5,  2, -2,  0, -3, -2,  1,  0, -3, -1,  0, -1, -2, -1, -2), // Q
            intArrayOf(-1,  0,  0,  2, -4,  2,  5, -2,  0, -3, -3,  1, -2, -3, -1,  0, -1, -3, -2, -2), // E
            intArrayOf( 0, -2,  0, -1, -3, -2, -2,  6, -2, -4, -4, -2, -3, -3, -2,  0, -2, -2, -3, -3), // G
            intArrayOf(-2,  0,  1, -1, -3,  0,  0, -2,  8, -3, -3, -1, -2, -1, -2, -1, -2, -2,  2, -3), // H
            intArrayOf(-1, -3, -3, -3, -1, -3, -3, -4, -3,  4,  2, -3,  1,  0, -3, -2, -1, -3, -1,  3), // I
            intArrayOf(-1, -2, -3, -4, -1, -2, -3, -4, -3,  2,  4, -2,  2,  0, -3, -2, -1, -2, -1,  1), // L
            intArrayOf(-1,  2,  0, -1, -3,  1,  1, -2, -1, -3, -2,  5, -1, -3, -1,  0, -1, -3, -2, -2), // K
            intArrayOf(-1, -1, -2, -3, -1,  0, -2, -3, -2,  1,  2, -1,  5,  0, -2, -1, -1, -1, -1,  1), // M
            intArrayOf(-2, -3, -3, -3, -2, -3, -3, -3, -1,  0,  0, -3,  0,  6, -4, -2, -2,  1,  3, -1), // F
            intArrayOf(-1, -2, -1, -1, -3, -1, -1, -2, -2, -3, -3, -1, -2, -4,  7, -1, -1, -4, -3, -2), // P
            intArrayOf( 1, -1,  0,  0, -1,  0,  0,  0, -1, -2, -2,  0, -1, -2, -1,  4,  1, -3, -2, -2), // S
            intArrayOf( 0, -1,  0, -1, -1, -1, -1, -2, -2, -1, -1, -1, -1, -2, -1,  1,  5, -2, -2,  0), // T
            intArrayOf(-3, -3, -4, -4, -2, -2, -3, -2, -2, -3, -2, -3, -1,  1, -4, -3, -2, 11,  2, -3), // W
            intArrayOf(-2, -2, -2, -3, -2, -1, -2, -3,  2, -1, -1, -2, -1,  3, -3, -2, -2,  2,  7, -1), // Y
            intArrayOf( 0, -3, -3, -3, -1, -2, -2, -3, -3,  3,  1, -2,  1, -1, -2, -2,  0, -3, -1,  4)  // V
        )

        for (i in aaList.indices) {
            for (j in aaList.indices) {
                matrix[Pair(aaList[i], aaList[j])] = rawMatrix[i][j]
            }
        }
        matrix
    }

    fun score(c1: Char, c2: Char): Int {
        return BLOSUM62[Pair(c1.uppercaseChar(), c2.uppercaseChar())] ?: -4
    }
}

data class AlignmentResult(
    val score: Double,
    val identity: Double,
    val alignedSeq1: String,
    val alignedSeq2: String
) {
    fun prettyPrint(): String {
        val matchLine = StringBuilder()
        var matches = 0
        val total = alignedSeq1.length

        for (i in 0 until total) {
            val c1 = alignedSeq1[i]
            val c2 = alignedSeq2[i]
            when {
                c1 == c2 && c1 != '-' -> {
                    matchLine.append("|")
                    matches++
                }
                c1 != '-' && c2 != '-' && ScoringMatrices.score(c1, c2) > 0 -> {
                    matchLine.append(":")
                }
                else -> {
                    matchLine.append(" ")
                }
            }
        }

        val sb = StringBuilder()
        sb.append("Alignment Score: $score\n")
        sb.append(String.format("Identity: %.2f%%\n\n", identity))
        
        // Print in blocks of 60 characters
        val chunkSize = 60
        var idx = 0
        while (idx < total) {
            val end = (idx + chunkSize).coerceAtMost(total)
            sb.append(String.format("Seq1: %s\n", alignedSeq1.substring(idx, end)))
            sb.append(String.format("      %s\n", matchLine.substring(idx, end)))
            sb.append(String.format("Seq2: %s\n\n", alignedSeq2.substring(idx, end)))
            idx = end
        }

        return sb.toString().trimEnd()
    }
}

class PairwiseAligner(
    val mode: Mode = Mode.GLOBAL,
    val substitutionMatrix: Map<Pair<Char, Char>, Int> = ScoringMatrices.BLOSUM62,
    val gapOpen: Double = -11.0,
    val gapExtend: Double = -1.0
) {
    enum class Mode { GLOBAL, LOCAL }

    fun align(s1: String, s2: String): AlignmentResult {
        val seq1 = s1.uppercase().filter { it in 'A'..'Z' }
        val seq2 = s2.uppercase().filter { it in 'A'..'Z' }

        if (seq1.isEmpty() || seq2.isEmpty()) {
            return AlignmentResult(0.0, 0.0, seq1, seq2)
        }

        val n = seq1.length
        val m = seq2.length

        // Needleman-Wunsch Global Alignment
        val dp = Array(n + 1) { DoubleArray(m + 1) }

        for (i in 0..n) dp[i][0] = i * gapOpen
        for (j in 0..m) dp[0][j] = j * gapOpen

        for (i in 1..n) {
            for (j in 1..m) {
                val matchScore = (substitutionMatrix[Pair(seq1[i - 1], seq2[j - 1])] ?: -4).toDouble()
                val scoreDiagonal = dp[i - 1][j - 1] + matchScore
                val scoreUp = dp[i - 1][j] + gapOpen
                val scoreLeft = dp[i][j - 1] + gapOpen

                dp[i][j] = max(scoreDiagonal, max(scoreUp, scoreLeft))
            }
        }

        // Traceback
        val align1 = StringBuilder()
        val align2 = StringBuilder()
        var i = n
        var j = m

        while (i > 0 && j > 0) {
            val currentScore = dp[i][j]
            val matchScore = (substitutionMatrix[Pair(seq1[i - 1], seq2[j - 1])] ?: -4).toDouble()

            if (i > 0 && j > 0 && Math.abs(currentScore - (dp[i - 1][j - 1] + matchScore)) < 0.001) {
                align1.append(seq1[i - 1])
                align2.append(seq2[j - 1])
                i--
                j--
            } else if (i > 0 && Math.abs(currentScore - (dp[i - 1][j] + gapOpen)) < 0.001) {
                align1.append(seq1[i - 1])
                align2.append("-")
                i--
            } else {
                align1.append("-")
                align2.append(seq2[j - 1])
                j--
            }
        }

        while (i > 0) {
            align1.append(seq1[i - 1])
            align2.append("-")
            i--
        }
        while (j > 0) {
            align1.append("-")
            align2.append(seq2[j - 1])
            j--
        }

        val finalSeq1 = align1.reverse().toString()
        val finalSeq2 = align2.reverse().toString()

        var matches = 0
        for (k in finalSeq1.indices) {
            if (finalSeq1[k] == finalSeq2[k] && finalSeq1[k] != '-') {
                matches++
            }
        }

        val totalLen = finalSeq1.length
        val identityPct = if (totalLen > 0) (matches.toDouble() / totalLen) * 100.0 else 0.0
        val alignmentScore = dp[n][m]

        return AlignmentResult(alignmentScore, identityPct, finalSeq1, finalSeq2)
    }
}
