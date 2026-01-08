package com.example.mountadmin.data.repository

import com.example.mountadmin.data.model.Admin
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val adminsCollection = firestore.collection("admins")

    suspend fun login(email: String, password: String): Result<Admin> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Login failed")

            // Fetch admin data from Firestore
            val adminDoc = adminsCollection.document(user.uid).get().await()

            if (!adminDoc.exists()) {
                auth.signOut()
                throw Exception("You are not authorized to access admin portal")
            }

            val admin = adminDoc.toObject(Admin::class.java)?.copy(uid = user.uid)
                ?: throw Exception("Failed to load admin data")

            // Check if admin is active
            if (admin.status != Admin.STATUS_ACTIVE) {
                auth.signOut()
                throw Exception("Your account has been disabled")
            }

            // Store credentials for Super Admin (needed for creating new admins)
            if (admin.isSuperAdmin()) {
                AdminRepository.superAdminEmail = email
                AdminRepository.superAdminPassword = password
            }

            Result.success(admin)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentAdmin(): Result<Admin> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("Not logged in")
            val adminDoc = adminsCollection.document(currentUser.uid).get().await()

            if (!adminDoc.exists()) {
                throw Exception("Admin not found")
            }

            val admin = adminDoc.toObject(Admin::class.java)?.copy(uid = currentUser.uid)
                ?: throw Exception("Failed to load admin data")

            Result.success(admin)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAdminAccount(
        email: String,
        password: String,
        admin: Admin
    ): Result<String> {
        return try {
            // Create Firebase Auth account
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Failed to create account")

            // Save admin data to Firestore
            val adminWithUid = admin.copy(
                uid = uid,
                email = email,
                createdAt = Timestamp.now()
            )

            adminsCollection.document(uid).set(adminWithUid).await()

            // Sign out from newly created account and sign back in as current admin
            // Note: In production, you'd use Admin SDK or Cloud Functions

            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        AdminRepository.superAdminEmail = null
        AdminRepository.superAdminPassword = null
        auth.signOut()
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}

