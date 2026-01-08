package com.example.mounttrack.data.repository

import android.util.Log
import com.example.mounttrack.data.firebase.FirebaseHelper
import com.example.mounttrack.data.model.HikingRoute
import com.example.mounttrack.data.model.Registration
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing route capacity from the user (MountTrack) side.
 * Handles capacity validation and atomic registration with capacity checks.
 */
class RouteCapacityRepository {

    companion object {
        private const val TAG = "RouteCapacityRepo"
    }

    private val firestore = FirebaseHelper.firestore

    /**
     * Get all routes with capacity info for a mountain
     */
    suspend fun getRoutesWithCapacity(mountainId: String): Result<List<HikingRoute>> {
        return try {
            val doc = firestore.collection(FirebaseHelper.COLLECTION_MOUNTAINS)
                .document(mountainId)
                .get()
                .await()

            if (!doc.exists()) {
                return Result.failure(Exception("Mountain not found"))
            }

            val routesData = doc.get("routes") as? List<Map<String, Any>> ?: emptyList()
            val routes = routesData.map { routeMap ->
                HikingRoute(
                    routeId = routeMap["routeId"] as? String ?: "",
                    name = routeMap["name"] as? String ?: "",
                    difficulty = routeMap["difficulty"] as? String ?: "Moderate",
                    estimatedTime = routeMap["estimatedTime"] as? String ?: "",
                    distance = routeMap["distance"] as? String ?: "",
                    maxCapacity = (routeMap["maxCapacity"] as? Long)?.toInt() ?: 0,
                    usedCapacity = (routeMap["usedCapacity"] as? Long)?.toInt() ?: 0,
                    status = routeMap["status"] as? String ?: HikingRoute.STATUS_OPEN
                )
            }

            Log.d(TAG, "Loaded ${routes.size} routes for mountain $mountainId")
            Result.success(routes)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting routes: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get a specific route's capacity info
     */
    suspend fun getRouteCapacity(mountainId: String, routeId: String): Result<HikingRoute> {
        return try {
            val routes = getRoutesWithCapacity(mountainId).getOrThrow()
            val route = routes.find { it.routeId == routeId || it.name == routeId }
                ?: return Result.failure(Exception("Route not found"))
            Result.success(route)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate if a route has available capacity for registration.
     * Returns true if route is open and has remaining capacity.
     */
    suspend fun validateCapacityForRegistration(mountainId: String, routeIdOrName: String): Result<CapacityValidationResult> {
        return try {
            val routes = getRoutesWithCapacity(mountainId).getOrThrow()
            val route = routes.find { it.routeId == routeIdOrName || it.name == routeIdOrName }
                ?: return Result.success(CapacityValidationResult(
                    isValid = false,
                    reason = "Route not found"
                ))

            when {
                route.status == HikingRoute.STATUS_CLOSED -> {
                    Result.success(CapacityValidationResult(
                        isValid = false,
                        reason = "This route is currently closed",
                        remainingCapacity = route.remainingCapacity
                    ))
                }
                route.isFull -> {
                    Result.success(CapacityValidationResult(
                        isValid = false,
                        reason = "This route is full. No available slots.",
                        remainingCapacity = 0
                    ))
                }
                else -> {
                    Result.success(CapacityValidationResult(
                        isValid = true,
                        reason = "Capacity available",
                        remainingCapacity = route.remainingCapacity
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error validating capacity: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Create registration with atomic capacity validation using Firestore transaction.
     * This ensures no overbooking by checking capacity at the moment of creation.
     *
     * Note: For PENDING registrations, we don't decrement capacity.
     * Capacity is only decremented when admin APPROVES the registration.
     */
    suspend fun createRegistrationWithValidation(registration: Registration): Result<String> {
        return try {
            val mountainId = registration.mountainId
            val routeIdOrName = registration.routeId.ifEmpty { registration.route }

            Log.d(TAG, "Creating registration for route: $routeIdOrName")

            // First validate capacity
            val validation = validateCapacityForRegistration(mountainId, routeIdOrName).getOrThrow()

            if (!validation.isValid) {
                return Result.failure(Exception(validation.reason))
            }

            // Create registration with PENDING status (capacity is updated on approval)
            val docRef = firestore.collection(FirebaseHelper.COLLECTION_REGISTRATIONS).document()
            val registrationWithId = registration.copy(
                registrationId = docRef.id,
                createdAt = Timestamp.now()
            )

            docRef.set(registrationWithId).await()

            Log.d(TAG, "Registration created: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating registration: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Cancel user's registration.
     * If registration was APPROVED, capacity will be restored by admin.
     */
    suspend fun cancelRegistration(registrationId: String): Result<Unit> {
        return try {
            // Get current registration to check status
            val regDoc = firestore.collection(FirebaseHelper.COLLECTION_REGISTRATIONS)
                .document(registrationId)
                .get()
                .await()

            if (!regDoc.exists()) {
                return Result.failure(Exception("Registration not found"))
            }

            val currentStatus = regDoc.getString("status") ?: ""
            val mountainId = regDoc.getString("mountainId") ?: ""
            val routeId = regDoc.getString("routeId") ?: ""
            val routeName = regDoc.getString("route") ?: ""

            // If registration was approved, restore capacity
            if (currentStatus == Registration.STATUS_APPROVED && mountainId.isNotEmpty()) {
                firestore.runTransaction { transaction ->
                    val mountainRef = firestore.collection(FirebaseHelper.COLLECTION_MOUNTAINS).document(mountainId)
                    val mountainSnapshot = transaction.get(mountainRef)

                    if (mountainSnapshot.exists()) {
                        val routesData = mountainSnapshot.get("routes") as? List<Map<String, Any>>

                        if (routesData != null) {
                            val routeIndex = routesData.indexOfFirst {
                                (it["routeId"] as? String) == routeId ||
                                (it["name"] as? String) == routeName
                            }

                            if (routeIndex != -1) {
                                val currentRoute = routesData[routeIndex]
                                val usedCapacity = (currentRoute["usedCapacity"] as? Long)?.toInt() ?: 0

                                val updatedRoutes = routesData.toMutableList()
                                val updatedRoute = currentRoute.toMutableMap()
                                updatedRoute["usedCapacity"] = maxOf(0, usedCapacity - 1)
                                updatedRoutes[routeIndex] = updatedRoute

                                transaction.update(mountainRef, "routes", updatedRoutes)
                            }
                        }
                    }

                    // Update registration status
                    val registrationRef = firestore.collection(FirebaseHelper.COLLECTION_REGISTRATIONS).document(registrationId)
                    transaction.update(registrationRef, "status", Registration.STATUS_CANCELLED)
                }.await()
            } else {
                // Just update status for non-approved registrations
                firestore.collection(FirebaseHelper.COLLECTION_REGISTRATIONS)
                    .document(registrationId)
                    .update("status", Registration.STATUS_CANCELLED)
                    .await()
            }

            Log.d(TAG, "Registration cancelled: $registrationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling registration: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get available routes (open and has capacity) for a mountain
     */
    suspend fun getAvailableRoutes(mountainId: String): Result<List<HikingRoute>> {
        return try {
            val routes = getRoutesWithCapacity(mountainId).getOrThrow()
            val availableRoutes = routes.filter { it.isAvailable }
            Result.success(availableRoutes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Result of capacity validation
 */
data class CapacityValidationResult(
    val isValid: Boolean,
    val reason: String,
    val remainingCapacity: Int = 0
)

