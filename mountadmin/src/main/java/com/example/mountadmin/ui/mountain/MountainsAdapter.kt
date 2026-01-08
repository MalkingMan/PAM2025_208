package com.example.mountadmin.ui.mountain

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.databinding.ItemMountainAdminBinding

class MountainsAdapter(
    private val onEditClick: (Mountain) -> Unit,
    private val onDeleteClick: (Mountain) -> Unit
) : ListAdapter<Mountain, MountainsAdapter.MountainViewHolder>(MountainDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MountainViewHolder {
        val binding = ItemMountainAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MountainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MountainViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MountainViewHolder(
        private val binding: ItemMountainAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mountain: Mountain) {
            binding.tvName.text = mountain.name
            binding.tvLocation.text = "${mountain.province}, ${mountain.country}"
            binding.tvElevation.text = "${mountain.elevation}m"

            binding.btnEdit.setOnClickListener {
                onEditClick(mountain)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(mountain)
            }
        }
    }

    class MountainDiffCallback : DiffUtil.ItemCallback<Mountain>() {
        override fun areItemsTheSame(oldItem: Mountain, newItem: Mountain): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Mountain, newItem: Mountain): Boolean {
            return oldItem == newItem
        }
    }
}

