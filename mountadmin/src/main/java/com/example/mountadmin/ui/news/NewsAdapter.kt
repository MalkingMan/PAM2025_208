package com.example.mountadmin.ui.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mountadmin.R
import com.example.mountadmin.data.model.News
import com.example.mountadmin.databinding.ItemNewsAdminBinding

class NewsAdapter(
    private val onEditClick: (News) -> Unit,
    private val onDeleteClick: (News) -> Unit
) : ListAdapter<News, NewsAdapter.NewsViewHolder>(NewsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsAdminBinding.inflate(
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
        private val binding: ItemNewsAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(news: News) {
            binding.tvTitle.text = news.title
            binding.tvCategory.text = news.category

            // Status badge
            val isPublished = news.status == News.STATUS_PUBLISHED
            binding.tvStatus.text = if (isPublished) "Published" else "Draft"
            binding.tvStatus.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isPublished) R.color.status_active else R.color.status_pending
                )
            )

            binding.btnEdit.setOnClickListener {
                onEditClick(news)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(news)
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

