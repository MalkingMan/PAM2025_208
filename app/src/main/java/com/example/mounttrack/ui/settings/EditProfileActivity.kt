package com.example.mounttrack.ui.settings

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mounttrack.R
import com.example.mounttrack.data.firebase.FirebaseHelper
import com.example.mounttrack.databinding.ActivityEditProfileBinding
import com.example.mounttrack.utils.gone
import com.example.mounttrack.utils.visible
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModels()

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val uiScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()

        // Load user profile
        viewModel.loadUserProfile()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.etDob.setOnClickListener {
            showDatePicker()
        }

        binding.tilDob.setEndIconOnClickListener {
            showDatePicker()
        }

        binding.btnUpdate.setOnClickListener {
            if (validatePasswordSection()) {
                if (shouldChangePassword()) {
                    changePasswordThenUpdateProfile()
                } else {
                    validateAndUpdate()
                }
            }
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                binding.etDob.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(this) { user ->
            user?.let {
                binding.etFullName.setText(it.fullName)
                binding.etPhone.setText(it.phone)
                binding.etDob.setText(it.dob)
                binding.etAddress.setText(it.address)
                binding.etEmail.setText(it.email)
            }
        }

        viewModel.updateState.observe(this) { state ->
            when (state) {
                is UpdateState.Loading -> {
                    binding.progressBar.visible()
                    binding.btnUpdate.text = ""
                    binding.btnUpdate.isEnabled = false
                }
                is UpdateState.Success -> {
                    binding.progressBar.gone()
                    binding.btnUpdate.text = getString(R.string.update_profile)
                    binding.btnUpdate.isEnabled = true
                    Toast.makeText(
                        this,
                        getString(R.string.profile_updated),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                is UpdateState.Error -> {
                    binding.progressBar.gone()
                    binding.btnUpdate.text = getString(R.string.update_profile)
                    binding.btnUpdate.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun shouldChangePassword(): Boolean {
        val current = binding.etCurrentPassword.text?.toString().orEmpty().trim()
        val newPass = binding.etNewPassword.text?.toString().orEmpty().trim()
        val confirm = binding.etConfirmNewPassword.text?.toString().orEmpty().trim()
        return current.isNotEmpty() || newPass.isNotEmpty() || confirm.isNotEmpty()
    }

    private fun validatePasswordSection(): Boolean {
        // also validate existing profile fields (fullName) via validateAndUpdate() path
        var isValid = true

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
                binding.tilConfirmNewPassword.error = getString(R.string.confirm_password)
                isValid = false
            } else if (confirm != newPass) {
                binding.tilConfirmNewPassword.error = getString(R.string.error_passwords_not_match)
                isValid = false
            } else {
                binding.tilConfirmNewPassword.error = null
            }
        } else {
            binding.tilCurrentPassword.error = null
            binding.tilNewPassword.error = null
            binding.tilConfirmNewPassword.error = null
        }

        return isValid
    }

    private fun changePasswordThenUpdateProfile() {
        val user = FirebaseHelper.currentUser
        val email = user?.email

        if (user == null || email.isNullOrBlank()) {
            Toast.makeText(this, getString(R.string.error_reauth_required), Toast.LENGTH_LONG).show()
            return
        }

        // NOTE: If user logged in with Google, reauth with password won't work.
        // We'll show a clear message.
        val providerIds = user.providerData.mapNotNull { it?.providerId }
        val hasPasswordProvider = providerIds.contains("password")
        if (!hasPasswordProvider) {
            Toast.makeText(
                this,
                getString(R.string.error_reauth_required),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val current = binding.etCurrentPassword.text?.toString().orEmpty().trim()
        val newPass = binding.etNewPassword.text?.toString().orEmpty().trim()

        uiScope.launch {
            try {
                binding.progressBar.visible()
                binding.btnUpdate.text = ""
                binding.btnUpdate.isEnabled = false

                val credential = EmailAuthProvider.getCredential(email, current)
                user.reauthenticate(credential).await()
                user.updatePassword(newPass).await()

                // Clear fields after success
                binding.etCurrentPassword.setText("")
                binding.etNewPassword.setText("")
                binding.etConfirmNewPassword.setText("")

                Toast.makeText(this@EditProfileActivity, getString(R.string.success_password_updated), Toast.LENGTH_SHORT).show()

                // Continue normal profile update
                validateAndUpdate()
            } catch (e: Exception) {
                binding.progressBar.gone()
                binding.btnUpdate.text = getString(R.string.update_profile)
                binding.btnUpdate.isEnabled = true
                Toast.makeText(
                    this@EditProfileActivity,
                    e.message ?: getString(R.string.error_password_update_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun validateAndUpdate() {
        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val dob = binding.etDob.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()

        // Validate full name
        if (fullName.isEmpty()) {
            binding.tilFullName.error = getString(R.string.error_empty_name)
            return
        }
        binding.tilFullName.error = null

        // Update profile
        viewModel.updateProfile(fullName, phone, dob, address)
    }

    override fun onDestroy() {
        super.onDestroy()
        uiScope.cancel()
    }
}
