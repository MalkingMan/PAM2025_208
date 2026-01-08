package com.example.mounttrack.ui.mountains

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mounttrack.R
import com.example.mounttrack.databinding.ActivityMountainsBinding
import com.example.mounttrack.ui.dashboard.DashboardActivity
import com.example.mounttrack.ui.news.NewsActivity
import com.example.mounttrack.ui.registration.MountainRegistrationActivity
import com.example.mounttrack.ui.settings.SettingsActivity
import com.example.mounttrack.ui.status.StatusActivity
import com.example.mounttrack.utils.Constants
import com.example.mounttrack.utils.gone
import com.example.mounttrack.utils.visible

class MountainsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMountainsBinding
    private val viewModel: MountainsViewModel by viewModels()

    private lateinit var mountainsAdapter: MountainsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMountainsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupBottomNavigation()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        mountainsAdapter = MountainsAdapter { mountain ->
            // Navigate to MountainRegistrationActivity
            val intent = Intent(this, MountainRegistrationActivity::class.java).apply {
                putExtra(Constants.EXTRA_MOUNTAIN_ID, mountain.mountainId)
                putExtra(Constants.EXTRA_MOUNTAIN_NAME, mountain.name)
            }
            startActivity(intent)
        }

        binding.rvMountains.apply {
            adapter = mountainsAdapter
            layoutManager = LinearLayoutManager(this@MountainsActivity)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_mountains

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_mountains -> true
                R.id.nav_news -> {
                    startActivity(Intent(this, NewsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
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
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visible()
            } else {
                binding.progressBar.gone()
            }
        }

        viewModel.mountains.observe(this) { mountains ->
            mountainsAdapter.submitList(mountains)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_mountains
    }
}

