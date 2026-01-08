package com.example.mountadmin.ui.gunungadmin.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mountadmin.R
import com.example.mountadmin.databinding.ActivityGunungAdminDashboardBinding
import com.example.mountadmin.ui.auth.LoginActivity
import com.example.mountadmin.ui.gunungadmin.mountain.GunungAdminMountainFragment
import com.example.mountadmin.ui.gunungadmin.profile.GunungAdminProfileFragment
import com.example.mountadmin.ui.gunungadmin.registration.GunungAdminRegistrationFragment
import com.google.firebase.auth.FirebaseAuth

class GunungAdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGunungAdminDashboardBinding
    private var assignedMountainId: String? = null
    private var adminName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGunungAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        assignedMountainId = intent.getStringExtra(EXTRA_MOUNTAIN_ID)
        adminName = intent.getStringExtra(EXTRA_ADMIN_NAME)

        if (assignedMountainId.isNullOrEmpty()) {
            // No mountain assigned, logout
            logout()
            return
        }

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(GunungAdminDashboardFragment.newInstance(assignedMountainId!!, adminName ?: "Admin"))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_dashboard -> GunungAdminDashboardFragment.newInstance(assignedMountainId!!, adminName ?: "Admin")
                R.id.nav_mountains -> GunungAdminMountainFragment.newInstance(assignedMountainId!!)
                R.id.nav_registration -> GunungAdminRegistrationFragment.newInstance(assignedMountainId!!)
                R.id.nav_profile -> GunungAdminProfileFragment.newInstance()
                else -> return@setOnItemSelectedListener false
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_MOUNTAIN_ID = "extra_mountain_id"
        const val EXTRA_ADMIN_NAME = "extra_admin_name"
    }
}

