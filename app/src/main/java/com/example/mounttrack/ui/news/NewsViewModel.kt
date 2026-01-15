package com.example.mounttrack.ui.news

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mounttrack.data.model.HikingNews
import com.example.mounttrack.data.repository.NewsRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        startRealtimeListener()
    }

    /**
     * Start real-time listener for news.
     * Data automatically updates when admin publishes/edits/deletes news.
     */
    private fun startRealtimeListener() {
        Log.d(TAG, "Starting real-time news listener...")
        _isLoading.value = true

        newsRepository.getNewsRealtime(100)
            .onEach { news ->
                Log.d(TAG, "Real-time news update: ${news.size} items")
                allNews = news

                // Set featured news (first with isFeatured=true, or fallback to latest)
                val featured = news.firstOrNull { it.isFeatured }
                _featuredNews.value = featured
                Log.d(TAG, "Featured news: ${featured?.title ?: "none"}")

                // Apply current category filter
                applyFilter()
                _isLoading.value = false
            }
            .catch { exception ->
                Log.e(TAG, "Error in news real-time stream: ${exception.message}")
                _featuredNews.value = null
                _newsList.value = emptyList()
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    fun filterByCategory(category: String) {
        currentCategory = category
        Log.d(TAG, "Filtering by category: $category")
        applyFilter()
    }

    private fun applyFilter() {
        Log.d(TAG, "Applying filter: $currentCategory, total news: ${allNews.size}")

        val featured = _featuredNews.value

        val filteredNews = when {
            currentCategory.equals("All", ignoreCase = true) -> {
                Log.d(TAG, "Showing all news")
                allNews
            }
            else -> {
                val filtered = allNews.filter { news ->
                    val categoryMatch = news.category.equals(currentCategory, ignoreCase = true) ||
                            news.category.replace(" ", "").equals(currentCategory.replace(" ", ""), ignoreCase = true) ||
                            currentCategory.contains(news.category, ignoreCase = true) ||
                            news.category.contains(currentCategory, ignoreCase = true)

                    if (categoryMatch) {
                        Log.d(TAG, "News '${news.title}' matches category filter")
                    }
                    categoryMatch
                }
                Log.d(TAG, "Filtered ${filtered.size} news for category: $currentCategory")
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

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared, removing listener")
        newsRepository.removeListener()
    }
}
