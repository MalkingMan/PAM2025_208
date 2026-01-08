package com.example.mounttrack.ui.news

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mounttrack.data.model.HikingNews
import com.example.mounttrack.data.repository.NewsRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsViewModel : ViewModel() {

    companion object {
        private const val TAG = "NewsViewModel"
    }

    private val newsRepository = NewsRepository()

    private val _featuredNews = MutableLiveData<HikingNews?>()
    val featuredNews: LiveData<HikingNews?> = _featuredNews

    private val _newsList = MutableLiveData<List<HikingNews>>()
    val newsList: LiveData<List<HikingNews>> = _newsList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var allNews: List<HikingNews> = emptyList()
    private var currentCategory: String = "All"

    init {
        loadNews()
    }

    private fun loadNews() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Loading all news...")

                val result = newsRepository.getLatestNews(100)

                result.onSuccess { news ->
                    Log.d(TAG, "Successfully loaded ${news.size} news")
                    allNews = news

                    // Set featured news (first with isFeatured=true, or fallback to latest)
                    val featured = news.firstOrNull { it.isFeatured }
                    _featuredNews.value = featured
                    Log.d(TAG, "Featured news: ${featured?.title}")

                    // Set news list
                    // If we have more than 2 news, exclude featured from list
                    // If we have 1-2 news, show all in the list too
                    val regularNews = if (featured != null && news.size > 2) {
                        news.filter { it.actualId != featured.actualId }
                    } else {
                        news
                    }
                    _newsList.value = regularNews
                    Log.d(TAG, "Regular news count: ${regularNews.size}")
                }.onFailure { exception ->
                    Log.e(TAG, "Error loading news", exception)
                    _featuredNews.value = null
                    _newsList.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadNews", e)
                _featuredNews.value = null
                _newsList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByCategory(category: String) {
        currentCategory = category
        Log.d(TAG, "Filtering by category: $category")
        Log.d(TAG, "Total news available: ${allNews.size}")

        val featured = _featuredNews.value

        val filteredNews = when {
            category.equals("All", ignoreCase = true) -> {
                Log.d(TAG, "Showing all news")
                allNews
            }
            else -> {
                val filtered = allNews.filter { news ->
                    val categoryMatch = news.category.equals(category, ignoreCase = true) ||
                            news.category.replace(" ", "").equals(category.replace(" ", ""), ignoreCase = true) ||
                            category.contains(news.category, ignoreCase = true) ||
                            news.category.contains(category, ignoreCase = true)

                    if (categoryMatch) {
                        Log.d(TAG, "News '${news.title}' matches category filter")
                    }
                    categoryMatch
                }
                Log.d(TAG, "Filtered ${filtered.size} news for category: $category")
                filtered
            }
        }

        // Show all filtered news in the list
        // Only exclude featured if we have more than 2 items
        val regularNews = if (featured != null && filteredNews.size > 2) {
            filteredNews.filter { it.actualId != featured.actualId }
        } else {
            filteredNews
        }

        Log.d(TAG, "Final news list count after removing featured: ${regularNews.size}")
        _newsList.value = regularNews
    }

    fun formatDate(timestamp: Timestamp?): String {
        if (timestamp == null) return ""

        val now = System.currentTimeMillis()
        val diff = now - (timestamp.seconds * 1000)

        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} mins ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hrs ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> {
                val days = diff / (24 * 60 * 60 * 1000)
                if (days == 1L) "Yesterday" else "$days days ago"
            }
            else -> {
                val date = Date(timestamp.seconds * 1000)
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
            }
        }
    }
}

