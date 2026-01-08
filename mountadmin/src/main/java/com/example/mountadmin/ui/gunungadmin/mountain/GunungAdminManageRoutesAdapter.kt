package com.example.mountadmin.ui.gunungadmin.mountain

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mountadmin.data.model.HikingRoute
import com.example.mountadmin.databinding.ItemGunungAdminManageRouteBinding

class GunungAdminManageRoutesAdapter(
    private val onEdit: (HikingRoute) -> Unit,
    private val onDelete: (HikingRoute) -> Unit,
    private val onToggleStatus: (HikingRoute) -> Unit
) : ListAdapter<HikingRoute, GunungAdminManageRoutesAdapter.VH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemGunungAdminManageRouteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding, onEdit, onDelete, onToggleStatus)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemGunungAdminManageRouteBinding,
        private val onEdit: (HikingRoute) -> Unit,
        private val onDelete: (HikingRoute) -> Unit,
        private val onToggleStatus: (HikingRoute) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(route: HikingRoute) {
            binding.tvRouteName.text = route.name
            binding.tvRouteMeta.text = "${route.difficulty} • Capacity ${route.usedCapacity}/${route.maxCapacity}"
            binding.tvRouteStatus.text = if (route.status == HikingRoute.STATUS_OPEN) "OPEN" else "CLOSED"
            binding.btnToggle.text = if (route.status == HikingRoute.STATUS_OPEN) "Close" else "Open"

            binding.btnEdit.setOnClickListener { onEdit(route) }
            binding.btnDelete.setOnClickListener { onDelete(route) }
            binding.btnToggle.setOnClickListener { onToggleStatus(route) }

            binding.root.setOnClickListener { onEdit(route) }
        }
    }

    class Diff : DiffUtil.ItemCallback<HikingRoute>() {
        override fun areItemsTheSame(oldItem: HikingRoute, newItem: HikingRoute): Boolean {
            return (oldItem.routeId.isNotBlank() && oldItem.routeId == newItem.routeId) ||
                oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: HikingRoute, newItem: HikingRoute): Boolean {
            return oldItem == newItem
        }
    }
}
