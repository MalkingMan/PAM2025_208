package com.example.mounttrack.ui.news

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.mounttrack.R
import com.example.mounttrack.data.model.HikingNews
import com.example.mounttrack.utils.ImageDecodeUtils
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsListAdapter(
    private val onItemClick: (HikingNews) -> Unit
) : ListAdapter<HikingNews, NewsListAdapter.NewsViewHolder>(NewsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news_card, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivNewsThumbnail: ImageView = itemView.findViewById(R.id.ivNewsThumbnail)
        private val tvNewsCategory: TextView = itemView.findViewById(R.id.tvNewsCategory)
        private val tvNewsTitle: TextView = itemView.findViewById(R.id.tvNewsTitle)
        private val tvNewsDescription: TextView = itemView.findViewById(R.id.tvNewsDescription)
        private val tvNewsTime: TextView = itemView.findViewById(R.id.tvNewsTime)

        fun bind(news: HikingNews) {
            tvNewsCategory.text = news.category
            tvNewsTitle.text = news.title
            tvNewsDescription.text = news.description
            tvNewsTime.text = formatTimeAgo(news.createdAt)

            val imageValue = news.coverImageUrl
            Log.d("NewsListAdapter", "Loading news image len=${imageValue.length} id=${news.actualId}")

            if (ImageDecodeUtils.isLikelyBase64Image(imageValue)) {
                val bitmap = ImageDecodeUtils.decodeBase64ToBitmap(imageValue)
                if (bitmap != null) {
                    ivNewsThumbnail.setImageBitmap(bitmap)
                } else {
                    ivNewsThumbnail.setImageResource(R.drawable.placeholder_mountain)
                }
            } else if (imageValue.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(imageValue)
                    .placeholder(R.drawable.placeholder_mountain)
                    .error(R.drawable.placeholder_mountain)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivNewsThumbnail)
            } else {
                ivNewsThumbnail.setImageResource(R.drawable.placeholder_mountain)
            }

            // Set category badge color
            val categoryColor = getCategoryColor(news.category)
            tvNewsCategory.setBackgroundResource(categoryColor)

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(news)
            }
        }

        private fun getCategoryColor(category: String): Int {
            return when (category.uppercase()) {
                "SAFETY" -> R.drawable.bg_category_safety
                "TRAIL UPDATE", "TRAIL UPDATES" -> R.drawable.bg_category_trail
                "EVENT", "EVENTS" -> R.drawable.bg_category_event
                "WEATHER" -> R.drawable.bg_category_weather
                else -> R.drawable.bg_category_default
            }
        }

        private fun formatTimeAgo(timestamp: Timestamp?): String {
            if (timestamp == null) return ""

            val now = System.currentTimeMillis()
            val diff = now - (timestamp.seconds * 1000)

            return when {
                diff < 60 * 1000 -> "Just now"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} mins ago"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hrs ago"
                diff < 7 * 24 * 60 * 60 * 1000 -> {
                    val days = diff / (24 * 60 * 60 * 1000)
                    if (days == 1L) "Yesterday" else "$days days ago"
                }
                else -> {
                    val date = Date(timestamp.seconds * 1000)
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
                }
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
