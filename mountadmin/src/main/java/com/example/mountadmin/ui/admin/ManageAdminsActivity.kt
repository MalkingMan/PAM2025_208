package com.example.mountadmin.ui.admin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mountadmin.R
import com.example.mountadmin.data.model.Admin
import com.example.mountadmin.databinding.ActivityManageAdminsBinding
import com.example.mountadmin.ui.dashboard.SuperAdminDashboardActivity
import com.example.mountadmin.ui.mountain.ManageMountainsActivity
import com.example.mountadmin.ui.news.ManageNewsActivity
import com.example.mountadmin.ui.settings.SettingsActivity
import com.example.mountadmin.utils.Constants
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible
import com.google.android.material.tabs.TabLayout

class ManageAdminsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageAdminsBinding
    private val viewModel: ManageAdminsViewModel by viewModels()
    private lateinit var adapter: AdminsAdapter

    private val addLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) viewModel.loadAdmins()
    }
    private val editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) viewModel.loadAdmins()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageAdminsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAdapter()
        setupTabs()
        setupSearch()
        setupClickListeners()
        setupBottomNavigation()
        observeViewModel()
        viewModel.loadAdmins()
    }

    private fun setupAdapter() {
        adapter = AdminsAdapter(
            onEditClick = { editLauncher.launch(Intent(this, EditAdminActivity::class.java).putExtra(Constants.EXTRA_ADMIN_ID, it.uid)) },
            onToggleStatusClick = { showToggleStatusDialog(it) },
            onDeleteClick = { showDeleteDialog(it) }
        )
        binding.rvAdmins.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.setFilter(when (tab?.position) {
                    1 -> ManageAdminsViewModel.FilterType.MOUNTAIN_ADMIN
                    2 -> ManageAdminsViewModel.FilterType.NEWS_ADMIN
                    else -> ManageAdminsViewModel.FilterType.ALL
                })
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { viewModel.setSearchQuery(s?.toString() ?: "") }
        })
    }

    private fun setupClickListeners() {
        binding.fabAddAdmin.setOnClickListener { addLauncher.launch(Intent(this, AddAdminActivity::class.java)) }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_admin
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, SuperAdminDashboardActivity::class.java)); finish(); true }
                R.id.nav_mountains -> { startActivity(Intent(this, ManageMountainsActivity::class.java)); finish(); true }
                R.id.nav_admin -> true
                R.id.nav_news -> { startActivity(Intent(this, ManageNewsActivity::class.java)); finish(); true }
                R.id.nav_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.filteredAdmins.observe(this) { admins ->
            adapter.submitList(admins)
            if (admins.isEmpty()) { binding.tvEmpty.visible(); binding.rvAdmins.gone() }
            else { binding.tvEmpty.gone(); binding.rvAdmins.visible() }
        }
        viewModel.isLoading.observe(this) { if (it) binding.progressBar.visible() else binding.progressBar.gone() }
        viewModel.error.observe(this) { it?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show(); viewModel.clearMessages() } }
        viewModel.operationSuccess.observe(this) { it?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show(); viewModel.clearMessages() } }
    }

    private fun showToggleStatusDialog(admin: Admin) {
        val action = if (admin.isActive()) "disable" else "enable"
        AlertDialog.Builder(this)
            .setTitle("${action.replaceFirstChar { it.uppercase() }} Admin")
            .setMessage("Are you sure you want to $action ${admin.fullName}?")
            .setPositiveButton(R.string.yes) { _, _ -> viewModel.toggleAdminStatus(admin) }
            .setNegativeButton(R.string.no, null).show()
    }

    private fun showDeleteDialog(admin: Admin) {
        AlertDialog.Builder(this)
            .setTitle(R.string.remove_admin)
            .setMessage(R.string.confirm_delete_admin)
            .setPositiveButton(R.string.yes) { _, _ -> viewModel.deleteAdmin(admin) }
            .setNegativeButton(R.string.no, null).show()
    }

    override fun onResume() { super.onResume(); binding.bottomNavigation.selectedItemId = R.id.nav_admin }
}

