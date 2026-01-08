package com.example.mountadmin.ui.mountain

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.mountadmin.R
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
            binding.chipElevation.text = "${mountain.elevation} m"

            // Image loading rules: HTTPS only from Firestore
            val url = mountain.imageUrl.trim()
            Log.d("MountainsAdapter", "mountain=${mountain.id} url=${url.take(80)}")

            binding.progressImage.visibility = View.GONE

            if (url.isBlank()) {
                binding.ivMountain.setImageResource(R.drawable.ic_mountain_placeholder)
                return
            }

            if (!url.startsWith("https://")) {
                Log.w("MountainsAdapter", "Non-https imageUrl blocked for mountain=${mountain.id}")
                binding.ivMountain.setImageResource(R.drawable.ic_mountain_placeholder)
                return
            }

            binding.progressImage.visibility = View.VISIBLE

            Glide.with(binding.root)
                .load(url)
                .placeholder(R.drawable.ic_mountain_placeholder)
                .error(R.drawable.ic_mountain_placeholder)
                .centerCrop()
                .listener(object : RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<android.graphics.drawable.Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("MountainsAdapter", "Glide load failed mountain=${mountain.id} url=$url", e)
                        binding.progressImage.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable?,
                        model: Any?,
                        target: Target<android.graphics.drawable.Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressImage.visibility = View.GONE
                        return false
                    }
                })
                .into(binding.ivMountain)

            binding.btnEdit.setOnClickListener { onEditClick(mountain) }
            binding.btnDelete.setOnClickListener { onDeleteClick(mountain) }
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
