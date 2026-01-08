package com.example.mountadmin.ui.news

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mountadmin.R
import com.example.mountadmin.data.seed.NewsSeeder
import com.example.mountadmin.databinding.ActivityManageNewsBinding
import com.example.mountadmin.ui.admin.ManageAdminsActivity
import com.example.mountadmin.ui.dashboard.SuperAdminDashboardActivity
import com.example.mountadmin.ui.mountain.ManageMountainsActivity
import com.example.mountadmin.ui.settings.SettingsActivity
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible
import kotlinx.coroutines.launch

class ManageNewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageNewsBinding
    private val viewModel: ManageNewsViewModel by viewModels()
    private lateinit var adapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        setupBottomNavigation()
        setupClickListeners()
        observeViewModel()

        viewModel.loadNews()
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter(
            onEditClick = { news ->
                val intent = Intent(this, EditNewsActivity::class.java)
                intent.putExtra(EditNewsActivity.EXTRA_NEWS_ID, news.id)
                startActivity(intent)
            },
            onDeleteClick = { news ->
                viewModel.deleteNews(news.id)
            }
        )

        binding.rvNews.layoutManager = LinearLayoutManager(this)
        binding.rvNews.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchNews(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_news

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, SuperAdminDashboardActivity::class.java))
                    true
                }
                R.id.nav_mountains -> {
                    startActivity(Intent(this, ManageMountainsActivity::class.java))
                    true
                }
                R.id.nav_admin -> {
                    startActivity(Intent(this, ManageAdminsActivity::class.java))
                    true
                }
                R.id.nav_news -> true
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        // Tap = add manual
        binding.fabAddNews.setOnClickListener {
            startActivity(Intent(this, AddNewsActivity::class.java))
        }

        // Long press = seed demo content
        binding.fabAddNews.setOnLongClickListener {
            showSeedNewsDialog()
            true
        }
    }

    private fun showSeedNewsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Seed demo news?")
            .setMessage(
                "Ini akan mengisi Firestore collection 'news' dengan 30 artikel demo (news_001..news_030). " +
                    "Kalau collection sudah berisi data, seeding normal akan di-skip. Kamu bisa pilih paksa untuk overwrite."
            )
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Force overwrite") { _, _ ->
                seedNews(force = true)
            }
            .setPositiveButton("Seed") { _, _ ->
                seedNews(force = false)
            }
            .show()
    }

    private fun seedNews(force: Boolean) {
        binding.progressBar.visible()
        binding.fabAddNews.isEnabled = false

        lifecycleScope.launch {
            val result = NewsSeeder.seedNews(force = force)

            result.onSuccess { inserted ->
                when {
                    inserted > 0 -> Toast.makeText(this@ManageNewsActivity, "Seeded $inserted news", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this@ManageNewsActivity, "Skip: collection 'news' sudah berisi data", Toast.LENGTH_SHORT).show()
                }
                viewModel.loadNews()
            }.onFailure { e ->
                Toast.makeText(this@ManageNewsActivity, "Seed failed: ${e.message}", Toast.LENGTH_LONG).show()
            }

            binding.fabAddNews.isEnabled = true
            binding.progressBar.gone()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visible()
            } else {
                binding.progressBar.gone()
            }
        }

        viewModel.newsList.observe(this) { newsList ->
            adapter.submitList(newsList)

            if (newsList.isEmpty()) {
                binding.tvEmpty.visible()
                binding.rvNews.gone()
            } else {
                binding.tvEmpty.gone()
                binding.rvNews.visible()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_news
        viewModel.loadNews()
    }
}
