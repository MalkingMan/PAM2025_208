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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModels()
    private var currentAdmin: Admin? = null

    private val uiScope = MainScope()

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
                // If password fields filled, change password first (requires re-auth)
                if (shouldChangePassword()) {
                    changePasswordThenSaveProfile()
                } else {
                    saveProfile()
                }
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

    private fun shouldChangePassword(): Boolean {
        val current = binding.etCurrentPassword.text?.toString().orEmpty().trim()
        val newPass = binding.etNewPassword.text?.toString().orEmpty().trim()
        val confirm = binding.etConfirmNewPassword.text?.toString().orEmpty().trim()
        return current.isNotEmpty() || newPass.isNotEmpty() || confirm.isNotEmpty()
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

        if (shouldChangePassword()) {
            val current = binding.etCurrentPassword.text?.toString().orEmpty().trim()
            val newPass = binding.etNewPassword.text?.toString().orEmpty().trim()
            val confirm = binding.etConfirmNewPassword.text?.toString().orEmpty().trim()

            if (current.isEmpty()) {
                binding.tilCurrentPassword.error = getString(R.string.error_current_password_required)
                isValid = false
            } else {
                binding.tilCurrentPassword.error = null
            }

            if (newPass.isEmpty()) {
                binding.tilNewPassword.error = getString(R.string.error_empty_password)
                isValid = false
            } else if (newPass.length < 6) {
                binding.tilNewPassword.error = getString(R.string.error_password_short)
                isValid = false
            } else {
                binding.tilNewPassword.error = null
            }

            if (confirm.isEmpty()) {
                binding.tilConfirmNewPassword.error = getString(R.string.confirm_the_password)
                isValid = false
            } else if (confirm != newPass) {
                binding.tilConfirmNewPassword.error = getString(R.string.error_passwords_not_match)
                isValid = false
            } else {
                binding.tilConfirmNewPassword.error = null
            }
        } else {
            // Clear any previous errors
            binding.tilCurrentPassword.error = null
            binding.tilNewPassword.error = null
            binding.tilConfirmNewPassword.error = null
        }

        return isValid
    }

    private fun changePasswordThenSaveProfile() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val email = user?.email

        if (user == null || email.isNullOrBlank()) {
            Toast.makeText(this, getString(R.string.error_reauth_required), Toast.LENGTH_LONG).show()
            return
        }

        val current = binding.etCurrentPassword.text?.toString().orEmpty().trim()
        val newPass = binding.etNewPassword.text?.toString().orEmpty().trim()

        uiScope.launch {
            try {
                binding.progressBar.visible()
                binding.btnSave.isEnabled = false

                val credential = EmailAuthProvider.getCredential(email, current)
                user.reauthenticate(credential).await()
                user.updatePassword(newPass).await()

                // Clear fields after success
                binding.etCurrentPassword.setText("")
                binding.etNewPassword.setText("")
                binding.etConfirmNewPassword.setText("")

                Toast.makeText(this@EditProfileActivity, getString(R.string.success_password_updated), Toast.LENGTH_SHORT).show()

                // Continue saving profile
                saveProfile()
            } catch (e: Exception) {
                binding.progressBar.gone()
                binding.btnSave.isEnabled = true
                Toast.makeText(
                    this@EditProfileActivity,
                    e.message ?: getString(R.string.error_password_update_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()
        uiScope.cancel()
    }
}
