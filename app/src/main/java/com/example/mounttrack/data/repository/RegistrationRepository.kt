package com.example.mounttrack.data.repository

import com.example.mounttrack.data.firebase.FirebaseHelper
import com.example.mounttrack.data.model.Registration
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class RegistrationRepository {

    private val firestore = FirebaseHelper.firestore

    suspend fun createRegistration(registration: Registration): Result<String> {
        return try {
            val docRef = firestore.collection(FirebaseHelper.COLLECTION_REGISTRATIONS)
                .document()

            val registrationWithId = registration.copy(
                registrationId = docRef.id,
                createdAt = Timestamp.now()
            )

            docRef.set(registrationWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRegistrations(userId: String): Result<List<Registration>> {
        return try {
            val snapshot = firestore.collection(FirebaseHelper.COLLECTION_REGISTRATIONS)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val registrations = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Registration::class.java)
            }.sortedByDescending { it.createdAt }

            Result.success(registrations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentRegistration(userId: String): Result<Registration?> {
        return try {
            val snapshot = firestore.collection(FirebaseHelper.COLLECTION_REGISTRATIONS)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", Registration.STATUS_PENDING)
                .get()
                .await()

            val registration = snapshot.documents.firstOrNull()?.toObject(Registration::class.java)
            Result.success(registration)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPreviousRegistrations(userId: String): Result<List<Registration>> {
        return try {
            val snapshot = firestore.collection(FirebaseHelper.COLLECTION_REGISTRATIONS)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val registrations = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Registration::class.java)
            }.filter { it.status != Registration.STATUS_PENDING }
                .sortedByDescending { it.createdAt }

            Result.success(registrations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

