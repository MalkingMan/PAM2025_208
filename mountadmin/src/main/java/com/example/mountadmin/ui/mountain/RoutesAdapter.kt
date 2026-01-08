package com.example.mountadmin.ui.mountain

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mountadmin.data.model.HikingRoute
import com.example.mountadmin.databinding.ItemRouteBinding

class RoutesAdapter(
    private val onDeleteClick: (HikingRoute) -> Unit
) : ListAdapter<HikingRoute, RoutesAdapter.RouteViewHolder>(RouteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = ItemRouteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RouteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RouteViewHolder(
        private val binding: ItemRouteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(route: HikingRoute) {
            binding.tvRouteName.text = route.name
            binding.tvDifficulty.text = route.difficulty
            binding.tvCapacity.text = "Capacity: ${route.maxCapacity}"

            binding.btnDelete.setOnClickListener {
                onDeleteClick(route)
            }
        }
    }

    class RouteDiffCallback : DiffUtil.ItemCallback<HikingRoute>() {
        override fun areItemsTheSame(oldItem: HikingRoute, newItem: HikingRoute): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: HikingRoute, newItem: HikingRoute): Boolean {
            return oldItem == newItem
        }
    }
}

