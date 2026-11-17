package com.example.proteinexplorer.network

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UniProtService {

    @GET("uniprotkb/{accession}.fasta")
    suspend fun getFastaSequence(
        @Path("accession") accession: String
    ): Response<String>

    @GET("uniprotkb/{accession}.json")
    suspend fun getProteinJson(
        @Path("accession") accession: String
    ): Response<JsonObject>
}
