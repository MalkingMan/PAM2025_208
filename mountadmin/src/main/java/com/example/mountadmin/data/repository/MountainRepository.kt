package com.example.mountadmin.data.repository

import android.util.Log
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.data.model.HikingRoute
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MountainRepository {

    companion object {
        private const val TAG = "MountainRepository"
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val mountainsCollection = firestore.collection("mountains")

    suspend fun getAllMountains(): Result<List<Mountain>> {
        return try {
            val snapshot = mountainsCollection
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()

            val mountains = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Mountain::class.java)?.copy(mountainId = doc.id)
            }

            Result.success(mountains)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveMountains(): Result<List<Mountain>> {
        return try {
            val snapshot = mountainsCollection
                .whereEqualTo("isActive", true)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()

            val mountains = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Mountain::class.java)?.copy(mountainId = doc.id)
            }

            Result.success(mountains)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMountainById(mountainId: String): Result<Mountain> {
        return try {
            val doc = mountainsCollection.document(mountainId).get().await()

            if (!doc.exists()) {
                throw Exception("Mountain not found")
            }

            val mountain = doc.toObject(Mountain::class.java)?.copy(mountainId = doc.id)
                ?: throw Exception("Failed to load mountain data")

            Result.success(mountain)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createMountain(mountain: Mountain): Result<String> {
        return try {
            Log.d(TAG, "Creating mountain: ${mountain.name}")

            val mountainId = UUID.randomUUID().toString()
            val newMountain = mountain.copy(
                mountainId = mountainId,
                createdAt = Timestamp.now()
            )

            // Convert to map for Firestore
            val data = hashMapOf(
                "mountainId" to newMountain.mountainId,
                "name" to newMountain.name,
                "province" to newMountain.province,
                "country" to newMountain.country,
                "location" to newMountain.location,
                "elevation" to newMountain.elevation,
                "height" to newMountain.height,
                "description" to newMountain.description,
                "imageUrl" to newMountain.imageUrl,
                "routes" to newMountain.routes.map { route ->
                    mapOf(
                        "routeId" to route.routeId.ifEmpty { UUID.randomUUID().toString() },
                        "name" to route.name,
                        "difficulty" to route.difficulty,
                        "estimatedTime" to route.estimatedTime,
                        "distance" to route.distance,
                        // Capacity fields - MUST be included
                        "maxCapacity" to route.maxCapacity,
                        "usedCapacity" to route.usedCapacity,
                        "status" to route.status
                    )
                },
                "isActive" to newMountain.isActive,
                "createdAt" to newMountain.createdAt
            )

            Log.d(TAG, "Saving to Firestore with ID: $mountainId")
            mountainsCollection.document(mountainId).set(data).await()
            Log.d(TAG, "Mountain saved successfully: $mountainId")

            Result.success(mountainId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating mountain: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Alias for createMountain
    suspend fun addMountain(mountain: Mountain): Result<String> = createMountain(mountain)

    suspend fun updateMountain(mountain: Mountain): Result<Unit> {
        return try {
            val data = hashMapOf(
                "name" to mountain.name,
                "province" to mountain.province,
                "country" to mountain.country,
                "location" to mountain.location,
                "elevation" to mountain.elevation,
                "height" to mountain.height,
                "description" to mountain.description,
                "imageUrl" to mountain.imageUrl,
                "routes" to mountain.routes.map { route ->
                    mapOf(
                        "routeId" to route.routeId.ifEmpty { UUID.randomUUID().toString() },
                        "name" to route.name,
                        "difficulty" to route.difficulty,
                        "estimatedTime" to route.estimatedTime,
                        "distance" to route.distance,
                        // Capacity fields - MUST be included
                        "maxCapacity" to route.maxCapacity,
                        "usedCapacity" to route.usedCapacity,
                        "status" to route.status
                    )
                },
                "isActive" to mountain.isActive
            )

            mountainsCollection.document(mountain.mountainId).update(data as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMountain(mountainId: String): Result<Unit> {
        return try {
            mountainsCollection.document(mountainId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchMountains(query: String): Result<List<Mountain>> {
        return try {
            val allMountains = getAllMountains().getOrThrow()
            val filtered = allMountains.filter { mountain ->
                mountain.name.contains(query, ignoreCase = true) ||
                mountain.province.contains(query, ignoreCase = true) ||
                mountain.country.contains(query, ignoreCase = true)
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

