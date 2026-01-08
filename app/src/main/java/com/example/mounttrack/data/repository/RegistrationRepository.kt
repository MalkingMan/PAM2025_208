package com.example.mounttrack.data.repository

import android.util.Log
import com.example.mounttrack.data.firebase.FirebaseHelper
import com.example.mounttrack.data.model.Registration
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class RegistrationRepository {

    companion object {
        private const val TAG = "RegistrationRepository"
    }

    private val firestore = FirebaseHelper.firestore
    private val capacityRepository = RouteCapacityRepository()

    /**
     * Create registration with capacity validation.
     * This validates that the route has available capacity before creating.
     */
    suspend fun createRegistration(registration: Registration): Result<String> {
        return try {
            Log.d(TAG, "Creating registration for mountain: ${registration.mountainId}, route: ${registration.route}")

            // Validate capacity first
            val routeIdentifier = registration.routeId.ifEmpty { registration.route }
            val validation = capacityRepository.validateCapacityForRegistration(
                registration.mountainId,
                routeIdentifier
            ).getOrThrow()

            if (!validation.isValid) {
                Log.w(TAG, "Capacity validation failed: ${validation.reason}")
                return Result.failure(Exception(validation.reason))
            }

            Log.d(TAG, "Capacity validated. Remaining: ${validation.remainingCapacity}")

            // Create registration
            val docRef = firestore.collection(FirebaseHelper.COLLECTION_REGISTRATIONS)
                .document()

            val registrationWithId = registration.copy(
                registrationId = docRef.id,
                createdAt = Timestamp.now()
            )

            docRef.set(registrationWithId).await()
            Log.d(TAG, "Registration created successfully: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating registration: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Create registration without capacity validation (legacy method).
     * Use createRegistration() instead for proper capacity handling.
     */
    suspend fun createRegistrationLegacy(registration: Registration): Result<String> {
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

    /**
     * Cancel a registration and restore capacity if it was approved
     */
    suspend fun cancelRegistration(registrationId: String): Result<Unit> {
        return capacityRepository.cancelRegistration(registrationId)
    }

    /**
     * Get registration by ID
     */
    suspend fun getRegistrationById(registrationId: String): Result<Registration> {
        return try {
            val doc = firestore.collection(FirebaseHelper.COLLECTION_REGISTRATIONS)
                .document(registrationId)
                .get()
                .await()

            if (!doc.exists()) {
                return Result.failure(Exception("Registration not found"))
            }

            val registration = doc.toObject(Registration::class.java)
                ?: return Result.failure(Exception("Failed to parse registration"))

            Result.success(registration)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

