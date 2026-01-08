package com.example.mountadmin.ui.gunungadmin.mountain

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mountadmin.data.model.HikingRoute
import com.example.mountadmin.databinding.ItemMountainRouteBinding

class MountainRoutesAdapter : ListAdapter<HikingRoute, MountainRoutesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMountainRouteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemMountainRouteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HikingRoute) {
            binding.tvRouteName.text = item.name

            val statusLabel = if (item.status == HikingRoute.STATUS_OPEN) "OPEN" else "CLOSED"
            val capLabel = "${item.usedCapacity}/${item.maxCapacity}"

            binding.tvRouteDifficulty.text = "${item.difficulty} • $statusLabel"
            binding.tvRouteDuration.text = "Capacity: $capLabel"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HikingRoute>() {
        override fun areItemsTheSame(oldItem: HikingRoute, newItem: HikingRoute): Boolean {
            return oldItem.routeId == newItem.routeId
        }

        override fun areContentsTheSame(oldItem: HikingRoute, newItem: HikingRoute): Boolean {
            return oldItem == newItem
        }
    }
}
