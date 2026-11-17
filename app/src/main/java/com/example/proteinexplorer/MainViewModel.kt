package com.example.proteinexplorer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proteinexplorer.analysis.ProteinAnalysisResult
import com.example.proteinexplorer.auth.AuthManager
import com.example.proteinexplorer.auth.UserProfile
import com.example.proteinexplorer.data.ProteinDatabase
import com.example.proteinexplorer.data.ProteinEntity
import com.example.proteinexplorer.data.ProteinRepository
import com.example.proteinexplorer.network.UniProtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = AuthManager(application)
    private val uniProtRepository = UniProtRepository()

    private val db = ProteinDatabase.getDatabase(application)
    private val proteinRepository = ProteinRepository(db.proteinDao())

    val savedProteins: StateFlow<List<ProteinEntity>> = proteinRepository.allProteins.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _userProfile = MutableStateFlow<UserProfile?>(authManager.getCurrentUser())
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _accessionInput = MutableStateFlow("P69905")
    val accessionInput: StateFlow<String> = _accessionInput.asStateFlow()

    private val _currentAnalysisResult = MutableStateFlow<ProteinAnalysisResult?>(null)
    val currentAnalysisResult: StateFlow<ProteinAnalysisResult?> = _currentAnalysisResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun updateAccessionInput(newInput: String) {
        _accessionInput.value = newInput
        _errorMessage.value = null
    }

    fun setGuestUser() {
        _userProfile.value = UserProfile(
            displayName = "Practical Exam User",
            email = "demo.researcher@uniprot.org",
            isGuest = true
        )
    }

    fun setUserProfile(profile: UserProfile) {
        _userProfile.value = profile
    }

    fun signOut() {
        authManager.signOut()
        _userProfile.value = null
    }

    fun fetchProtein(accession: String) {
        val cleanAccession = accession.trim().uppercase()
        if (cleanAccession.isEmpty()) {
            _errorMessage.value = "Accession cannot be empty. Try P69905."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = uniProtRepository.fetchProtein(cleanAccession)
            result.onSuccess { analysis ->
                _currentAnalysisResult.value = analysis
                _isLoading.value = false
            }.onFailure { exception ->
                _errorMessage.value = exception.localizedMessage ?: "Failed to fetch protein sequence."
                _isLoading.value = false
            }
        }
    }

    fun saveProtein(analysis: ProteinAnalysisResult) {
        viewModelScope.launch {
            val entity = ProteinEntity(
                accession = analysis.accession,
                proteinName = analysis.proteinName,
                organism = analysis.organism,
                sequence = analysis.sequence,
                sequenceLength = analysis.sequenceLength,
                molecularWeight = analysis.molecularWeight,
                isoelectricPoint = analysis.isoelectricPoint,
                instabilityIndex = analysis.instabilityIndex,
                aromaticity = analysis.aromaticity,
                gravy = analysis.gravy
            )
            proteinRepository.insertProtein(entity)
        }
    }

    fun deleteProtein(entity: ProteinEntity) {
        viewModelScope.launch {
            proteinRepository.deleteProtein(entity)
        }
    }
}
