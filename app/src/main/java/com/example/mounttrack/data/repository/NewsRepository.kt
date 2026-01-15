package com.example.mounttrack.data.repository

import android.util.Log
import com.example.mounttrack.data.firebase.FirebaseHelper
import com.example.mounttrack.data.model.HikingNews
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NewsRepository {

    companion object {
        private const val TAG = "NewsRepository"
    }

    private val firestore = FirebaseHelper.firestore
    private var newsListener: ListenerRegistration? = null

    /**
     * Real-time stream of published news using Firestore snapshot listener.
     * Automatically updates when news data changes in Firestore.
     */
    fun getNewsRealtime(limit: Int = 100): Flow<List<HikingNews>> = callbackFlow {
        Log.d(TAG, "Starting real-time news listener...")

        val listener = firestore.collection(FirebaseHelper.COLLECTION_NEWS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Real-time news listener error: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d(TAG, "Real-time news update: ${snapshot.documents.size} documents")
                    val newsList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val mapped = doc.toObject(HikingNews::class.java)
                            if (mapped != null) {
                                if (mapped.newsId.isBlank() && mapped.id.isBlank()) {
                                    mapped.newsId = doc.id
                                }
                            }
                            mapped
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing news document ${doc.id}: ${e.message}")
                            null
                        }
                    }.filter { isNewsPublished(it) }
                        .sortedByDescending { newsSortKeySeconds(it) }
                        .take(limit)

                    Log.d(TAG, "Filtered ${newsList.size} published news")
                    trySend(newsList)
                }
            }

        newsListener = listener

        awaitClose {
            Log.d(TAG, "Closing real-time news listener")
            listener.remove()
        }
    }

    fun removeListener() {
        newsListener?.remove()
        newsListener = null
    }

    private fun logFirebaseConfigOnce() {
        try {
            val app = FirebaseApp.getInstance()
            val options = app.options
            Log.d(TAG, "FirebaseApp.name=${app.name}")
            Log.d(TAG, "Firebase projectId=${options.projectId}")
            Log.d(TAG, "Firebase applicationId=${options.applicationId}")
            Log.d(TAG, "Firestore instance=${firestore.app.name}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to log Firebase config: ${e.message}")
        }
    }

    private fun isNewsPublished(news: HikingNews): Boolean {
        // Be tolerant to different admin implementations.
        // Support:
        // - status: "PUBLISHED" | "Published" | "published" | "publish"
        // - published: true
        val normalizedStatus = news.status.trim().uppercase()
        return news.published || normalizedStatus == "PUBLISHED" || normalizedStatus == "PUBLISH"
    }

    private fun newsSortKeySeconds(news: HikingNews): Long {
        // Prefer createdAt, fallback to updatedAt
        return (news.createdAt?.seconds ?: news.updatedAt?.seconds ?: 0L)
    }

    /**
     * Get published news from Firestore
     * Only returns news with status = "PUBLISHED"
     * Ordered by createdAt descending (newest first)
     */
    suspend fun getPublishedNews(): Result<List<HikingNews>> {
        return try {
            logFirebaseConfigOnce()
            Log.d(TAG, "Fetching published news from Firestore...")

            // NOTE: Use broad query then filter locally to tolerate status casing/boolean fields.
            val snapshot = firestore.collection(FirebaseHelper.COLLECTION_NEWS)
                .get()
                .await()

            val newsList = snapshot.documents.mapNotNull { doc ->
                try {
                    val mapped = doc.toObject(HikingNews::class.java)
                    if (mapped != null) {
                        if (mapped.newsId.isBlank() && mapped.id.isBlank()) {
                            mapped.newsId = doc.id
                        }
                    }
                    mapped
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing news document ${doc.id}: ${e.message}")
                    null
                }
            }.filter { isNewsPublished(it) }
                .sortedByDescending { newsSortKeySeconds(it) }

            Log.d(TAG, "Successfully loaded ${newsList.size} published news")
            Result.success(newsList)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching published news: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get latest published news (limited to n items)
     * Tries multiple query strategies to handle different field naming
     */
    suspend fun getLatestNews(limit: Int = 10): Result<List<HikingNews>> {
        return try {
            logFirebaseConfigOnce()
            Log.d(TAG, "========== FETCHING NEWS ==========")
            Log.d(TAG, "Collection path: ${FirebaseHelper.COLLECTION_NEWS}")

            // Get all news from collection
            val snapshot = firestore.collection(FirebaseHelper.COLLECTION_NEWS)
                .get()
                .await()

            Log.d(TAG, "Query completed. Document count: ${snapshot.documents.size}")
            Log.d(TAG, "Is empty: ${snapshot.isEmpty}")

            if (snapshot.isEmpty) {
                Log.w(TAG, "NEWS COLLECTION IS EMPTY!")
                return Result.success(emptyList())
            }

            val allNews = mutableListOf<HikingNews>()

            snapshot.documents.forEachIndexed { index, doc ->
                Log.d(TAG, "--- Document $index ---")
                Log.d(TAG, "Document ID: ${doc.id}")
                Log.d(TAG, "Document exists: ${doc.exists()}")
                Log.d(TAG, "Raw data: ${doc.data}")

                try {
                    // Ensure the model has a usable ID even if admin didn't set newsId/id
                    val mapped = doc.toObject(HikingNews::class.java)
                    if (mapped != null) {
                        if (mapped.newsId.isBlank() && mapped.id.isBlank()) {
                            mapped.newsId = doc.id
                        }
                        allNews.add(mapped)
                    } else {
                        Log.e(TAG, "toObject returned null!")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing document ${doc.id}: ${e.message}", e)
                }
            }

            Log.d(TAG, "Total parsed news: ${allNews.size}")

            // Filter published news (tolerant)
            val publishedNews = allNews.filter { news ->
                val isPublished = isNewsPublished(news)
                Log.d(
                    TAG,
                    "News '${news.title}' - status='${news.status}', published=${news.published}, isPublished=$isPublished"
                )
                isPublished
            }

            Log.d(TAG, "Filtered published news: ${publishedNews.size}")

            // Sort by createdAt/updatedAt descending and take limit
            val newsList = publishedNews
                .sortedByDescending { newsSortKeySeconds(it) }
                .take(limit)

            Log.d(TAG, "Final news list count: ${newsList.size}")
            Log.d(TAG, "========== END FETCHING NEWS ==========")

            Result.success(newsList)

        } catch (e: Exception) {
            Log.e(TAG, "EXCEPTION fetching news: ${e.message}", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Get news by category
     */
    suspend fun getNewsByCategory(category: String): Result<List<HikingNews>> {
        return try {
            Log.d(TAG, "Fetching news by category: $category")

            val snapshot = firestore.collection(FirebaseHelper.COLLECTION_NEWS)
                .whereEqualTo("category", category)
                .get()
                .await()

            val newsList = snapshot.documents.mapNotNull { doc ->
                try {
                    val mapped = doc.toObject(HikingNews::class.java)
                    if (mapped != null) {
                        if (mapped.newsId.isBlank() && mapped.id.isBlank()) {
                            mapped.newsId = doc.id
                        }
                    }
                    mapped
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing news document ${doc.id}: ${e.message}")
                    null
                }
            }.filter { isNewsPublished(it) }
             .sortedByDescending { newsSortKeySeconds(it) }

            Log.d(TAG, "Successfully loaded ${newsList.size} news for category: $category")
            Result.success(newsList)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching news by category: ${e.message}", e)
            Result.failure(e)
        }
    }
}
