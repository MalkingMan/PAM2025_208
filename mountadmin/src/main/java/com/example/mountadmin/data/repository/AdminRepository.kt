package com.example.mountadmin.data.repository

import android.util.Log
import com.example.mountadmin.data.model.Admin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class AdminRepository {

    companion object {
        private const val TAG = "AdminRepository"
        var superAdminEmail: String? = null
        var superAdminPassword: String? = null
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val adminsCollection = firestore.collection("admins")

    suspend fun getAllAdmins(): Result<List<Admin>> {
        return try {
            Log.d(TAG, "Fetching all admins...")

            // Get all admins without orderBy to avoid index requirement
            val snapshot = adminsCollection.get().await()

            Log.d(TAG, "Found ${snapshot.documents.size} admin documents")

            val admins = snapshot.documents.mapNotNull { doc ->
                try {
                    Log.d(TAG, "Processing admin doc: ${doc.id}")
                    doc.toObject(Admin::class.java)?.copy(uid = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing admin doc ${doc.id}: ${e.message}")
                    null
                }
            }

            // Sort by createdAt in memory (handles missing field gracefully)
            val sortedAdmins = admins.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }

            Log.d(TAG, "Successfully loaded ${sortedAdmins.size} admins")
            Result.success(sortedAdmins)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching admins: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Function to add existing auth user to Firestore (for recovery)
    suspend fun addExistingAuthUserToFirestore(
        uid: String,
        email: String,
        fullName: String,
        phone: String,
        role: String,
        assignedMountainId: String? = null,
        assignedMountainName: String? = null
    ): Result<Unit> {
        return try {
            val admin = Admin(
                uid = uid,
                email = email,
                fullName = fullName,
                phone = phone,
                role = role,
                assignedMountainId = assignedMountainId,
                assignedMountainName = assignedMountainName,
                status = Admin.STATUS_ACTIVE,
                createdAt = Timestamp.now()
            )

            adminsCollection.document(uid).set(admin).await()
            Log.d(TAG, "Successfully added auth user to Firestore: $uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding auth user to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getAdminsByRole(role: String): Result<List<Admin>> {
        return try {
            val snapshot = adminsCollection
                .whereEqualTo("role", role)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val admins = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Admin::class.java)?.copy(uid = doc.id)
            }

            Result.success(admins)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminById(uid: String): Result<Admin> {
        return try {
            val doc = adminsCollection.document(uid).get().await()

            if (!doc.exists()) {
                throw Exception("Admin not found")
            }

            val admin = doc.toObject(Admin::class.java)?.copy(uid = doc.id)
                ?: throw Exception("Failed to load admin data")

            Result.success(admin)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAdmin(email: String, password: String, admin: Admin): Result<String> {
        return try {
            // Store current super admin info
            val currentSuperAdminEmail = superAdminEmail
            val currentSuperAdminPassword = superAdminPassword

            if (currentSuperAdminEmail.isNullOrEmpty() || currentSuperAdminPassword.isNullOrEmpty()) {
                throw Exception("Super admin credentials not available. Please re-login.")
            }

            var newUid: String

            try {
                // Try to create new auth account
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                newUid = authResult.user?.uid ?: throw Exception("Failed to create account")

                // Sign out the newly created user
                auth.signOut()
            } catch (authException: Exception) {
                // If email already exists in Auth, try to sign in to get the UID
                if (authException.message?.contains("already in use") == true) {
                    try {
                        val signInResult = auth.signInWithEmailAndPassword(email, password).await()
                        newUid = signInResult.user?.uid ?: throw Exception("Failed to get existing user")

                        // Check if already exists in Firestore
                        val existingDoc = adminsCollection.document(newUid).get().await()
                        if (existingDoc.exists()) {
                            auth.signOut()
                            // Re-auth as super admin
                            auth.signInWithEmailAndPassword(currentSuperAdminEmail, currentSuperAdminPassword).await()
                            throw Exception("Admin with this email already exists in database")
                        }

                        auth.signOut()
                    } catch (signInException: Exception) {
                        if (signInException.message?.contains("already exists") == true) {
                            throw signInException
                        }
                        // Can't sign in - wrong password or other issue
                        // Re-auth as super admin and throw original error
                        auth.signOut()
                        auth.signInWithEmailAndPassword(currentSuperAdminEmail, currentSuperAdminPassword).await()
                        throw Exception("Email exists in Authentication but password doesn't match. Please use different email or contact system administrator.")
                    }
                } else {
                    throw authException
                }
            }

            // Re-authenticate as Super Admin BEFORE writing to Firestore
            auth.signInWithEmailAndPassword(currentSuperAdminEmail, currentSuperAdminPassword).await()

            // Now save admin to Firestore with Super Admin permissions
            val newAdmin = admin.copy(
                uid = newUid,
                email = email,
                createdAt = Timestamp.now()
            )
            adminsCollection.document(newUid).set(newAdmin).await()

            Result.success(newUid)
        } catch (e: Exception) {
            // Try to re-authenticate super admin even on failure
            try {
                if (!superAdminEmail.isNullOrEmpty() && !superAdminPassword.isNullOrEmpty()) {
                    auth.signInWithEmailAndPassword(superAdminEmail!!, superAdminPassword!!).await()
                }
            } catch (_: Exception) {}

            Result.failure(e)
        }
    }

    suspend fun updateAdmin(admin: Admin): Result<Unit> {
        return try {
            val updates = mapOf(
                "fullName" to admin.fullName,
                "phone" to admin.phone,
                "role" to admin.role,
                "assignedMountainId" to admin.assignedMountainId,
                "assignedMountainName" to admin.assignedMountainName,
                "status" to admin.status
            )

            adminsCollection.document(admin.uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAdminStatus(uid: String, status: String): Result<Unit> {
        return try {
            adminsCollection.document(uid).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAdmin(uid: String): Result<Unit> {
        return try {
            adminsCollection.document(uid).delete().await()
            // Note: In production, you should also delete the Auth account using Admin SDK
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

