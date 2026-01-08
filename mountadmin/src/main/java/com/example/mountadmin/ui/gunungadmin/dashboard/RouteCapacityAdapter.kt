package com.example.mountadmin.ui.gunungadmin.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mountadmin.R
import com.example.mountadmin.databinding.ItemRouteCapacityBinding

class RouteCapacityAdapter : ListAdapter<RouteCapacity, RouteCapacityAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRouteCapacityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemRouteCapacityBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RouteCapacity) {
            binding.tvRouteName.text = item.routeName
            binding.tvRouteCount.text = "${item.currentCount} / ${item.maxCapacity} Hikers"
            binding.tvRoutePercentage.text = "${item.percentage}% Full"
            binding.tvRouteStatus.text = item.status
            binding.progressRoute.progress = item.percentage

            // Set color based on percentage
            val colorRes = when {
                item.percentage >= 90 -> R.color.status_rejected
                item.percentage >= 70 -> R.color.status_pending
                else -> R.color.status_approved
            }
            binding.progressRoute.setIndicatorColor(
                binding.root.context.getColor(colorRes)
            )
            binding.tvRoutePercentage.setTextColor(
                binding.root.context.getColor(colorRes)
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RouteCapacity>() {
        override fun areItemsTheSame(oldItem: RouteCapacity, newItem: RouteCapacity): Boolean {
            return oldItem.routeName == newItem.routeName
        }

        override fun areContentsTheSame(oldItem: RouteCapacity, newItem: RouteCapacity): Boolean {
            return oldItem == newItem
        }
    }
}

