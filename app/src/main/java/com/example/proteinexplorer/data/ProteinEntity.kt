package com.example.proteinexplorer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proteins")
data class ProteinEntity(
    @PrimaryKey val accession: String,
    val proteinName: String,
    val organism: String,
    val sequence: String,
    val sequenceLength: Int,
    val molecularWeight: Double,
    val isoelectricPoint: Double,
    val instabilityIndex: Double,
    val aromaticity: Double,
    val gravy: Double,
    val timestamp: Long = System.currentTimeMillis()
)
