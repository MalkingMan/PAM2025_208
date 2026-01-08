package com.example.mounttrack.ui.status

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mounttrack.R
import com.example.mounttrack.data.model.Registration
import com.example.mounttrack.databinding.ActivityStatusBinding
import com.example.mounttrack.ui.dashboard.DashboardActivity
import com.example.mounttrack.ui.mountains.MountainsActivity
import com.example.mounttrack.ui.news.NewsActivity
import com.example.mounttrack.ui.settings.SettingsActivity
import com.example.mounttrack.utils.DateUtils
import com.example.mounttrack.utils.gone
import com.example.mounttrack.utils.visible

class StatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatusBinding
    private val viewModel: StatusViewModel by viewModels()

    private lateinit var previousRegistrationsAdapter: PreviousRegistrationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupBottomNavigation()
        observeViewModel()

        // Load data
        viewModel.loadRegistrations()
    }

    private fun setupRecyclerView() {
        previousRegistrationsAdapter = PreviousRegistrationsAdapter()
        binding.rvPreviousRegistrations.apply {
            adapter = previousRegistrationsAdapter
            layoutManager = LinearLayoutManager(this@StatusActivity)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_status

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_mountains -> {
                    startActivity(Intent(this, MountainsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_news -> {
                    startActivity(Intent(this, NewsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_status -> true
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

        viewModel.currentRegistration.observe(this) { registration ->
            if (registration != null) {
                binding.cardCurrentRegistration.visible()
                binding.tvNoCurrentRegistration.gone()
                bindCurrentRegistration(registration)
            } else {
                binding.cardCurrentRegistration.gone()
                binding.tvNoCurrentRegistration.visible()
            }
        }

        viewModel.previousRegistrations.observe(this) { registrations ->
            if (registrations.isNotEmpty()) {
                binding.rvPreviousRegistrations.visible()
                binding.tvNoPreviousRegistrations.gone()
                previousRegistrationsAdapter.submitList(registrations)
            } else {
                binding.rvPreviousRegistrations.gone()
                binding.tvNoPreviousRegistrations.visible()
            }
        }
    }

    private fun bindCurrentRegistration(registration: Registration) {
        binding.tvCurrentMountainName.text = registration.mountainName
        binding.tvCurrentRoute.text = getString(R.string.via_route, registration.route)
        binding.tvCurrentRegistrationId.text = getString(
            R.string.registration_id,
            registration.registrationId.take(8)
        )
        binding.tvCurrentDate.text = DateUtils.formatDate(registration.createdAt)

        // Set status badge
        when (registration.status) {
            Registration.STATUS_APPROVED -> {
                binding.tvCurrentStatus.text = "● ${getString(R.string.approved)}"
                binding.tvCurrentStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.status_approved)
                )
                binding.tvCurrentStatus.setBackgroundResource(R.drawable.bg_status_approved)
            }
            Registration.STATUS_REJECTED -> {
                binding.tvCurrentStatus.text = "● ${getString(R.string.rejected)}"
                binding.tvCurrentStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.status_rejected)
                )
                binding.tvCurrentStatus.setBackgroundResource(R.drawable.bg_status_rejected)
            }
            else -> {
                binding.tvCurrentStatus.text = "● ${getString(R.string.pending)}"
                binding.tvCurrentStatus.setTextColor(
                    ContextCompat.getColor(this, R.color.status_pending)
                )
                binding.tvCurrentStatus.setBackgroundResource(R.drawable.bg_status_pending)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_status
        viewModel.loadRegistrations()
    }
}

