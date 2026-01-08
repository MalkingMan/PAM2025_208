package com.example.mountadmin.ui.news

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.News
import com.example.mountadmin.data.repository.NewsRepository
import kotlinx.coroutines.launch

class ManageNewsViewModel : ViewModel() {

    private val repository = NewsRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _newsList = MutableLiveData<List<News>>()
    val newsList: LiveData<List<News>> = _newsList

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var allNews: List<News> = emptyList()

    fun loadNews() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("ManageNewsViewModel", "Starting to load news...")
            val result = repository.getAllNews()

            result.onSuccess { news ->
                Log.d("ManageNewsViewModel", "Successfully loaded ${news.size} news articles")
                news.forEach {
                    Log.d("ManageNewsViewModel", "News: ${it.title}, Status: ${it.status}")
                }
                allNews = news
                _newsList.value = news
            }.onFailure { e ->
                Log.e("ManageNewsViewModel", "Error loading news: ${e.message}", e)
                _error.value = e.message
                _newsList.value = emptyList()
            }

            _isLoading.value = false
        }
    }

    fun searchNews(query: String) {
        if (query.isEmpty()) {
            _newsList.value = allNews
        } else {
            _newsList.value = allNews.filter { news ->
                news.title.contains(query, ignoreCase = true) ||
                news.category.contains(query, ignoreCase = true)
            }
        }
    }

    fun deleteNews(newsId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.deleteNews(newsId)

            result.onSuccess {
                loadNews()
            }.onFailure { e ->
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
}

