package com.example.mountadmin.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mountadmin.R
import com.example.mountadmin.data.model.MountainWeather
import com.example.mountadmin.data.model.WeatherStatus
import com.example.mountadmin.databinding.ItemWeatherAlertBinding

/**
 * Adapter untuk menampilkan weather alerts di dashboard
 */
class WeatherAlertAdapter : ListAdapter<MountainWeather, WeatherAlertAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWeatherAlertBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemWeatherAlertBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(weather: MountainWeather) {
            val context = binding.root.context

            // Mountain name
            binding.tvMountainName.text = weather.mountainName

            // Weather status badge
            binding.chipWeatherStatus.text = weather.weatherStatus.displayName
            binding.chipWeatherStatus.chipBackgroundColor = ContextCompat.getColorStateList(
                context,
                getColorResId(weather.weatherStatus)
            )
            binding.chipWeatherStatus.chipIcon = ContextCompat.getDrawable(
                context,
                getWeatherIcon(weather.weatherStatus)
            )

            // Weather icon (large)
            binding.ivWeatherIcon.setImageResource(getWeatherIcon(weather.weatherStatus))
            binding.ivWeatherIcon.setColorFilter(
                ContextCompat.getColor(context, getColorResId(weather.weatherStatus))
            )

            // Weather details
            binding.tvTemperature.text = "${weather.temperature.toInt()}°C"
            binding.tvWindSpeed.text = "${weather.windSpeed.toInt()} km/h"
            binding.tvHumidity.text = "${weather.humidity}%"
        }

        private fun getColorResId(status: WeatherStatus): Int {
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

        private fun getWeatherIcon(status: WeatherStatus): Int {
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
    }

    class DiffCallback : DiffUtil.ItemCallback<MountainWeather>() {
        override fun areItemsTheSame(oldItem: MountainWeather, newItem: MountainWeather): Boolean {
            return oldItem.mountainId == newItem.mountainId
        }

        override fun areContentsTheSame(oldItem: MountainWeather, newItem: MountainWeather): Boolean {
            return oldItem == newItem
        }
    }
}
