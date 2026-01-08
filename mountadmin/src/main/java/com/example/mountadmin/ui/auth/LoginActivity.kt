package com.example.mountadmin.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mountadmin.R
import com.example.mountadmin.data.model.Admin
import com.example.mountadmin.databinding.ActivityLoginBinding
import com.example.mountadmin.ui.dashboard.SuperAdminDashboardActivity
import com.example.mountadmin.ui.gunungadmin.dashboard.GunungAdminDashboardActivity
import com.example.mountadmin.ui.news.NewsAdminDashboardActivity
import com.example.mountadmin.utils.ValidationUtils
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
        viewModel.checkCurrentAdmin()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.error = getString(R.string.error_password_short)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.progressBar.visible()
                    binding.btnLogin.text = ""
                    binding.btnLogin.isEnabled = false
                }
                is LoginState.Success -> {
                    binding.progressBar.gone()
                    binding.btnLogin.text = getString(R.string.login)
                    binding.btnLogin.isEnabled = true
                    navigateToDashboard(state.admin)
                }
                is LoginState.Error -> {
                    binding.progressBar.gone()
                    binding.btnLogin.text = getString(R.string.login)
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToDashboard(admin: Admin) {
        val intent = when (admin.role) {
            Admin.ROLE_SUPER_ADMIN -> {
                Intent(this, SuperAdminDashboardActivity::class.java)
            }
            Admin.ROLE_MOUNTAIN_ADMIN -> {
                Intent(this, GunungAdminDashboardActivity::class.java).apply {
                    putExtra(GunungAdminDashboardActivity.EXTRA_MOUNTAIN_ID, admin.assignedMountainId)
                    putExtra(GunungAdminDashboardActivity.EXTRA_ADMIN_NAME, admin.fullName)
                }
            }
            Admin.ROLE_NEWS_ADMIN -> {
                Intent(this, NewsAdminDashboardActivity::class.java).apply {
                    putExtra(NewsAdminDashboardActivity.EXTRA_ADMIN_NAME, admin.fullName)
                }
            }
            else -> {
                // Default fallback to super admin dashboard
                Intent(this, SuperAdminDashboardActivity::class.java)
            }
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

