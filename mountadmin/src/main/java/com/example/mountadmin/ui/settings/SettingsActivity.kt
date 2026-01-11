package com.example.mountadmin.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mountadmin.R
import com.example.mountadmin.data.repository.AuthRepository
import com.example.mountadmin.databinding.ActivitySettingsBinding
import com.example.mountadmin.ui.admin.ManageAdminsActivity
import com.example.mountadmin.ui.auth.LoginActivity
import com.example.mountadmin.ui.dashboard.SuperAdminDashboardActivity
import com.example.mountadmin.ui.mountain.ManageMountainsActivity
import com.example.mountadmin.ui.news.ManageNewsActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        setupClickListeners()
        bindProfileHeader()
    }

    private fun bindProfileHeader() {
        val user = FirebaseAuth.getInstance().currentUser
        binding.tvAdminName.text = user?.displayName ?: (user?.email?.substringBefore("@") ?: "Admin")
        binding.tvAdminEmail.text = user?.email ?: ""
        binding.tvAdminRole.text = "SUPER ADMIN"
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_settings

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
                R.id.nav_news -> {
                    startActivity(Intent(this, ManageNewsActivity::class.java))
                    true
                }
                R.id.nav_settings -> true
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        binding.cardEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.cardLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.confirm_logout))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                logout()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun logout() {
        authRepository.logout()
        Toast.makeText(this, getString(R.string.success_logout), Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_settings
        bindProfileHeader()
    }
}
