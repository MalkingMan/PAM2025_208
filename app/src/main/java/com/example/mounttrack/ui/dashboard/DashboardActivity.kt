package com.example.mounttrack.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mounttrack.R
import com.example.mounttrack.databinding.ActivityDashboardBinding
import com.example.mounttrack.ui.mountains.MountainsActivity
import com.example.mounttrack.ui.news.NewsActivity
import com.example.mounttrack.ui.registration.MountainRegistrationActivity
import com.example.mounttrack.ui.settings.SettingsActivity
import com.example.mounttrack.ui.status.StatusActivity
import com.example.mounttrack.utils.Constants

class DashboardActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DashboardActivity"
    }

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var popularMountainsAdapter: PopularMountainsAdapter
    private lateinit var hikingNewsAdapter: HikingNewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerViews()
        setupBottomNavigation()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // Popular Mountains RecyclerView
        popularMountainsAdapter = PopularMountainsAdapter { mountain ->
            // Navigate to registration when mountain is clicked
            val intent = Intent(this, MountainRegistrationActivity::class.java).apply {
                putExtra(Constants.EXTRA_MOUNTAIN_ID, mountain.mountainId)
                putExtra(Constants.EXTRA_MOUNTAIN_NAME, mountain.name)
            }
            startActivity(intent)
        }

        binding.rvPopularMountains.apply {
            adapter = popularMountainsAdapter
            layoutManager = LinearLayoutManager(
                this@DashboardActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }

        // Hiking News RecyclerView
        hikingNewsAdapter = HikingNewsAdapter()
        binding.rvHikingNews.apply {
            adapter = hikingNewsAdapter
            layoutManager = LinearLayoutManager(this@DashboardActivity)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_mountains -> {
                    startActivity(Intent(this, MountainsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_news -> {
                    startActivity(Intent(this, NewsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_status -> {
                    startActivity(Intent(this, StatusActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.popularMountains.observe(this) { mountains ->
            Log.d(TAG, "Received ${mountains.size} mountains from ViewModel")
            popularMountainsAdapter.submitList(mountains)
        }

        viewModel.hikingNews.observe(this) { news ->
            Log.d(TAG, "========== NEWS OBSERVER ==========")
            Log.d(TAG, "Received ${news.size} news from ViewModel")
            news.forEachIndexed { index, item ->
                Log.d(TAG, "News[$index]: title='${item.title}'")
            }
            hikingNewsAdapter.submitList(news)
            Log.d(TAG, "Submitted news list to adapter")
            Log.d(TAG, "================================")
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }
}

