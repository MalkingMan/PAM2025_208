package com.example.mounttrack.data.repository

import com.example.mounttrack.data.firebase.FirebaseHelper
import com.example.mounttrack.data.model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseHelper.auth
    private val firestore = FirebaseHelper.firestore

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Login failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user?.let { firebaseUser ->
                // Check if user exists in Firestore
                val userDoc = firestore.collection(FirebaseHelper.COLLECTION_USERS)
                    .document(firebaseUser.uid)
                    .get()
                    .await()

                // If user doesn't exist, create profile
                if (!userDoc.exists()) {
                    val user = User(
                        uid = firebaseUser.uid,
                        fullName = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        phone = "",
                        dob = "",
                        address = ""
                    )
                    saveUserProfile(user)
                }

                Result.success(firebaseUser)
            } ?: Result.failure(Exception("Google Sign-In failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                // Save user profile to Firestore
                val user = User(
                    uid = firebaseUser.uid,
                    fullName = fullName,
                    email = email,
                    phone = "",
                    dob = "",
                    address = ""
                )
                saveUserProfile(user)
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("Registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveUserProfile(user: User) {
        firestore.collection(FirebaseHelper.COLLECTION_USERS)
            .document(user.uid)
            .set(user)
            .await()
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean = FirebaseHelper.isLoggedIn()

    fun getCurrentUserId(): String? = FirebaseHelper.currentUserId
}

