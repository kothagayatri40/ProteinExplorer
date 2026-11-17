package com.example.proteinexplorer.network

import com.example.proteinexplorer.analysis.ProteinAnalysisResult
import com.example.proteinexplorer.analysis.ProteinAnalyzer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class UniProtRepository {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://rest.uniprot.org/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(UniProtService::class.java)

    suspend fun fetchProtein(accession: String): Result<ProteinAnalysisResult> {
        val cleanAccession = accession.trim().uppercase()
        if (cleanAccession.isEmpty()) {
            return Result.failure(IllegalArgumentException("Accession cannot be empty."))
        }

        return try {
            // 1. Fetch FASTA
            val fastaResponse = service.getFastaSequence(cleanAccession)
            if (!fastaResponse.isSuccessful || fastaResponse.body().isNullOrEmpty()) {
                return Result.failure(Exception("UniProt accession '$cleanAccession' not found or server error (Code ${fastaResponse.code()})."))
            }

            val fastaBody = fastaResponse.body()!!
            val (fastaHeader, sequence) = parseFasta(fastaBody)

            if (sequence.isEmpty()) {
                return Result.failure(Exception("Failed to extract sequence from UniProt FASTA response."))
            }

            // 2. Fetch JSON for metadata (Protein name, Organism)
            var proteinName = extractNameFromFastaHeader(fastaHeader)
            var organism = extractOrganismFromFastaHeader(fastaHeader)

            try {
                val jsonResponse = service.getProteinJson(cleanAccession)
                if (jsonResponse.isSuccessful && jsonResponse.body() != null) {
                    val root = jsonResponse.body()!!
                    
                    // Extract Protein Name
                    val jsonName = root.getAsJsonObject("proteinDescription")
                        ?.getAsJsonObject("recommendedName")
                        ?.getAsJsonObject("fullName")
                        ?.get("value")?.asString
                        ?: root.getAsJsonObject("proteinDescription")
                            ?.getAsJsonArray("submissionNames")
                            ?.get(0)?.asJsonObject
                            ?.getAsJsonObject("fullName")
                            ?.get("value")?.asString

                    if (!jsonName.isNullOrEmpty()) {
                        proteinName = jsonName
                    }

                    // Extract Organism
                    val jsonOrganism = root.getAsJsonObject("organism")
                        ?.get("scientificName")?.asString
                    if (!jsonOrganism.isNullOrEmpty()) {
                        organism = jsonOrganism
                    }
                }
            } catch (e: Exception) {
                // Fallback to FASTA header metadata if JSON call encounters any issue
            }

            val result = ProteinAnalyzer.analyze(
                accession = cleanAccession,
                proteinName = proteinName,
                organism = organism,
                rawSequence = sequence
            )

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseFasta(fastaText: String): Pair<String, String> {
        val lines = fastaText.lines()
        var header = ""
        val seqBuilder = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith(">")) {
                header = trimmed
            } else if (trimmed.isNotEmpty()) {
                seqBuilder.append(trimmed)
            }
        }
        return Pair(header, seqBuilder.toString())
    }

    private fun extractNameFromFastaHeader(header: String): String {
        if (header.isEmpty()) return "Protein $header"
        // Example: >sp|P69905|HBA_HUMAN Hemoglobin subunit alpha OS=Homo sapiens OX=9606 GN=HBA1 PE=1 SV=2
        val parts = header.split(" ")
        if (parts.size > 1) {
            val nameParts = mutableListOf<String>()
            for (i in 1 until parts.size) {
                if (parts[i].startsWith("OS=")) break
                nameParts.add(parts[i])
            }
            if (nameParts.isNotEmpty()) {
                return nameParts.joinToString(" ")
            }
        }
        return header.removePrefix(">")
    }

    private fun extractOrganismFromFastaHeader(header: String): String {
        val osIndex = header.indexOf("OS=")
        if (osIndex != -1) {
            val sub = header.substring(osIndex + 3)
            val oxIndex = sub.indexOf(" OX=")
            val gnIndex = sub.indexOf(" GN=")
            val peIndex = sub.indexOf(" PE=")

            val endIndices = listOf(oxIndex, gnIndex, peIndex).filter { it != -1 }
            val minEnd = if (endIndices.isNotEmpty()) endIndices.minOrNull()!! else sub.length
            return sub.substring(0, minEnd).trim()
        }
        return "Unknown Organism"
    }
}
