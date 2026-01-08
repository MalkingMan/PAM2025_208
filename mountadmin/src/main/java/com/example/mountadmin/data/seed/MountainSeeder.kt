package com.example.mountadmin.data.seed

import android.util.Log
import com.example.mountadmin.data.model.Mountain
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Idempotent Firestore seeder for `mountains` collection.
 *
 * Strategy:
 * - Upsert by deterministic document id (slug-like) to prevent duplicates.
 * - Always writes all required fields with correct types.
 */
object MountainSeeder {

    private const val TAG = "MountainSeeder"

    /**
     * Seed mountains into Firestore.
     *
     * If [force] is false, it will SKIP seeding when it finds at least 1 document.
     * If [force] is true, it will upsert (merge=false) the full seed set.
     */
    suspend fun seedMountains(
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
        force: Boolean = false
    ): Result<Unit> {
        return try {
            val col = firestore.collection("mountains")

            if (!force) {
                val existing = col.limit(1).get().await()
                if (!existing.isEmpty) {
                    Log.d(TAG, "Skipping seed: mountains collection is not empty")
                    return Result.success(Unit)
                }
            }

            val mountains = MountainSeedData.build()
            val batch = firestore.batch()

            mountains.forEach { mountain ->
                val docId = stableDocId(mountain)
                val docRef = col.document(docId)
                val data = mountainToFirestoreMap(docId, mountain)
                batch.set(docRef, data) // overwrite for consistency
            }

            batch.commit().await()
            Log.d(TAG, "Seeded ${mountains.size} mountains")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding mountains: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun stableDocId(mountain: Mountain): String {
        // Ensures deterministic id for upserts. Not shown to user.
        val base = mountain.name
            .lowercase()
            .replace("(", "")
            .replace(")", "")
            .replace("/", "-")
            .replace("\u2013", "-")
            .replace("\u2014", "-")
            .replace("&", "and")
            .replace("'", "")
            .replace("\"", "")
            .replace("  ", " ")
            .trim()
            .replace(" ", "-")
        return "seed-$base"
    }

    private fun mountainToFirestoreMap(docId: String, mountain: Mountain): Map<String, Any?> {
        val createdAt = Timestamp.now()

        // routeId is blank in UI sometimes; for seed we generate stable ids
        val routes = mountain.routes.map { route ->
            val routeId = if (route.routeId.isNotBlank()) route.routeId else UUID.randomUUID().toString()
            mapOf(
                "routeId" to routeId,
                "name" to route.name,
                "difficulty" to route.difficulty,
                "estimatedTime" to route.estimatedTime,
                "distance" to route.distance
            )
        }

        // Firestore schema required by existing repository:
        // mountainId (String), name, province, country, location, elevation(Int/Long), height(Int/Long),
        // description, imageUrl, routes(List<Map>), isActive(Boolean), createdAt(Timestamp)
        return hashMapOf(
            "mountainId" to docId,
            "name" to mountain.name,
            "province" to mountain.province,
            "country" to mountain.country,
            "location" to "${mountain.province}, ${mountain.country}",
            "elevation" to mountain.elevation,
            "height" to mountain.elevation,
            "description" to mountain.description,
            "imageUrl" to mountain.imageUrl,
            "routes" to routes,
            "isActive" to mountain.isActive,
            "createdAt" to createdAt
        )
    }
}
