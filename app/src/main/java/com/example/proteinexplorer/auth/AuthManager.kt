package com.example.proteinexplorer.auth

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

data class UserProfile(
    val displayName: String,
    val email: String,
    val isGuest: Boolean = false
)

class AuthManager(private val context: Context) {

    private val auth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUser(): UserProfile? {
        val user: FirebaseUser? = try {
            auth?.currentUser
        } catch (e: Exception) {
            null
        }

        return if (user != null) {
            UserProfile(
                displayName = user.displayName ?: "Authenticated User",
                email = user.email ?: "user@firebase.com",
                isGuest = false
            )
        } else {
            null
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            // Ignore if Firebase not initialized
        }
    }
}
