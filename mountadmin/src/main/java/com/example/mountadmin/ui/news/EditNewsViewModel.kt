package com.example.mountadmin.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.News
import com.example.mountadmin.data.repository.NewsRepository
import kotlinx.coroutines.launch

class EditNewsViewModel : ViewModel() {

    private val repository = NewsRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _news = MutableLiveData<News?>()
    val news: LiveData<News?> = _news

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> = _deleteSuccess

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadNews(newsId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getNewsById(newsId)

            result.onSuccess { news ->
                _news.value = news
            }.onFailure { e ->
                _error.value = e.message
            }

            _isLoading.value = false
        }
    }

    fun updateNews(news: News) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.updateNews(news)

            result.onSuccess {
                _saveSuccess.value = true
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to update news"
            }

            _isLoading.value = false
        }
    }

    fun deleteNews(newsId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.deleteNews(newsId)

            result.onSuccess {
                _deleteSuccess.value = true
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to delete news"
            }

            _isLoading.value = false
        }
    }
}

