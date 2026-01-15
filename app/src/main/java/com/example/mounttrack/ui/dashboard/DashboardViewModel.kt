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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        startRealtimeListeners()
    }

    /**
     * Start real-time listeners for mountains and news.
     * Data will automatically update when admin makes changes.
     */
    private fun startRealtimeListeners() {
        Log.d(TAG, "Starting real-time listeners...")
        _isLoading.value = true

        // Real-time popular mountains listener
        mountainRepository.getPopularMountainsRealtime()
            .onEach { mountains ->
                Log.d(TAG, "Real-time update: ${mountains.size} popular mountains")
                _popularMountains.value = mountains
                _isLoading.value = false
            }
            .catch { e ->
                Log.e(TAG, "Error in mountains real-time stream: ${e.message}")
                _popularMountains.value = emptyList()
                _isLoading.value = false
            }
            .launchIn(viewModelScope)

        // Real-time news listener
        newsRepository.getNewsRealtime(10)
            .onEach { news ->
                Log.d(TAG, "========== REAL-TIME NEWS UPDATE ==========")
                Log.d(TAG, "Received ${news.size} news items")
                news.forEachIndexed { index, item ->
                    Log.d(TAG, "News[$index]: title='${item.title}', id='${item.actualId}'")
                }
                _hikingNews.value = news
                Log.d(TAG, "Posted news to LiveData")
                Log.d(TAG, "=============================================")
            }
            .catch { e ->
                Log.e(TAG, "Error in news real-time stream: ${e.message}")
                _hikingNews.value = emptyList()
            }
            .launchIn(viewModelScope)
    }

    /**
     * Legacy method for manual refresh (still works, but not needed with real-time)
     */
    fun refreshData() {
        Log.d(TAG, "Manual refresh requested - real-time already active")
        // Real-time listeners are already active, this is just for compatibility
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared, removing listeners")
        mountainRepository.removeListener()
        newsRepository.removeListener()
    }
}
