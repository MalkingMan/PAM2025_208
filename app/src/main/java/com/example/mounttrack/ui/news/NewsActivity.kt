package com.example.mounttrack.ui.news

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mounttrack.R
import com.example.mounttrack.ui.dashboard.DashboardActivity
import com.example.mounttrack.ui.mountains.MountainsActivity
import com.example.mounttrack.ui.news.detail.NewsDetailActivity
import com.example.mounttrack.ui.settings.SettingsActivity
import com.example.mounttrack.ui.status.StatusActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class NewsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "NewsActivity"
    }

    private val viewModel: NewsViewModel by viewModels()
    private lateinit var newsAdapter: NewsListAdapter

    // Views
    private lateinit var cardTopNews: MaterialCardView
    private lateinit var tvTopNewsCategory: TextView
    private lateinit var tvTopNewsDate: TextView
    private lateinit var tvTopNewsTitle: TextView
    private lateinit var tvTopNewsDescription: TextView
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var rvNews: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        initViews()
        setupRecyclerView()
        setupCategoryFilter()
        setupBottomNavigation()
        observeViewModel()
    }

    private fun initViews() {
        cardTopNews = findViewById(R.id.cardTopNews)
        tvTopNewsCategory = findViewById(R.id.tvTopNewsCategory)
        tvTopNewsDate = findViewById(R.id.tvTopNewsDate)
        tvTopNewsTitle = findViewById(R.id.tvTopNewsTitle)
        tvTopNewsDescription = findViewById(R.id.tvTopNewsDescription)
        chipGroupCategories = findViewById(R.id.chipGroupCategories)
        chipAll = findViewById(R.id.chipAll)
        rvNews = findViewById(R.id.rvNews)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        progressBar = findViewById(R.id.progressBar)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsListAdapter { news ->
            // Open news detail when clicked
            val intent = Intent(this, NewsDetailActivity::class.java)
            intent.putExtra(NewsDetailActivity.EXTRA_NEWS_ID, news.actualId)
            startActivity(intent)
        }
        rvNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(this@NewsActivity)
        }
    }

    private fun setupCategoryFilter() {
        chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                val category = chip?.text?.toString() ?: "All"
                Log.d(TAG, "Category selected: $category")
                viewModel.filterByCategory(category)
            }
        }

        // Select "All" by default
        chipAll.isChecked = true
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_news

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_mountains -> {
                    startActivity(Intent(this, MountainsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_news -> true
                R.id.nav_status -> {
                    startActivity(Intent(this, StatusActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.featuredNews.observe(this) { news ->
            Log.d(TAG, "Featured news: ${news?.title ?: "None"}")
            if (news != null) {
                cardTopNews.visibility = View.VISIBLE
                tvTopNewsCategory.text = news.category
                tvTopNewsDate.text = viewModel.formatDate(news.createdAt)
                tvTopNewsTitle.text = news.title
                tvTopNewsDescription.text = news.description

                // Set category badge color
                val categoryColor = getCategoryColor(news.category)
                tvTopNewsCategory.setBackgroundResource(categoryColor)

                // Set click listener for featured news
                cardTopNews.setOnClickListener {
                    val intent = Intent(this, NewsDetailActivity::class.java)
                    intent.putExtra(NewsDetailActivity.EXTRA_NEWS_ID, news.actualId)
                    startActivity(intent)
                }
            } else {
                cardTopNews.visibility = View.GONE
            }
        }

        viewModel.newsList.observe(this) { news ->
            Log.d(TAG, "========== NEWS LIST OBSERVER ==========")
            Log.d(TAG, "News list updated: ${news.size} items")

            if (news.isNotEmpty()) {
                news.forEachIndexed { index, item ->
                    Log.d(TAG, "News $index: ${item.title} - Category: ${item.category}")
                }
            }

            newsAdapter.submitList(news)
            Log.d(TAG, "Submitted ${news.size} items to adapter")

            // Show/hide empty state
            if (news.isEmpty()) {
                Log.d(TAG, "Showing empty state, hiding RecyclerView")
                tvEmptyState.visibility = View.VISIBLE
                rvNews.visibility = View.GONE
            } else {
                Log.d(TAG, "Showing RecyclerView with ${news.size} items, hiding empty state")
                tvEmptyState.visibility = View.GONE
                rvNews.visibility = View.VISIBLE
            }
            Log.d(TAG, "========================================")
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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

    override fun onResume() {
        super.onResume()
        bottomNavigation.selectedItemId = R.id.nav_news
    }
}

