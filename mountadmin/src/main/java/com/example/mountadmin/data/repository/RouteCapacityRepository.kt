package com.example.mountadmin.data.repository

import android.util.Log
import com.example.mountadmin.data.model.HikingRoute
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.data.model.Registration
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing route capacity.
 * Handles all capacity-related operations with Firestore transactions
 * to ensure data consistency and prevent overbooking.
 */
class RouteCapacityRepository {

    companion object {
        private const val TAG = "RouteCapacityRepo"
        private const val COLLECTION_MOUNTAINS = "mountains"
        private const val COLLECTION_REGISTRATIONS = "registrations"
    }

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Get all routes with capacity info for a mountain
     */
    suspend fun getRoutesWithCapacity(mountainId: String): Result<List<HikingRoute>> {
        return try {
            val doc = firestore.collection(COLLECTION_MOUNTAINS)
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
                    difficulty = routeMap["difficulty"] as? String ?: HikingRoute.DIFFICULTY_MODERATE,
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
            val route = routes.find { it.routeId == routeId }
                ?: return Result.failure(Exception("Route not found"))
            Result.success(route)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate if a route has available capacity
     */
    suspend fun validateCapacity(mountainId: String, routeId: String): Result<Boolean> {
        return try {
            val route = getRouteCapacity(mountainId, routeId).getOrThrow()
            val isAvailable = route.isAvailable
            Log.d(TAG, "Route $routeId capacity check: available=$isAvailable, remaining=${route.remainingCapacity}")
            Result.success(isAvailable)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Approve registration with atomic capacity update using Firestore transaction.
     * This prevents overbooking by checking and updating capacity atomically.
     */
    suspend fun approveRegistrationWithCapacityUpdate(
        registration: Registration
    ): Result<Unit> {
        return try {
            val mountainId = registration.mountainId
            val routeId = registration.routeId
            val registrationId = registration.registrationId

            if (routeId.isEmpty()) {
                // Fallback: find routeId by route name if not set
                return approveRegistrationByRouteName(registration)
            }

            Log.d(TAG, "Approving registration $registrationId for route $routeId")

            firestore.runTransaction { transaction ->
                // 1. Read current mountain data
                val mountainRef = firestore.collection(COLLECTION_MOUNTAINS).document(mountainId)
                val mountainSnapshot = transaction.get(mountainRef)

                if (!mountainSnapshot.exists()) {
                    throw Exception("Mountain not found")
                }

                // 2. Get current routes array
                val routesData = mountainSnapshot.get("routes") as? List<Map<String, Any>>
                    ?: throw Exception("Routes not found")

                // 3. Find the target route and check capacity
                val routeIndex = routesData.indexOfFirst {
                    (it["routeId"] as? String ?: it["name"] as? String) == routeId ||
                    (it["name"] as? String) == registration.route
                }

                if (routeIndex == -1) {
                    throw Exception("Route not found in mountain")
                }

                val currentRoute = routesData[routeIndex]
                val maxCapacity = (currentRoute["maxCapacity"] as? Long)?.toInt() ?: 0
                val usedCapacity = (currentRoute["usedCapacity"] as? Long)?.toInt() ?: 0
                val remainingCapacity = maxCapacity - usedCapacity

                Log.d(TAG, "Route capacity: max=$maxCapacity, used=$usedCapacity, remaining=$remainingCapacity")

                // 4. Validate capacity
                if (remainingCapacity <= 0) {
                    throw Exception("Route is full. No available capacity.")
                }

                // 5. Build updated routes array with incremented usedCapacity
                val updatedRoutes = routesData.toMutableList()
                val updatedRoute = currentRoute.toMutableMap()
                updatedRoute["usedCapacity"] = usedCapacity + 1
                updatedRoutes[routeIndex] = updatedRoute

                // 6. Update mountain document with new routes array
                transaction.update(mountainRef, "routes", updatedRoutes)

                // 7. Update registration status to APPROVED
                val registrationRef = firestore.collection(COLLECTION_REGISTRATIONS).document(registrationId)
                transaction.update(registrationRef, "status", Registration.STATUS_APPROVED)

                Log.d(TAG, "Transaction successful: registration approved, capacity updated")
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error approving registration: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fallback method when routeId is not available - finds route by name
     */
    private suspend fun approveRegistrationByRouteName(registration: Registration): Result<Unit> {
        return try {
            val mountainId = registration.mountainId
            val routeName = registration.route
            val registrationId = registration.registrationId

            Log.d(TAG, "Approving by route name: $routeName")

            firestore.runTransaction { transaction ->
                val mountainRef = firestore.collection(COLLECTION_MOUNTAINS).document(mountainId)
                val mountainSnapshot = transaction.get(mountainRef)

                if (!mountainSnapshot.exists()) {
                    throw Exception("Mountain not found")
                }

                val routesData = mountainSnapshot.get("routes") as? List<Map<String, Any>>
                    ?: throw Exception("Routes not found")

                val routeIndex = routesData.indexOfFirst {
                    (it["name"] as? String) == routeName
                }

                if (routeIndex == -1) {
                    throw Exception("Route '$routeName' not found")
                }

                val currentRoute = routesData[routeIndex]
                val maxCapacity = (currentRoute["maxCapacity"] as? Long)?.toInt() ?: 0
                val usedCapacity = (currentRoute["usedCapacity"] as? Long)?.toInt() ?: 0

                if (maxCapacity - usedCapacity <= 0) {
                    throw Exception("Route is full. No available capacity.")
                }

                val updatedRoutes = routesData.toMutableList()
                val updatedRoute = currentRoute.toMutableMap()
                updatedRoute["usedCapacity"] = usedCapacity + 1
                updatedRoutes[routeIndex] = updatedRoute

                transaction.update(mountainRef, "routes", updatedRoutes)

                val registrationRef = firestore.collection(COLLECTION_REGISTRATIONS).document(registrationId)
                transaction.update(registrationRef, "status", Registration.STATUS_APPROVED)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error approving by route name: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Reject registration - does NOT change capacity
     */
    suspend fun rejectRegistration(registrationId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_REGISTRATIONS)
                .document(registrationId)
                .update("status", Registration.STATUS_REJECTED)
                .await()

            Log.d(TAG, "Registration $registrationId rejected")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting registration: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Cancel registration and restore capacity using Firestore transaction.
     * Only restores capacity if registration was previously APPROVED.
     */
    suspend fun cancelRegistrationWithCapacityRestore(
        registration: Registration
    ): Result<Unit> {
        return try {
            val mountainId = registration.mountainId
            val routeId = registration.routeId
            val routeName = registration.route
            val registrationId = registration.registrationId
            val wasApproved = registration.status == Registration.STATUS_APPROVED

            Log.d(TAG, "Cancelling registration $registrationId, wasApproved=$wasApproved")

            firestore.runTransaction { transaction ->
                // Only restore capacity if registration was approved
                if (wasApproved) {
                    val mountainRef = firestore.collection(COLLECTION_MOUNTAINS).document(mountainId)
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
                                // Decrement usedCapacity but don't go below 0
                                updatedRoute["usedCapacity"] = maxOf(0, usedCapacity - 1)
                                updatedRoutes[routeIndex] = updatedRoute

                                transaction.update(mountainRef, "routes", updatedRoutes)
                                Log.d(TAG, "Capacity restored for route")
                            }
                        }
                    }
                }

                // Update registration status to CANCELLED
                val registrationRef = firestore.collection(COLLECTION_REGISTRATIONS).document(registrationId)
                transaction.update(registrationRef, "status", Registration.STATUS_CANCELLED)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling registration: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Update route capacity settings (SuperAdmin only)
     */
    suspend fun updateRouteCapacity(
        mountainId: String,
        routeId: String,
        newMaxCapacity: Int,
        newStatus: String? = null
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val mountainRef = firestore.collection(COLLECTION_MOUNTAINS).document(mountainId)
                val mountainSnapshot = transaction.get(mountainRef)

                if (!mountainSnapshot.exists()) {
                    throw Exception("Mountain not found")
                }

                val routesData = mountainSnapshot.get("routes") as? List<Map<String, Any>>
                    ?: throw Exception("Routes not found")

                val routeIndex = routesData.indexOfFirst {
                    (it["routeId"] as? String) == routeId
                }

                if (routeIndex == -1) {
                    throw Exception("Route not found")
                }

                val updatedRoutes = routesData.toMutableList()
                val updatedRoute = routesData[routeIndex].toMutableMap()
                updatedRoute["maxCapacity"] = newMaxCapacity
                if (newStatus != null) {
                    updatedRoute["status"] = newStatus
                }
                updatedRoutes[routeIndex] = updatedRoute

                transaction.update(mountainRef, "routes", updatedRoutes)
            }.await()

            Log.d(TAG, "Route $routeId capacity updated to $newMaxCapacity")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating route capacity: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Update route status (open/closed)
     */
    suspend fun updateRouteStatus(
        mountainId: String,
        routeId: String,
        newStatus: String
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val mountainRef = firestore.collection(COLLECTION_MOUNTAINS).document(mountainId)
                val mountainSnapshot = transaction.get(mountainRef)

                if (!mountainSnapshot.exists()) {
                    throw Exception("Mountain not found")
                }

                val routesData = mountainSnapshot.get("routes") as? List<Map<String, Any>>
                    ?: throw Exception("Routes not found")

                val routeIndex = routesData.indexOfFirst {
                    (it["routeId"] as? String) == routeId ||
                    (it["name"] as? String) == routeId // fallback to name match
                }

                if (routeIndex == -1) {
                    throw Exception("Route not found")
                }

                val updatedRoutes = routesData.toMutableList()
                val updatedRoute = routesData[routeIndex].toMutableMap()
                updatedRoute["status"] = newStatus
                updatedRoutes[routeIndex] = updatedRoute

                transaction.update(mountainRef, "routes", updatedRoutes)
            }.await()

            Log.d(TAG, "Route $routeId status updated to $newStatus")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating route status: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Recalculate used capacity from actual approved registrations.
     * Use this for data correction/migration.
     */
    suspend fun recalculateRouteCapacity(mountainId: String): Result<Unit> {
        return try {
            // Get all approved registrations for this mountain
            val registrationsSnapshot = firestore.collection(COLLECTION_REGISTRATIONS)
                .whereEqualTo("mountainId", mountainId)
                .whereEqualTo("status", Registration.STATUS_APPROVED)
                .get()
                .await()

            // Count registrations per route
            val routeCounts = mutableMapOf<String, Int>()
            registrationsSnapshot.documents.forEach { doc ->
                val routeId = doc.getString("routeId") ?: doc.getString("route") ?: ""
                if (routeId.isNotEmpty()) {
                    routeCounts[routeId] = (routeCounts[routeId] ?: 0) + 1
                }
            }

            // Update mountain routes with correct counts
            firestore.runTransaction { transaction ->
                val mountainRef = firestore.collection(COLLECTION_MOUNTAINS).document(mountainId)
                val mountainSnapshot = transaction.get(mountainRef)

                if (!mountainSnapshot.exists()) {
                    throw Exception("Mountain not found")
                }

                val routesData = mountainSnapshot.get("routes") as? List<Map<String, Any>>
                    ?: return@runTransaction

                val updatedRoutes = routesData.map { routeMap ->
                    val mutableRoute = routeMap.toMutableMap()
                    val routeId = routeMap["routeId"] as? String ?: ""
                    val routeName = routeMap["name"] as? String ?: ""

                    // Check both routeId and route name for matches
                    val count = routeCounts[routeId] ?: routeCounts[routeName] ?: 0
                    mutableRoute["usedCapacity"] = count
                    mutableRoute
                }

                transaction.update(mountainRef, "routes", updatedRoutes)
            }.await()

            Log.d(TAG, "Recalculated capacity for mountain $mountainId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error recalculating capacity: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Add a new route or update an existing route (matched by routeId, fallback by name).
     * Preserves usedCapacity unless the caller provides it.
     */
    suspend fun addOrUpdateRoute(
        mountainId: String,
        route: HikingRoute
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val mountainRef = firestore.collection(COLLECTION_MOUNTAINS).document(mountainId)
                val mountainSnapshot = transaction.get(mountainRef)

                if (!mountainSnapshot.exists()) {
                    throw Exception("Mountain not found")
                }

                val routesData = mountainSnapshot.get("routes") as? List<Map<String, Any>> ?: emptyList()

                val routeIndex = routesData.indexOfFirst {
                    val existingId = it["routeId"] as? String
                    val existingName = it["name"] as? String
                    (route.routeId.isNotBlank() && existingId == route.routeId) ||
                        (route.routeId.isBlank() && existingName == route.name)
                }

                val routeMap = mutableMapOf<String, Any>(
                    "routeId" to (route.routeId.ifBlank { route.name }),
                    "name" to route.name,
                    "difficulty" to route.difficulty,
                    "estimatedTime" to route.estimatedTime,
                    "distance" to route.distance,
                    "maxCapacity" to route.maxCapacity,
                    "usedCapacity" to route.usedCapacity,
                    "status" to route.status
                )

                val updated = routesData.toMutableList()

                if (routeIndex >= 0) {
                    // Preserve usedCapacity if stored used is higher (safety)
                    val curr = routesData[routeIndex]
                    val currUsed = (curr["usedCapacity"] as? Long)?.toInt() ?: (curr["usedCapacity"] as? Int ?: 0)
                    if (routeMap["usedCapacity"] is Int) {
                        val newUsed = route.usedCapacity
                        routeMap["usedCapacity"] = maxOf(currUsed, newUsed)
                    }

                    updated[routeIndex] = routeMap
                } else {
                    // New route
                    if (routeMap["routeId"].toString().isBlank()) {
                        routeMap["routeId"] = java.util.UUID.randomUUID().toString()
                    }
                    if ((routeMap["maxCapacity"] as? Int ?: 0) <= 0) {
                        routeMap["maxCapacity"] = 100
                    }
                    if (routeMap["status"].toString().isBlank()) {
                        routeMap["status"] = HikingRoute.STATUS_OPEN
                    }
                    updated.add(routeMap)
                }

                transaction.update(mountainRef, "routes", updated)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error addOrUpdateRoute: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete route if it exists. Caller should enforce safety checks (e.g., usedCapacity == 0).
     */
    suspend fun deleteRoute(
        mountainId: String,
        route: HikingRoute
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val mountainRef = firestore.collection(COLLECTION_MOUNTAINS).document(mountainId)
                val mountainSnapshot = transaction.get(mountainRef)

                if (!mountainSnapshot.exists()) {
                    throw Exception("Mountain not found")
                }

                val routesData = mountainSnapshot.get("routes") as? List<Map<String, Any>> ?: emptyList()

                val updated = routesData.filterNot {
                    val existingId = it["routeId"] as? String
                    val existingName = it["name"] as? String
                    (route.routeId.isNotBlank() && existingId == route.routeId) ||
                        existingName == route.name
                }

                transaction.update(mountainRef, "routes", updated)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleteRoute: ${e.message}", e)
            Result.failure(e)
        }
    }
}
