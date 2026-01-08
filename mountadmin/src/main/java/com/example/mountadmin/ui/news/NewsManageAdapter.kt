package com.example.mountadmin.ui.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mountadmin.R
import com.example.mountadmin.data.model.News
import com.example.mountadmin.databinding.ItemNewsManageBinding
import java.text.SimpleDateFormat
import java.util.Locale

class NewsManageAdapter(
    private val onEditClick: (News) -> Unit,
    private val onDeleteClick: (News) -> Unit
) : ListAdapter<News, NewsManageAdapter.NewsViewHolder>(NewsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsManageBinding.inflate(
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
        private val binding: ItemNewsManageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(news: News) {
            binding.tvTitle.text = news.title

            // Format category and date
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val formattedDate = try {
                dateFormat.format(news.createdAt.toDate())
            } catch (_: Exception) {
                "Recently"
            }
            binding.tvCategoryDate.text = buildString {
                append(news.category)
                append(" • ")
                append(formattedDate)
            }

            // Author
            binding.tvAuthor.text = news.authorName.ifEmpty { "Admin Berita" }

            // Load thumbnail or show placeholder
            if (news.coverImageUrl.isNotEmpty()) {
                binding.ivThumbnail.setImageResource(R.drawable.ic_mountain_placeholder)
            } else {
                binding.ivThumbnail.setImageResource(R.drawable.ic_mountain_placeholder)
            }

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

