package com.example.mountadmin.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mountadmin.R
import com.example.mountadmin.data.model.Admin
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.databinding.ActivityEditAdminBinding
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible

class EditAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAdminBinding
    private val viewModel: EditAdminViewModel by viewModels()

    private var adminId: String = ""
    private var currentAdmin: Admin? = null
    private var selectedRole: String = ""
    private var selectedMountainId: String? = null
    private var mountains: List<Mountain> = emptyList()

    companion object {
        const val EXTRA_ADMIN_ID = "extra_admin_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminId = intent.getStringExtra(EXTRA_ADMIN_ID) ?: run {
            finish()
            return
        }

        setupClickListeners()
        setupRoleDropdown()
        observeViewModel()

        viewModel.loadMountains()
        viewModel.loadAdmin(adminId)
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                updateAdmin()
            }
        }

        binding.btnRemove.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
            binding.tvStatusActive.setTextColor(
                getColor(if (isChecked) R.color.primary else R.color.text_secondary)
            )
            binding.tvStatusDisabled.setTextColor(
                getColor(if (isChecked) R.color.text_secondary else R.color.status_disabled)
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

        // Pre-select mountain if admin already has one assigned
        currentAdmin?.assignedMountainId?.let { mountainId ->
            val mountain = mountains.find { it.id == mountainId }
            mountain?.let {
                binding.actvMountain.setText(it.name, false)
                selectedMountainId = it.id
            }
        }
    }

    private fun populateAdminData(admin: Admin) {
        currentAdmin = admin
        selectedRole = admin.role
        selectedMountainId = admin.assignedMountainId

        binding.etFullName.setText(admin.fullName)
        binding.etEmail.setText(admin.email)
        binding.etPhone.setText(admin.phone)

        // Set role dropdown
        val roleText = when (admin.role) {
            Admin.ROLE_SUPER_ADMIN -> getString(R.string.super_admin)
            Admin.ROLE_MOUNTAIN_ADMIN -> getString(R.string.mountain_admin)
            Admin.ROLE_NEWS_ADMIN -> getString(R.string.news_admin)
            else -> ""
        }
        binding.actvRole.setText(roleText, false)

        // Show/hide mountain selection
        if (admin.role == Admin.ROLE_MOUNTAIN_ADMIN) {
            binding.tvMountainLabel.visible()
            binding.tilMountain.visible()
        } else {
            binding.tvMountainLabel.gone()
            binding.tilMountain.gone()
        }

        // Set status
        val isActive = admin.status == Admin.STATUS_ACTIVE
        binding.switchStatus.isChecked = isActive
        binding.tvStatusActive.setTextColor(
            getColor(if (isActive) R.color.primary else R.color.text_secondary)
        )
        binding.tvStatusDisabled.setTextColor(
            getColor(if (isActive) R.color.text_secondary else R.color.status_disabled)
        )

        // Pre-select mountain if available
        if (mountains.isNotEmpty()) {
            setupMountainDropdown(mountains)
        }
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

    private fun updateAdmin() {
        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val status = if (binding.switchStatus.isChecked) Admin.STATUS_ACTIVE else Admin.STATUS_DISABLED

        currentAdmin?.let { admin ->
            val updatedAdmin = admin.copy(
                fullName = fullName,
                phone = phone,
                role = selectedRole,
                assignedMountainId = if (selectedRole == Admin.ROLE_MOUNTAIN_ADMIN) selectedMountainId else null,
                status = status
            )

            viewModel.updateAdmin(updatedAdmin)
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.remove_admin))
            .setMessage(getString(R.string.confirm_delete_admin))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteAdmin(adminId)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
            binding.btnRemove.isEnabled = !isLoading
        }

        viewModel.admin.observe(this) { admin ->
            admin?.let { populateAdminData(it) }
        }

        viewModel.mountains.observe(this) { mountainList ->
            mountains = mountainList
            setupMountainDropdown(mountainList)
        }

        viewModel.updateSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.success_admin_updated), Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.deleteSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.success_admin_deleted), Toast.LENGTH_SHORT).show()
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

