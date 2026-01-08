package com.example.mountadmin.ui.news

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mountadmin.R
import com.example.mountadmin.databinding.ActivityNewsAdminDashboardBinding

class NewsAdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsAdminDashboardBinding

    companion object {
        const val EXTRA_ADMIN_NAME = "extra_admin_name"
    }

    private var adminName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminName = intent.getStringExtra(EXTRA_ADMIN_NAME) ?: ""

        setupBottomNavigation()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(NewsListFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_news_list -> {
                    loadFragment(NewsListFragment())
                    true
                }
                R.id.nav_news_manage -> {
                    loadFragment(NewsManageFragment())
                    true
                }
                R.id.nav_news_profile -> {
                    loadFragment(NewsAdminProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}

