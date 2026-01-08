package com.example.mounttrack.ui.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mounttrack.data.model.HikingNews
import com.example.mounttrack.data.model.Mountain
import com.example.mounttrack.data.repository.MountainRepository
import com.example.mounttrack.data.repository.NewsRepository
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    companion object {
        private const val TAG = "DashboardViewModel"
    }

    private val mountainRepository = MountainRepository()
    private val newsRepository = NewsRepository()

    private val _popularMountains = MutableLiveData<List<Mountain>>()
    val popularMountains: LiveData<List<Mountain>> = _popularMountains

    private val _hikingNews = MutableLiveData<List<HikingNews>>()
    val hikingNews: LiveData<List<HikingNews>> = _hikingNews

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadPopularMountains()
        loadHikingNews()
    }

    private fun loadPopularMountains() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Loading popular mountains...")

            val result = mountainRepository.getPopularMountains()
            result.fold(
                onSuccess = { mountains ->
                    Log.d(TAG, "Loaded ${mountains.size} popular mountains")
                    _popularMountains.value = mountains
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error loading popular mountains: ${exception.message}")
                    _popularMountains.value = emptyList()
                }
            )
            _isLoading.value = false
        }
    }

    private fun loadHikingNews() {
        viewModelScope.launch {
            Log.d(TAG, "========== LOADING HIKING NEWS ==========")
            _isLoading.value = true

            try {
                val result = newsRepository.getLatestNews(10)
                result.fold(
                    onSuccess = { news ->
                        Log.d(TAG, "SUCCESS: Loaded ${news.size} hiking news")
                        news.forEachIndexed { index, item ->
                            Log.d(TAG, "News[$index]: title='${item.title}', id='${item.actualId}'")
                        }
                        _hikingNews.postValue(news)
                        Log.d(TAG, "Posted news to LiveData")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "FAILURE loading hiking news: ${exception.message}", exception)
                        _hikingNews.postValue(emptyList())
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "EXCEPTION in loadHikingNews: ${e.message}", e)
                _hikingNews.postValue(emptyList())
            }

            _isLoading.value = false
            Log.d(TAG, "========== END LOADING HIKING NEWS ==========")
        }
    }

    fun refreshData() {
        loadPopularMountains()
        loadHikingNews()
    }
}

