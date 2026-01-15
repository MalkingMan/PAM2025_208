package com.example.mountadmin.ui.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mountadmin.R
import com.example.mountadmin.databinding.ActivitySuperAdminDashboardBinding
import com.example.mountadmin.ui.admin.ManageAdminsActivity
import com.example.mountadmin.ui.mountain.ManageMountainsActivity
import com.example.mountadmin.ui.news.ManageNewsActivity
import com.example.mountadmin.ui.settings.SettingsActivity
import com.example.mountadmin.utils.formatNumber
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.snackbar.Snackbar
import kotlin.math.ceil
import kotlin.math.max

class SuperAdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuperAdminDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var weatherAdapter: WeatherAlertAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuperAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        setupChart()
        setupWeatherAlerts()
        observeViewModel()
        viewModel.loadDashboardData()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_mountains -> {
                    startActivity(Intent(this, ManageMountainsActivity::class.java))
                    true
                }
                R.id.nav_admin -> {
                    startActivity(Intent(this, ManageAdminsActivity::class.java))
                    true
                }
                R.id.nav_news -> {
                    startActivity(Intent(this, ManageNewsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupChart() {
        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setFitBars(true)
            animateY(1000)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.WHITE
                textSize = 10f
                granularity = 1f
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#333333")
                textColor = Color.WHITE
                textSize = 10f
                axisMinimum = 0f
                granularity = 2f
                valueFormatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
                        return value.toInt().toString()
                    }
                }
            }

            axisRight.isEnabled = false
        }
    }

    private fun setupWeatherAlerts() {
        weatherAdapter = WeatherAlertAdapter()

        binding.rvWeatherAlerts.apply {
            layoutManager = LinearLayoutManager(this@SuperAdminDashboardActivity)
            adapter = weatherAdapter
            setHasFixedSize(false)
        }

        // Refresh button click
        binding.btnRefreshWeather.setOnClickListener {
            viewModel.refreshWeather()
            Snackbar.make(binding.root, "Refreshing weather data...", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) binding.progressBar.visible() else binding.progressBar.gone()
        }

        viewModel.dashboardStats.observe(this) { stats ->
            binding.tvTotalHikers.text = stats.totalHikers.formatNumber()
            binding.tvRegisteredThisMonth.text = stats.registeredThisMonth.formatNumber()
            binding.tvMostClimbedMountain.text = stats.mostClimbedMountain

            updateChart(stats.mountainRegistrations.map {
                Pair(it.mountainName, it.count.toFloat())
            })
        }

        // Weather observers
        viewModel.isWeatherLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressWeather.visible()
                binding.tvWeatherError.gone()
            } else {
                binding.progressWeather.gone()
            }
        }

        viewModel.weatherAlerts.observe(this) { weatherList ->
            if (weatherList.isEmpty()) {
                binding.rvWeatherAlerts.gone()
                binding.tvNoWeather.visible()
            } else {
                binding.rvWeatherAlerts.visible()
                binding.tvNoWeather.gone()
                weatherAdapter.submitList(weatherList)
            }
        }

        viewModel.weatherError.observe(this) { error ->
            error?.let {
                binding.tvWeatherError.text = it
                binding.tvWeatherError.visible()
                binding.rvWeatherAlerts.gone()
                binding.tvNoWeather.gone()
            } ?: binding.tvWeatherError.gone()
        }
    }

    private fun updateChart(data: List<Pair<String, Float>>) {
        if (data.isEmpty()) return

        val entries = data.mapIndexed { index, (_, value) ->
            BarEntry(index.toFloat(), value)
        }

        val labels = data.map { it.first }

        val dataSet = BarDataSet(entries, "Registrations").apply {
            color = ContextCompat.getColor(this@SuperAdminDashboardActivity, R.color.primary)
            setDrawValues(false)
        }

        val maxValue = data.maxOfOrNull { it.second } ?: 0f
        val axisMax = max(2f, ceil(maxValue / 2f) * 2f)

        binding.barChart.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            axisLeft.axisMaximum = axisMax
            axisLeft.setLabelCount((axisMax / 2f).toInt() + 1, true)
            this.data = BarData(dataSet)
            invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }
}
