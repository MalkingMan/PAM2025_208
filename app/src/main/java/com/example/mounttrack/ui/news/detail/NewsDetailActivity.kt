package com.example.mounttrack.ui.news.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mounttrack.R
import com.example.mounttrack.databinding.ActivityNewsDetailBinding
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NEWS_ID = "extra_news_id"
        private const val TAG = "NewsDetailActivity"
    }

    private lateinit var binding: ActivityNewsDetailBinding
    private val viewModel: NewsDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()
        loadArticle()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadArticle() {
        val newsId = intent.getStringExtra(EXTRA_NEWS_ID)

        if (newsId.isNullOrEmpty()) {
            Log.e(TAG, "News ID is null or empty")
            showError("Invalid article")
            return
        }

        Log.d(TAG, "Loading article with ID: $newsId")
        viewModel.loadArticle(newsId)
    }

    private fun setupObservers() {
        viewModel.article.observe(this) { news ->
            if (news != null) {
                Log.d(TAG, "Article loaded: ${news.title}")
                displayArticle(news)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                showError(error)
            }
        }
    }

    private fun displayArticle(news: com.example.mounttrack.data.model.HikingNews) {
        // Category badge
        binding.tvCategory.text = news.category
        binding.tvCategory.setBackgroundResource(getCategoryColor(news.category))

        // Title
        binding.tvTitle.text = news.title

        // Date
        binding.tvDate.text = formatDate(news.createdAt)

        // Author info
        if (news.authorName.isNotEmpty()) {
            binding.tvAuthor.visibility = View.VISIBLE
            binding.tvAuthor.text = "By ${news.authorName}"
        } else {
            binding.tvAuthor.visibility = View.GONE
        }

        // Content
        binding.tvContent.text = news.content

        // Tags (if available)
        if (news.tags.isNotEmpty()) {
            binding.tvTags.visibility = View.VISIBLE
            binding.tvTags.text = news.tags.split(",").joinToString(" • ") { it.trim() }
        } else {
            binding.tvTags.visibility = View.GONE
        }

        // Cover image placeholder (can be enhanced with image loading library)
        binding.ivCover.setImageResource(R.drawable.placeholder_mountain)
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

    private fun formatDate(timestamp: Timestamp?): String {
        if (timestamp == null) return ""

        val date = Date(timestamp.seconds * 1000)
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return "${dateFormat.format(date)} at ${timeFormat.format(date)}"
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.contentLayout.visibility = View.GONE
        // You can add an error TextView to the layout if needed
        Log.e(TAG, "Error: $message")
    }
}

