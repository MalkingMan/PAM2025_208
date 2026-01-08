package com.example.mountadmin.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.News
import com.example.mountadmin.data.repository.NewsRepository
import kotlinx.coroutines.launch

class AddNewsViewModel : ViewModel() {

    private val repository = NewsRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun saveNews(news: News) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.addNews(news)

            result.onSuccess {
                _saveSuccess.value = true
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to save news"
            }

            _isLoading.value = false
        }
    }
}

