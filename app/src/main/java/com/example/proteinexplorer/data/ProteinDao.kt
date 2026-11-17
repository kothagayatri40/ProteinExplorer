package com.example.proteinexplorer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProteinDao {

    @Query("SELECT * FROM proteins ORDER BY timestamp DESC")
    fun getAllProteins(): Flow<List<ProteinEntity>>

    @Query("SELECT * FROM proteins WHERE accession = :accession LIMIT 1")
    fun getProteinByAccession(accession: String): ProteinEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProtein(protein: ProteinEntity)

    @Delete
    fun deleteProtein(protein: ProteinEntity)

    @Query("DELETE FROM proteins WHERE accession = :accession")
    fun deleteByAccession(accession: String)
}
