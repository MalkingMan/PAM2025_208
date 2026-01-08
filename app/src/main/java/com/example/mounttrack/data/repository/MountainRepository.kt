package com.example.mounttrack.data.repository

import android.util.Log
import com.example.mounttrack.data.firebase.FirebaseHelper
import com.example.mounttrack.data.model.Mountain
import com.example.mounttrack.data.model.HikingRoute
import kotlinx.coroutines.tasks.await

class MountainRepository {

    companion object {
        private const val TAG = "MountainRepository"
    }

    private val firestore = FirebaseHelper.firestore

    suspend fun getMountains(): Result<List<Mountain>> {
        return try {
            Log.d(TAG, "Fetching mountains from Firestore...")

            val snapshot = firestore.collection(FirebaseHelper.COLLECTION_MOUNTAINS)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            Log.d(TAG, "Found ${snapshot.documents.size} mountains")

            val mountains = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    Log.d(TAG, "Mountain data: $data")

                    // Parse routes with capacity data
                    val routesData = (data?.get("routes") as? List<Map<String, Any>>) ?: emptyList()
                    val routesMapped = routesData.map { routeMap ->
                        mapOf(
                            "routeId" to (routeMap["routeId"] ?: ""),
                            "name" to (routeMap["name"] ?: ""),
                            "difficulty" to (routeMap["difficulty"] ?: "Moderate"),
                            "estimatedTime" to (routeMap["estimatedTime"] ?: ""),
                            "distance" to (routeMap["distance"] ?: ""),
                            "maxCapacity" to (routeMap["maxCapacity"] ?: 0L),
                            "usedCapacity" to (routeMap["usedCapacity"] ?: 0L),
                            "status" to (routeMap["status"] ?: HikingRoute.STATUS_OPEN)
                        )
                    }

                    Mountain(
                        mountainId = doc.id,
                        name = data?.get("name") as? String ?: "",
                        province = data?.get("province") as? String ?: "",
                        country = data?.get("country") as? String ?: "Indonesia",
                        elevation = (data?.get("elevation") as? Long)?.toInt() ?: 0,
                        description = data?.get("description") as? String ?: "",
                        imageUrl = data?.get("imageUrl") as? String ?: "",
                        routes = routesMapped,
                        isActive = data?.get("isActive") as? Boolean ?: true
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing mountain: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Parsed ${mountains.size} mountains successfully")
            Result.success(mountains)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching mountains: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getMountainById(mountainId: String): Result<Mountain> {
        return try {
            Log.d(TAG, "Fetching mountain by ID: $mountainId")

            val document = firestore.collection(FirebaseHelper.COLLECTION_MOUNTAINS)
                .document(mountainId)
                .get()
                .await()

            val data = document.data
            if (data != null) {
                // Parse routes with capacity data
                val routesData = (data["routes"] as? List<Map<String, Any>>) ?: emptyList()
                val routesMapped = routesData.map { routeMap ->
                    mapOf(
                        "routeId" to (routeMap["routeId"] ?: ""),
                        "name" to (routeMap["name"] ?: ""),
                        "difficulty" to (routeMap["difficulty"] ?: "Moderate"),
                        "estimatedTime" to (routeMap["estimatedTime"] ?: ""),
                        "distance" to (routeMap["distance"] ?: ""),
                        "maxCapacity" to (routeMap["maxCapacity"] ?: 0L),
                        "usedCapacity" to (routeMap["usedCapacity"] ?: 0L),
                        "status" to (routeMap["status"] ?: HikingRoute.STATUS_OPEN)
                    )
                }

                val mountain = Mountain(
                    mountainId = document.id,
                    name = data["name"] as? String ?: "",
                    province = data["province"] as? String ?: "",
                    country = data["country"] as? String ?: "Indonesia",
                    elevation = (data["elevation"] as? Long)?.toInt() ?: 0,
                    description = data["description"] as? String ?: "",
                    imageUrl = data["imageUrl"] as? String ?: "",
                    routes = routesMapped,
                    isActive = data["isActive"] as? Boolean ?: true
                )
                Log.d(TAG, "Mountain found: ${mountain.name}")
                Result.success(mountain)
            } else {
                Log.e(TAG, "Mountain not found: $mountainId")
                Result.failure(Exception("Mountain not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching mountain: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getPopularMountains(): Result<List<Mountain>> {
        return try {
            val result = getMountains()
            result.map { mountains ->
                mountains.take(4)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get routes with capacity info for a mountain
     */
    suspend fun getRoutesWithCapacity(mountainId: String): Result<List<HikingRoute>> {
        return try {
            val mountain = getMountainById(mountainId).getOrThrow()
            Result.success(mountain.getHikingRoutes())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get only available routes (open and has capacity)
     */
    suspend fun getAvailableRoutes(mountainId: String): Result<List<HikingRoute>> {
        return try {
            val routes = getRoutesWithCapacity(mountainId).getOrThrow()
            val available = routes.filter { it.isAvailable }
            Result.success(available)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

