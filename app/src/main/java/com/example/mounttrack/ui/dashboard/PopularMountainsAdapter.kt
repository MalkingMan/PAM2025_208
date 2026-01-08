package com.example.mounttrack.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.mounttrack.R
import com.example.mounttrack.data.model.Mountain
import com.example.mounttrack.databinding.ItemPopularMountainBinding
import com.example.mounttrack.utils.ImageDecodeUtils

class PopularMountainsAdapter(
    private val onMountainClick: (Mountain) -> Unit
) : ListAdapter<Mountain, PopularMountainsAdapter.MountainViewHolder>(MountainDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MountainViewHolder {
        val binding = ItemPopularMountainBinding.inflate(
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
        private val binding: ItemPopularMountainBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMountainClick(getItem(position))
                }
            }
        }

        fun bind(mountain: Mountain) {
            binding.tvMountainName.text = mountain.name
            binding.tvMountainLocation.text = mountain.location

            val imageValue = mountain.imageUrl

            if (ImageDecodeUtils.isLikelyBase64Image(imageValue)) {
                val bitmap = ImageDecodeUtils.decodeBase64ToBitmap(imageValue)
                if (bitmap != null) {
                    binding.ivMountain.setImageBitmap(bitmap)
                } else {
                    binding.ivMountain.setImageResource(R.drawable.placeholder_mountain)
                }
                return
            }

            // Load mountain image using Glide
            if (imageValue.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(imageValue)
                    .placeholder(R.drawable.placeholder_mountain)
                    .error(R.drawable.placeholder_mountain)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.ivMountain)
            } else {
                binding.ivMountain.setImageResource(R.drawable.placeholder_mountain)
            }
        }
    }

    class MountainDiffCallback : DiffUtil.ItemCallback<Mountain>() {
        override fun areItemsTheSame(oldItem: Mountain, newItem: Mountain): Boolean {
            return oldItem.mountainId == newItem.mountainId
        }

        override fun areContentsTheSame(oldItem: Mountain, newItem: Mountain): Boolean {
            return oldItem == newItem
        }
    }
}
