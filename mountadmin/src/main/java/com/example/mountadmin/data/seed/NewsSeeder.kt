package com.example.mountadmin.data.seed

import android.util.Log
import com.example.mountadmin.data.model.News
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Idempotent Firestore seeder for `news` collection.
 *
 * Uses deterministic doc IDs (news_001..news_030) to avoid duplicates.
 * Always writes all required fields with correct types.
 */
object NewsSeeder {

    private const val TAG = "NewsSeeder"

    suspend fun seedNews(
        firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
        force: Boolean = false
    ): Result<Int> {
        return try {
            val col = firestore.collection("news")

            if (!force) {
                val existing = col.limit(1).get().await()
                if (!existing.isEmpty) {
                    Log.d(TAG, "Skipping seed: news collection is not empty")
                    return Result.success(0)
                }
            }

            val now = Timestamp.now()
            val newsList = NewsSeedData.build(now)

            val batch = firestore.batch()
            newsList.forEach { news ->
                val docId = news.newsId.ifBlank { news.id }
                val docRef = col.document(docId)
                batch.set(docRef, newsToFirestoreMap(docId, news)) // overwrite for consistency
            }

            batch.commit().await()
            Log.d(TAG, "Seeded ${newsList.size} news")
            Result.success(newsList.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding news: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun newsToFirestoreMap(docId: String, news: News): Map<String, Any?> {
        // Keep both `newsId` and `id` to be tolerant with user app mapping.
        // User app model supports id/newsId/docId.
        return hashMapOf(
            "id" to docId,
            "newsId" to docId,
            "title" to news.title,
            "category" to news.category,
            "content" to news.content,
            "tags" to news.tags,
            "coverImageUrl" to news.coverImageUrl,
            "status" to news.status,
            "published" to true,
            "isFeatured" to news.isFeatured,
            "authorId" to news.authorId,
            "authorName" to news.authorName,
            "createdAt" to news.createdAt,
            "updatedAt" to news.updatedAt
        )
    }
}

