package com.example.mounttrack.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mounttrack.R
import com.example.mounttrack.data.model.HikingNews
import com.example.mounttrack.databinding.ItemHikingNewsBinding
import com.example.mounttrack.utils.ImageDecodeUtils

class HikingNewsAdapter(
    private val onNewsClick: (HikingNews) -> Unit
) : ListAdapter<HikingNews, HikingNewsAdapter.NewsViewHolder>(NewsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemHikingNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NewsViewHolder(
        private val binding: ItemHikingNewsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(news: HikingNews) {
            binding.tvNewsTitle.text = news.title
            binding.tvNewsDescription.text = news.description

            // Load cover image (Base64 or URL)
            val imageValue = news.coverImageUrl

            if (ImageDecodeUtils.isLikelyBase64Image(imageValue)) {
                val bitmap = ImageDecodeUtils.decodeBase64ToBitmap(imageValue)
                if (bitmap != null) {
                    binding.ivNewsImage.setImageBitmap(bitmap)
                } else {
                    binding.ivNewsImage.setImageResource(R.drawable.placeholder_mountain)
                }
            } else if (imageValue.isNotBlank()) {
                Glide.with(binding.root.context)
                    .load(imageValue)
                    .placeholder(R.drawable.placeholder_mountain)
                    .error(R.drawable.placeholder_mountain)
                    .centerCrop()
                    .into(binding.ivNewsImage)
            } else {
                binding.ivNewsImage.setImageResource(R.drawable.placeholder_mountain)
            }

            // Click listener to open news detail
            binding.root.setOnClickListener {
                onNewsClick(news)
            }
        }
    }

    class NewsDiffCallback : DiffUtil.ItemCallback<HikingNews>() {
        override fun areItemsTheSame(oldItem: HikingNews, newItem: HikingNews): Boolean {
            return oldItem.actualId == newItem.actualId
        }

        override fun areContentsTheSame(oldItem: HikingNews, newItem: HikingNews): Boolean {
            return oldItem == newItem
        }
    }
}
