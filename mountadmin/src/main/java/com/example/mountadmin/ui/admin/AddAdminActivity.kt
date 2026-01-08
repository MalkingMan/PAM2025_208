package com.example.mountadmin.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mountadmin.R
import com.example.mountadmin.data.model.Admin
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.databinding.ActivityAddAdminBinding
import com.example.mountadmin.utils.ValidationUtils
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible

class AddAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAdminBinding
    private val viewModel: AddAdminViewModel by viewModels()

    private var selectedRole: String = ""
    private var selectedMountainId: String? = null
    private var mountains: List<Mountain> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupRoleDropdown()
        observeViewModel()
        viewModel.loadMountains()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnCreate.setOnClickListener {
            if (validateInput()) {
                createAdmin()
            }
        }

        binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
            binding.tvStatusLabel.text = if (isChecked) getString(R.string.active) else getString(R.string.disabled)
            binding.tvStatusLabel.setTextColor(
                getColor(if (isChecked) R.color.status_active else R.color.status_disabled)
            )
        }
    }

    private fun setupRoleDropdown() {
        val roles = listOf(
            getString(R.string.super_admin),
            getString(R.string.mountain_admin),
            getString(R.string.news_admin)
        )

        val adapter = ArrayAdapter(this, R.layout.item_dropdown, roles)
        binding.actvRole.setAdapter(adapter)
        binding.actvRole.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup)

        binding.actvRole.setOnItemClickListener { _, _, position, _ ->
            selectedRole = when (position) {
                0 -> Admin.ROLE_SUPER_ADMIN
                1 -> Admin.ROLE_MOUNTAIN_ADMIN
                2 -> Admin.ROLE_NEWS_ADMIN
                else -> ""
            }

            // Show/hide mountain selection for Mountain Admin
            if (selectedRole == Admin.ROLE_MOUNTAIN_ADMIN) {
                binding.tvMountainLabel.visible()
                binding.tilMountain.visible()
            } else {
                binding.tvMountainLabel.gone()
                binding.tilMountain.gone()
                selectedMountainId = null
            }
        }
    }

    private fun setupMountainDropdown(mountains: List<Mountain>) {
        this.mountains = mountains
        val mountainNames = mountains.map { it.name }
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, mountainNames)
        binding.actvMountain.setAdapter(adapter)
        binding.actvMountain.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup)

        binding.actvMountain.setOnItemClickListener { _, _, position, _ ->
            selectedMountainId = mountains[position].id
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (fullName.isEmpty()) {
            binding.tilFullName.error = getString(R.string.error_empty_name)
            isValid = false
        } else {
            binding.tilFullName.error = null
        }

        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (phone.isEmpty()) {
            binding.tilPhone.error = getString(R.string.enter_phone_number)
            isValid = false
        } else {
            binding.tilPhone.error = null
        }

        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.error = getString(R.string.error_password_short)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_passwords_not_match)
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        if (selectedRole.isEmpty()) {
            binding.tilRole.error = getString(R.string.error_select_role)
            isValid = false
        } else {
            binding.tilRole.error = null
        }

        if (selectedRole == Admin.ROLE_MOUNTAIN_ADMIN && selectedMountainId == null) {
            binding.tilMountain.error = getString(R.string.error_select_mountain)
            isValid = false
        } else {
            binding.tilMountain.error = null
        }

        return isValid
    }

    private fun createAdmin() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val status = if (binding.switchStatus.isChecked) Admin.STATUS_ACTIVE else Admin.STATUS_DISABLED

        val admin = Admin(
            fullName = fullName,
            email = email,
            phone = phone,
            role = selectedRole,
            assignedMountainId = selectedMountainId,
            status = status
        )

        viewModel.createAdmin(email, password, admin)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnCreate.isEnabled = !isLoading
        }

        viewModel.mountains.observe(this) { mountains ->
            setupMountainDropdown(mountains)
        }

        viewModel.createSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.success_admin_created), Toast.LENGTH_SHORT).show()
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

