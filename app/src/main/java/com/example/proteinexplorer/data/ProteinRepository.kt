package com.example.proteinexplorer.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProteinRepository(private val proteinDao: ProteinDao) {

    val allProteins: Flow<List<ProteinEntity>> = proteinDao.getAllProteins()

    suspend fun insertProtein(protein: ProteinEntity) = withContext(Dispatchers.IO) {
        proteinDao.insertProtein(protein)
    }

    suspend fun getProtein(accession: String): ProteinEntity? = withContext(Dispatchers.IO) {
        proteinDao.getProteinByAccession(accession)
    }

    suspend fun deleteProtein(protein: ProteinEntity) = withContext(Dispatchers.IO) {
        proteinDao.deleteProtein(protein)
    }

    suspend fun deleteByAccession(accession: String) = withContext(Dispatchers.IO) {
        proteinDao.deleteByAccession(accession)
    }
}
