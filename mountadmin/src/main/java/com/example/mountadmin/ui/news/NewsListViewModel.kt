package com.example.mountadmin.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.News
import com.example.mountadmin.data.repository.NewsRepository
import kotlinx.coroutines.launch

class NewsListViewModel : ViewModel() {

    private val newsRepository = NewsRepository()

    private val _newsList = MutableLiveData<List<News>>()
    val newsList: LiveData<List<News>> = _newsList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadPublishedNews() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = newsRepository.getPublishedNews()
            result.fold(
                onSuccess = { news ->
                    _newsList.value = news
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                    _newsList.value = emptyList()
                }
            )
        }
    }
}
