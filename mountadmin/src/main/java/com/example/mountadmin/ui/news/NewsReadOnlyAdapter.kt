package com.example.mountadmin.ui.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mountadmin.R
import com.example.mountadmin.data.model.News
import com.example.mountadmin.databinding.ItemNewsReadOnlyBinding
import java.text.SimpleDateFormat
import java.util.Locale

class NewsReadOnlyAdapter(
    private val onItemClick: (News) -> Unit
) : ListAdapter<News, NewsReadOnlyAdapter.NewsViewHolder>(NewsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsReadOnlyBinding.inflate(
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
        private val binding: ItemNewsReadOnlyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(news: News) {
            binding.tvTitle.text = news.title
            binding.tvDescription.text = news.content

            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val formattedDate = try {
                "Published on: ${dateFormat.format(news.createdAt.toDate())}"
            } catch (_: Exception) {
                "Published recently"
            }
            binding.tvDate.text = formattedDate

            // Load cover image or show placeholder
            if (news.coverImageUrl.isNotEmpty()) {
                // Use Glide or Coil to load image
                binding.ivCover.setImageResource(R.drawable.ic_mountain_placeholder)
            } else {
                binding.ivCover.setImageResource(R.drawable.ic_mountain_placeholder)
            }

            binding.root.setOnClickListener {
                onItemClick(news)
            }

            binding.ivArrow.setOnClickListener {
                onItemClick(news)
            }
        }
    }

    class NewsDiffCallback : DiffUtil.ItemCallback<News>() {
        override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem == newItem
        }
    }
}

