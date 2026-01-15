package com.example.mounttrack.ui.registration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.mounttrack.R
import com.example.mounttrack.data.model.HikingRoute

class RouteAdapter(
    context: Context,
    private val routes: List<HikingRoute>
) : ArrayAdapter<HikingRoute>(context, 0, routes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_route_dropdown, parent, false)

        val route = routes[position]

        val tvRouteName = view.findViewById<TextView>(R.id.tvRouteName)
        val tvDifficulty = view.findViewById<TextView>(R.id.tvDifficulty)
        val progressCapacity = view.findViewById<ProgressBar>(R.id.progressCapacity)
        val ivCapacityIcon = view.findViewById<ImageView>(R.id.ivCapacityIcon)
        val tvCapacityInfo = view.findViewById<TextView>(R.id.tvCapacityInfo)
        val tvRouteStatus = view.findViewById<TextView>(R.id.tvRouteStatus)

        // Route name
        tvRouteName.text = route.name

        // Set difficulty with color
        tvDifficulty.text = route.difficulty
        val difficultyColor = when (route.difficulty) {
            HikingRoute.DIFFICULTY_EASY -> R.color.difficulty_easy
            HikingRoute.DIFFICULTY_MODERATE -> R.color.difficulty_moderate
            HikingRoute.DIFFICULTY_HARD, HikingRoute.DIFFICULTY_DIFFICULT -> R.color.difficulty_hard
            HikingRoute.DIFFICULTY_EXPERT -> R.color.difficulty_expert
            else -> R.color.difficulty_moderate
        }
        tvDifficulty.setTextColor(ContextCompat.getColor(context, difficultyColor))

        // Capacity calculation
        val remaining = route.remainingCapacity
        val maxCap = route.maxCapacity
        val usedCap = route.usedCapacity
        val usagePercent = if (maxCap > 0) ((usedCap.toFloat() / maxCap) * 100).toInt() else 0

        // Set progress bar
        progressCapacity.max = 100
        progressCapacity.progress = usagePercent

        when {
            route.status == HikingRoute.STATUS_CLOSED -> {
                // Route is closed
                progressCapacity.progressDrawable = ContextCompat.getDrawable(context, R.drawable.bg_progress_capacity_danger)
                progressCapacity.progress = 100
                
                ivCapacityIcon.setImageResource(R.drawable.ic_close_small)
                ivCapacityIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_rejected))
                
                tvCapacityInfo.text = "Route temporarily closed"
                tvCapacityInfo.setTextColor(ContextCompat.getColor(context, R.color.status_rejected))
                
                // Show CLOSED badge
                tvRouteStatus.visibility = View.VISIBLE
                tvRouteStatus.text = "CLOSED"
                tvRouteStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                tvRouteStatus.setBackgroundResource(R.drawable.bg_status_rejected)
            }
            route.isFull -> {
                // Route is full
                progressCapacity.progressDrawable = ContextCompat.getDrawable(context, R.drawable.bg_progress_capacity_danger)
                progressCapacity.progress = 100
                
                ivCapacityIcon.setImageResource(R.drawable.ic_warning_small)
                ivCapacityIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_rejected))
                
                tvCapacityInfo.text = "Fully booked • 0 of $maxCap slots"
                tvCapacityInfo.setTextColor(ContextCompat.getColor(context, R.color.status_rejected))
                
                // Show FULL badge
                tvRouteStatus.visibility = View.VISIBLE
                tvRouteStatus.text = "FULL"
                tvRouteStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                tvRouteStatus.setBackgroundResource(R.drawable.bg_status_rejected)
            }
            remaining <= 10 && remaining > 0 -> {
                // Low availability (warning) - less than 10 slots
                progressCapacity.progressDrawable = ContextCompat.getDrawable(context, R.drawable.bg_progress_capacity_warning)
                
                ivCapacityIcon.setImageResource(R.drawable.ic_warning_small)
                ivCapacityIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_pending))
                
                tvCapacityInfo.text = "$remaining of $maxCap slots left • Book soon!"
                tvCapacityInfo.setTextColor(ContextCompat.getColor(context, R.color.status_pending))
                
                // Show LIMITED badge
                tvRouteStatus.visibility = View.VISIBLE
                tvRouteStatus.text = "LIMITED"
                tvRouteStatus.setTextColor(ContextCompat.getColor(context, R.color.status_pending))
                tvRouteStatus.setBackgroundResource(R.drawable.bg_status_pending)
            }
            else -> {
                // Available - plenty of slots
                progressCapacity.progressDrawable = ContextCompat.getDrawable(context, R.drawable.bg_progress_capacity)
                
                ivCapacityIcon.setImageResource(R.drawable.ic_check_small)
                ivCapacityIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_approved))
                
                tvCapacityInfo.text = "$remaining of $maxCap slots available"
                tvCapacityInfo.setTextColor(ContextCompat.getColor(context, R.color.status_approved))
                
                // Hide status badge for available routes
                tvRouteStatus.visibility = View.GONE
            }
        }

        return view
    }
}
