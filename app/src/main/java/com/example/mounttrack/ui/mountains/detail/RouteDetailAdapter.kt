package com.example.mounttrack.ui.mountains.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mounttrack.R
import com.example.mounttrack.data.model.HikingRoute

class RouteDetailAdapter(
    private val onRouteClick: (HikingRoute) -> Unit
) : ListAdapter<HikingRoute, RouteDetailAdapter.RouteViewHolder>(RouteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_route_detail, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvRouteName: TextView = itemView.findViewById(R.id.tvRouteName)
        private val tvDifficulty: TextView = itemView.findViewById(R.id.tvDifficulty)
        private val progressCapacity: ProgressBar = itemView.findViewById(R.id.progressCapacity)
        private val ivCapacityIcon: ImageView = itemView.findViewById(R.id.ivCapacityIcon)
        private val tvCapacityInfo: TextView = itemView.findViewById(R.id.tvCapacityInfo)
        private val tvRouteStatus: TextView = itemView.findViewById(R.id.tvRouteStatus)

        fun bind(route: HikingRoute) {
            val context = itemView.context

            // Route name
            tvRouteName.text = route.name

            // Difficulty badge
            tvDifficulty.text = route.difficulty
            val difficultyColor = when (route.difficulty) {
                HikingRoute.DIFFICULTY_EASY -> R.color.difficulty_easy
                HikingRoute.DIFFICULTY_MODERATE -> R.color.difficulty_moderate
                HikingRoute.DIFFICULTY_HARD, HikingRoute.DIFFICULTY_DIFFICULT -> R.color.difficulty_hard
                HikingRoute.DIFFICULTY_EXPERT -> R.color.difficulty_expert
                else -> R.color.difficulty_moderate
            }
            tvDifficulty.setTextColor(ContextCompat.getColor(context, difficultyColor))

            // Capacity
            val remaining = route.remainingCapacity
            val maxCap = route.maxCapacity
            val usedCap = route.usedCapacity
            val usagePercent = if (maxCap > 0) ((usedCap.toFloat() / maxCap) * 100).toInt() else 0

            progressCapacity.max = 100
            progressCapacity.progress = usagePercent

            when {
                route.status == HikingRoute.STATUS_CLOSED -> {
                    progressCapacity.progressDrawable = ContextCompat.getDrawable(context, R.drawable.bg_progress_capacity_danger)
                    progressCapacity.progress = 100

                    ivCapacityIcon.setImageResource(R.drawable.ic_close_small)
                    ivCapacityIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_rejected))

                    tvCapacityInfo.text = context.getString(R.string.route_closed)
                    tvCapacityInfo.setTextColor(ContextCompat.getColor(context, R.color.status_rejected))

                    tvRouteStatus.visibility = View.VISIBLE
                    tvRouteStatus.text = context.getString(R.string.closed)
                    tvRouteStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    tvRouteStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                }
                route.isFull -> {
                    progressCapacity.progressDrawable = ContextCompat.getDrawable(context, R.drawable.bg_progress_capacity_danger)
                    progressCapacity.progress = 100

                    ivCapacityIcon.setImageResource(R.drawable.ic_warning_small)
                    ivCapacityIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_rejected))

                    tvCapacityInfo.text = context.getString(R.string.route_full_format, maxCap)
                    tvCapacityInfo.setTextColor(ContextCompat.getColor(context, R.color.status_rejected))

                    tvRouteStatus.visibility = View.VISIBLE
                    tvRouteStatus.text = context.getString(R.string.full)
                    tvRouteStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    tvRouteStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                }
                remaining <= 10 && remaining > 0 -> {
                    progressCapacity.progressDrawable = ContextCompat.getDrawable(context, R.drawable.bg_progress_capacity_warning)

                    ivCapacityIcon.setImageResource(R.drawable.ic_warning_small)
                    ivCapacityIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_pending))

                    tvCapacityInfo.text = context.getString(R.string.route_limited_format, remaining, maxCap)
                    tvCapacityInfo.setTextColor(ContextCompat.getColor(context, R.color.status_pending))

                    tvRouteStatus.visibility = View.VISIBLE
                    tvRouteStatus.text = context.getString(R.string.limited)
                    tvRouteStatus.setTextColor(ContextCompat.getColor(context, R.color.status_pending))
                    tvRouteStatus.setBackgroundResource(R.drawable.bg_status_pending)
                }
                else -> {
                    progressCapacity.progressDrawable = ContextCompat.getDrawable(context, R.drawable.bg_progress_capacity)

                    ivCapacityIcon.setImageResource(R.drawable.ic_check_small)
                    ivCapacityIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_approved))

                    tvCapacityInfo.text = context.getString(R.string.route_available_format, remaining, maxCap)
                    tvCapacityInfo.setTextColor(ContextCompat.getColor(context, R.color.status_approved))

                    tvRouteStatus.visibility = View.GONE
                }
            }

            // Click listener
            itemView.setOnClickListener {
                if (!route.isFull && route.status != HikingRoute.STATUS_CLOSED) {
                    onRouteClick(route)
                }
            }

            // Disable appearance for unavailable routes
            if (route.isFull || route.status == HikingRoute.STATUS_CLOSED) {
                itemView.alpha = 0.6f
            } else {
                itemView.alpha = 1f
            }
        }
    }

    class RouteDiffCallback : DiffUtil.ItemCallback<HikingRoute>() {
        override fun areItemsTheSame(oldItem: HikingRoute, newItem: HikingRoute): Boolean {
            return oldItem.routeId == newItem.routeId
        }

        override fun areContentsTheSame(oldItem: HikingRoute, newItem: HikingRoute): Boolean {
            return oldItem == newItem
        }
    }
}
