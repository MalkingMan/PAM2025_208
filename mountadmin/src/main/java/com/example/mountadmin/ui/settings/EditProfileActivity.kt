package com.example.mountadmin.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mountadmin.R
import com.example.mountadmin.data.model.Admin
import com.example.mountadmin.databinding.ActivityEditProfileBinding
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModels()
    private var currentAdmin: Admin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()

        viewModel.loadCurrentAdmin()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                saveProfile()
            }
        }
    }

    private fun populateAdminData(admin: Admin) {
        currentAdmin = admin

        binding.etFullName.setText(admin.fullName)
        binding.etEmail.setText(admin.email)
        binding.etPhone.setText(admin.phone)

        // Email is disabled (can't be changed)
        binding.etEmail.isEnabled = false
    }

    private fun validateInput(): Boolean {
        var isValid = true

        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.tilFullName.error = getString(R.string.error_empty_name)
            isValid = false
        } else {
            binding.tilFullName.error = null
        }

        if (phone.isEmpty()) {
            binding.tilPhone.error = getString(R.string.enter_phone_number)
            isValid = false
        } else {
            binding.tilPhone.error = null
        }

        return isValid
    }

    private fun saveProfile() {
        currentAdmin?.let { admin ->
            val updatedAdmin = admin.copy(
                fullName = binding.etFullName.text.toString().trim(),
                phone = binding.etPhone.text.toString().trim()
            )

            viewModel.updateProfile(updatedAdmin)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visible()
                binding.btnSave.isEnabled = false
            } else {
                binding.progressBar.gone()
                binding.btnSave.isEnabled = true
            }
        }

        viewModel.admin.observe(this) { admin ->
            admin?.let { populateAdminData(it) }
        }

        viewModel.updateSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }
}

