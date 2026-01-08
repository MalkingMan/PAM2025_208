package com.example.mounttrack.ui.news.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mounttrack.data.model.HikingNews
import com.example.mounttrack.data.repository.NewsRepository
import kotlinx.coroutines.launch

class NewsDetailViewModel : ViewModel() {

    companion object {
        private const val TAG = "NewsDetailViewModel"
    }

    private val newsRepository = NewsRepository()

    private val _article = MutableLiveData<HikingNews?>()
    val article: LiveData<HikingNews?> = _article

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadArticle(newsId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                Log.d(TAG, "Loading article with ID: $newsId")

                // Get all news and find by ID
                val result = newsRepository.getLatestNews(100)

                result.onSuccess { newsList ->
                    val article = newsList.find {
                        it.actualId == newsId || it.id == newsId || it.newsId == newsId
                    }

                    if (article != null) {
                        Log.d(TAG, "Article found: ${article.title}")
                        _article.value = article
                    } else {
                        Log.e(TAG, "Article not found with ID: $newsId")
                        _errorMessage.value = "Article not found"
                    }
                }.onFailure { exception ->
                    Log.e(TAG, "Error loading article", exception)
                    _errorMessage.value = exception.message ?: "Failed to load article"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadArticle", e)
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

