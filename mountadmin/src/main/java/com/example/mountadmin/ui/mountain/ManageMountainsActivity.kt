package com.example.mountadmin.ui.mountain

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mountadmin.R
import com.example.mountadmin.databinding.ActivityManageMountainsBinding
import com.example.mountadmin.ui.admin.ManageAdminsActivity
import com.example.mountadmin.ui.dashboard.SuperAdminDashboardActivity
import com.example.mountadmin.ui.news.ManageNewsActivity
import com.example.mountadmin.ui.settings.SettingsActivity
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ManageMountainsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageMountainsBinding
    private val viewModel: ManageMountainsViewModel by viewModels()
    private lateinit var adapter: MountainsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageMountainsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        setupBottomNavigation()
        setupClickListeners()
        observeViewModel()

        viewModel.loadMountains()
    }

    private fun setupRecyclerView() {
        adapter = MountainsAdapter(
            onEditClick = { mountain ->
                val intent = Intent(this, EditMountainActivity::class.java)
                intent.putExtra(EditMountainActivity.EXTRA_MOUNTAIN_ID, mountain.id)
                startActivity(intent)
            },
            onDeleteClick = { mountain ->
                viewModel.deleteMountain(mountain.id)
            }
        )

        binding.rvMountains.layoutManager = LinearLayoutManager(this)
        binding.rvMountains.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchMountains(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_mountains

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, SuperAdminDashboardActivity::class.java))
                    true
                }
                R.id.nav_mountains -> true
                R.id.nav_admin -> {
                    startActivity(Intent(this, ManageAdminsActivity::class.java))
                    true
                }
                R.id.nav_news -> {
                    startActivity(Intent(this, ManageNewsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        // Tap = add mountain (as before)
        binding.fabAddMountain.setOnClickListener {
            startActivity(Intent(this, AddMountainActivity::class.java))
        }

        // Long press = seed demo dataset into Firestore
        binding.fabAddMountain.setOnLongClickListener {
            val seedCount = com.example.mountadmin.data.seed.MountainSeedData.build().size
            MaterialAlertDialogBuilder(this, R.style.Theme_MountAdmin_AlertDialog)
                .setTitle("Seed data gunung")
                .setMessage(
                    "Ini akan mengisi $seedCount data gunung populer ke Firestore collection 'mountains'. " +
                        "Jika koleksi sudah berisi data, proses seed akan dilewati kecuali kamu paksa overwrite."
                )
                .setPositiveButton("Seed") { _, _ ->
                    viewModel.seedMountains(force = false)
                }
                .setNeutralButton("Force Overwrite") { _, _ ->
                    viewModel.seedMountains(force = true)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            true
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visible()
            } else {
                binding.progressBar.gone()
            }
        }

        viewModel.mountains.observe(this) { mountains ->
            adapter.submitList(mountains)

            if (mountains.isEmpty()) {
                binding.tvEmpty.visible()
                binding.rvMountains.gone()
            } else {
                binding.tvEmpty.gone()
                binding.rvMountains.visible()
            }
        }

        viewModel.seedSuccess.observe(this) { success ->
            if (success == true) {
                Toast.makeText(this, "Seed berhasil (data masuk Firestore)", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_mountains
        viewModel.loadMountains()
    }
}
