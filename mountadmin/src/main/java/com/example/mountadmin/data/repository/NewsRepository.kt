package com.example.mountadmin.data.repository

import android.util.Log
import com.example.mountadmin.data.model.News
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NewsRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val newsCollection = firestore.collection("news")

    suspend fun getAllNews(): Result<List<News>> {
        return try {
            Log.d("NewsRepository", "Fetching all news from Firestore...")
            val snapshot = newsCollection
                .get()
                .await()

            Log.d("NewsRepository", "Firestore query returned ${snapshot.size()} documents")

            val newsList = snapshot.documents.mapNotNull { doc ->
                try {
                    Log.d("NewsRepository", "Processing document: ${doc.id}")
                    val news = doc.toObject(News::class.java)?.copy(newsId = doc.id)
                    Log.d("NewsRepository", "Mapped to News: ${news?.title}, Status: ${news?.status}")
                    news
                } catch (e: Exception) {
                    Log.e("NewsRepository", "Error mapping document ${doc.id}: ${e.message}", e)
                    null
                }
            }

            // Sort in memory after fetching
            val sortedNews = newsList.sortedByDescending { it.createdAt.seconds }

            Log.d("NewsRepository", "Successfully mapped ${sortedNews.size} news articles")
            Result.success(sortedNews)
        } catch (e: Exception) {
            Log.e("NewsRepository", "Error fetching news: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getPublishedNews(): Result<List<News>> {
        return try {
            Log.d("NewsRepository", "Fetching published news from Firestore...")
            val snapshot = newsCollection
                .whereEqualTo("status", News.STATUS_PUBLISHED)
                .get()
                .await()

            Log.d("NewsRepository", "Published news query returned ${snapshot.size()} documents")

            val newsList = snapshot.documents.mapNotNull { doc ->
                try {
                    Log.d("NewsRepository", "Processing published document: ${doc.id}")
                    val news = doc.toObject(News::class.java)?.copy(newsId = doc.id)
                    Log.d("NewsRepository", "Published news: ${news?.title}, Status: ${news?.status}")
                    news
                } catch (e: Exception) {
                    Log.e("NewsRepository", "Error mapping published document ${doc.id}: ${e.message}", e)
                    null
                }
            }

            // Sort in memory after fetching
            val sortedNews = newsList.sortedByDescending { it.createdAt.seconds }

            Log.d("NewsRepository", "Successfully mapped ${sortedNews.size} published news articles")
            Result.success(sortedNews)
        } catch (e: Exception) {
            Log.e("NewsRepository", "Error fetching published news: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getNewsById(newsId: String): Result<News> {
        return try {
            val doc = newsCollection.document(newsId).get().await()

            if (!doc.exists()) {
                throw Exception("News not found")
            }

            val news = doc.toObject(News::class.java)?.copy(newsId = doc.id)
                ?: throw Exception("Failed to load news data")

            Result.success(news)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createNews(news: News): Result<String> {
        return try {
            val newsId = UUID.randomUUID().toString()
            val newNews = news.copy(
                newsId = newsId,
                published = news.status == News.STATUS_PUBLISHED, // Set published based on status
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            newsCollection.document(newsId).set(newNews).await()
            Result.success(newsId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Alias for createNews
    suspend fun addNews(news: News): Result<String> = createNews(news)

    suspend fun updateNews(news: News): Result<Unit> {
        return try {
            val updates = news.copy(
                published = news.status == News.STATUS_PUBLISHED, // Set published based on status
                updatedAt = Timestamp.now()
            )
            newsCollection.document(news.newsId).set(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNews(newsId: String): Result<Unit> {
        return try {
            newsCollection.document(newsId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchNews(query: String): Result<List<News>> {
        return try {
            val allNews = getAllNews().getOrThrow()
            val filtered = allNews.filter { news ->
                news.title.contains(query, ignoreCase = true) ||
                news.category.contains(query, ignoreCase = true) ||
                news.tags.contains(query, ignoreCase = true)
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

