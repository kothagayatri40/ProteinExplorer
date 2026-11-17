package com.example.proteinexplorer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proteinexplorer.analysis.ProteinAnalyzer
import com.example.proteinexplorer.auth.UserProfile
import com.example.proteinexplorer.scanner.QRScanner
import com.example.proteinexplorer.ui.CompareProteinsScreen
import com.example.proteinexplorer.ui.HomeScreen
import com.example.proteinexplorer.ui.LoginScreen
import com.example.proteinexplorer.ui.ProteinDetailsScreen
import com.example.proteinexplorer.ui.SavedProteinsScreen
import com.example.proteinexplorer.ui.theme.ProteinExplorerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProteinExplorerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProteinExplorerApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ProteinExplorerApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val userProfile by viewModel.userProfile.collectAsState()
    val accessionInput by viewModel.accessionInput.collectAsState()
    val currentAnalysis by viewModel.currentAnalysisResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val savedProteins by viewModel.savedProteins.collectAsState()

    // Automatically navigate to Details when a protein is fetched
    LaunchedEffect(currentAnalysis) {
        if (currentAnalysis != null) {
            navController.navigate("details")
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (userProfile != null) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(
                userProfile = userProfile,
                onGoogleSignInClick = {
                    Toast.makeText(
                        context,
                        "Google Sign-In initiated. Enabling Guest/Demo profile...",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.setGuestUser()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGuestLoginClick = {
                    viewModel.setGuestUser()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onContinueToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                userProfile = userProfile,
                accessionInput = accessionInput,
                onAccessionInputChange = { viewModel.updateAccessionInput(it) },
                isLoading = isLoading,
                errorMessage = errorMessage,
                onScanQRCode = {
                    val qrScanner = QRScanner(context)
                    qrScanner.scanQRCode(
                        onSuccess = { scannedAccession ->
                            viewModel.updateAccessionInput(scannedAccession)
                            Toast.makeText(context, "Scanned QR: $scannedAccession", Toast.LENGTH_SHORT).show()
                            viewModel.fetchProtein(scannedAccession)
                        },
                        onError = { err ->
                            Toast.makeText(context, "Scanner notice: $err", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onFetchProtein = { accession ->
                    viewModel.fetchProtein(accession)
                },
                onNavigateToSaved = {
                    navController.navigate("saved")
                },
                onNavigateToCompare = {
                    navController.navigate("compare")
                },
                onSignOut = {
                    viewModel.signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("details") {
            val analysis = currentAnalysis
            if (analysis != null) {
                ProteinDetailsScreen(
                    result = analysis,
                    onSaveProtein = { toSave ->
                        viewModel.saveProtein(toSave)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            } else {
                navController.popBackStack()
            }
        }

        composable("saved") {
            SavedProteinsScreen(
                proteins = savedProteins,
                onSelectProtein = { selected ->
                    viewModel.fetchProtein(selected.accession)
                },
                onDeleteProtein = { toDelete ->
                    viewModel.deleteProtein(toDelete)
                    Toast.makeText(context, "Deleted ${toDelete.accession}", Toast.LENGTH_SHORT).show()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("compare") {
            CompareProteinsScreen(
                savedProteins = savedProteins,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
