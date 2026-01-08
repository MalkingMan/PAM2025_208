package com.example.mounttrack.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mounttrack.data.model.HikingNews
import com.example.mounttrack.databinding.ItemHikingNewsBinding

class HikingNewsAdapter : ListAdapter<HikingNews, HikingNewsAdapter.NewsViewHolder>(NewsDiffCallback()) {

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
            // Image loading would go here with Glide
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

