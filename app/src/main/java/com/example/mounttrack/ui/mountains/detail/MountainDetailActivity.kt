package com.example.mounttrack.ui.mountains.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mounttrack.R
import com.example.mounttrack.data.model.HikingRoute
import com.example.mounttrack.data.model.Mountain
import com.example.mounttrack.data.model.MountainWeather
import com.example.mounttrack.data.model.WeatherStatus
import com.example.mounttrack.databinding.ActivityMountainDetailBinding
import com.example.mounttrack.ui.registration.MountainRegistrationActivity
import com.example.mounttrack.utils.ImageDecodeUtils

class MountainDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MOUNTAIN_ID = "extra_mountain_id"
        const val EXTRA_MOUNTAIN_NAME = "extra_mountain_name"
    }

    private lateinit var binding: ActivityMountainDetailBinding
    private val viewModel: MountainDetailViewModel by viewModels()
    private lateinit var routeAdapter: RouteDetailAdapter

    private var mountainId: String = ""
    private var mountainName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMountainDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mountainId = intent.getStringExtra(EXTRA_MOUNTAIN_ID) ?: ""
        mountainName = intent.getStringExtra(EXTRA_MOUNTAIN_NAME) ?: ""

        if (mountainId.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        viewModel.loadMountainDetails(mountainId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.collapsingToolbar.title = mountainName
    }

    private fun setupRecyclerView() {
        routeAdapter = RouteDetailAdapter { route ->
            navigateToRegistration(route)
        }
        binding.rvRoutes.apply {
            layoutManager = LinearLayoutManager(this@MountainDetailActivity)
            adapter = routeAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            navigateToRegistration(null)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.mountain.observe(this) { mountain ->
            mountain?.let { displayMountainInfo(it) }
        }

        viewModel.routes.observe(this) { routes ->
            routeAdapter.submitList(routes)
            binding.tvRouteCount.text = getString(R.string.route_count_format, routes.size)
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Weather observers
        viewModel.isWeatherLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.weatherOverlay.weatherOverlay.visibility = View.INVISIBLE
            }
        }

        viewModel.weather.observe(this) { weather ->
            weather?.let {
                displayWeatherOverlay(it)
            } ?: run {
                binding.weatherOverlay.weatherOverlay.visibility = View.GONE
            }
        }
    }

    private fun displayWeatherOverlay(weather: MountainWeather) {
        binding.weatherOverlay.weatherOverlay.visibility = View.VISIBLE

        // Temperature
        binding.weatherOverlay.tvTemperature.text = "${weather.temperature.toInt()}°C"

        // Status text
        binding.weatherOverlay.tvWeatherStatus.text = weather.weatherDescription

        // Wind & Humidity
        binding.weatherOverlay.tvWindSpeed.text = "${weather.windSpeed.toInt()} km/h"
        binding.weatherOverlay.tvHumidity.text = "${weather.humidity}%"

        // Weather icon with color
        val iconRes = getWeatherIconRes(weather.weatherStatus)
        val colorRes = getWeatherColorRes(weather.weatherStatus)
        binding.weatherOverlay.ivWeatherIcon.setImageResource(iconRes)
        binding.weatherOverlay.ivWeatherIcon.setColorFilter(
            ContextCompat.getColor(this, colorRes)
        )
    }

    private fun getWeatherIconRes(status: WeatherStatus): Int {
        return when (status) {
            WeatherStatus.CLEAR -> R.drawable.ic_weather_clear
            WeatherStatus.CLOUDY -> R.drawable.ic_weather_cloudy
            WeatherStatus.FOG -> R.drawable.ic_weather_cloudy
            WeatherStatus.DRIZZLE -> R.drawable.ic_weather_rain
            WeatherStatus.RAIN -> R.drawable.ic_weather_rain
            WeatherStatus.HEAVY_RAIN -> R.drawable.ic_weather_rain
            WeatherStatus.SNOW -> R.drawable.ic_weather_rain
            WeatherStatus.THUNDERSTORM -> R.drawable.ic_weather_rain
            WeatherStatus.STRONG_WIND -> R.drawable.ic_wind
        }
    }

    private fun getWeatherColorRes(status: WeatherStatus): Int {
        return when (status) {
            WeatherStatus.CLEAR -> R.color.weather_clear
            WeatherStatus.CLOUDY -> R.color.weather_cloudy
            WeatherStatus.FOG -> R.color.weather_fog
            WeatherStatus.DRIZZLE -> R.color.weather_fog
            WeatherStatus.RAIN -> R.color.weather_rain
            WeatherStatus.HEAVY_RAIN -> R.color.weather_rain
            WeatherStatus.SNOW -> R.color.weather_rain
            WeatherStatus.THUNDERSTORM -> R.color.weather_storm
            WeatherStatus.STRONG_WIND -> R.color.weather_wind
        }
    }

    private fun displayMountainInfo(mountain: Mountain) {
        // Set title
        binding.collapsingToolbar.title = mountain.name
        binding.tvMountainName.text = mountain.name

        // Location
        binding.tvLocation.text = mountain.location

        // Elevation
        binding.tvElevation.text = getString(R.string.elevation_format, mountain.elevation)

        // Status
        if (mountain.isActive) {
            binding.tvStatus.text = getString(R.string.open)
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_open)
        } else {
            binding.tvStatus.text = getString(R.string.closed)
            binding.tvStatus.setBackgroundResource(R.drawable.bg_status_rejected)
        }

        // Description
        binding.tvDescription.text = mountain.description.ifEmpty {
            getString(R.string.default_mountain_description)
        }

        // Image
        loadMountainImage(mountain.imageUrl)
    }

    private fun loadMountainImage(imageUrl: String) {
        if (ImageDecodeUtils.isLikelyBase64Image(imageUrl)) {
            val bitmap = ImageDecodeUtils.decodeBase64ToBitmap(imageUrl)
            if (bitmap != null) {
                binding.ivMountainImage.setImageBitmap(bitmap)
            } else {
                binding.ivMountainImage.setImageResource(R.drawable.placeholder_mountain)
            }
        } else if (imageUrl.isNotBlank()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_mountain)
                .error(R.drawable.placeholder_mountain)
                .centerCrop()
                .into(binding.ivMountainImage)
        } else {
            binding.ivMountainImage.setImageResource(R.drawable.placeholder_mountain)
        }
    }

    private fun navigateToRegistration(selectedRoute: HikingRoute?) {
        val intent = Intent(this, MountainRegistrationActivity::class.java).apply {
            putExtra(MountainRegistrationActivity.EXTRA_MOUNTAIN_ID, mountainId)
            putExtra(MountainRegistrationActivity.EXTRA_MOUNTAIN_NAME, mountainName)
            selectedRoute?.let {
                putExtra(MountainRegistrationActivity.EXTRA_ROUTE_NAME, it.name)
                putExtra(MountainRegistrationActivity.EXTRA_ROUTE_ID, it.routeId)
            }
        }
        startActivity(intent)
    }
}
