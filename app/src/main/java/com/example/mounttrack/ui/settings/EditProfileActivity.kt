package com.example.mounttrack.ui.settings

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mounttrack.R
import com.example.mounttrack.databinding.ActivityEditProfileBinding
import com.example.mounttrack.utils.gone
import com.example.mounttrack.utils.visible
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: EditProfileViewModel by viewModels()

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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
            validateAndUpdate()
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
}

