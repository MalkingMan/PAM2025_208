package com.example.mountadmin.ui.mountain

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mountadmin.R
import com.example.mountadmin.data.model.HikingRoute
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.databinding.ActivityAddMountainBinding
import com.example.mountadmin.databinding.DialogAddRouteBinding
import com.example.mountadmin.utils.ImageEncodeUtils
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddMountainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMountainBinding
    private val viewModel: AddMountainViewModel by viewModels()
    private lateinit var routesAdapter: RoutesAdapter
    private val routes = mutableListOf<HikingRoute>()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            displaySelectedImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMountainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        routesAdapter = RoutesAdapter { route ->
            routes.remove(route)
            routesAdapter.submitList(routes.toList())
            updateRoutesVisibility()
        }

        binding.rvRoutes.layoutManager = LinearLayoutManager(this)
        binding.rvRoutes.adapter = routesAdapter
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.cardUploadPhoto.setOnClickListener {
            openImagePicker()
        }

        binding.btnAddRoute.setOnClickListener {
            showAddRouteDialog()
        }

        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                saveMountain()
            }
        }
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun displaySelectedImage(uri: Uri) {
        binding.ivMountainPhoto.setImageURI(uri)
        binding.ivMountainPhoto.visible()
        binding.layoutUploadPlaceholder.gone()
    }

    private fun showAddRouteDialog() {
        val dialogBinding = DialogAddRouteBinding.inflate(LayoutInflater.from(this))

        val difficulties = listOf("Easy", "Moderate", "Hard", "Expert")
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, difficulties)
        dialogBinding.actvDifficulty.setAdapter(adapter)
        dialogBinding.actvDifficulty.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup)

        MaterialAlertDialogBuilder(this, R.style.Theme_MountAdmin_AlertDialog)
            .setTitle(getString(R.string.add_route))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.add_route)) { _, _ ->
                val routeName = dialogBinding.etRouteName.text.toString().trim()
                val difficulty = dialogBinding.actvDifficulty.text.toString()

                if (routeName.isNotEmpty() && difficulty.isNotEmpty()) {
                    val route = HikingRoute(
                        name = routeName,
                        difficulty = difficulty
                    )
                    routes.add(route)
                    routesAdapter.submitList(routes.toList())
                    updateRoutesVisibility()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateRoutesVisibility() {
        if (routes.isEmpty()) {
            binding.tvNoRoutes.visible()
            binding.rvRoutes.gone()
        } else {
            binding.tvNoRoutes.gone()
            binding.rvRoutes.visible()
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        val name = binding.etName.text.toString().trim()
        val province = binding.etProvince.text.toString().trim()
        val country = binding.etCountry.text.toString().trim()
        val elevation = binding.etElevation.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Mountain name is required"
            isValid = false
        }

        if (province.isEmpty()) {
            binding.etProvince.error = "Province is required"
            isValid = false
        }

        if (country.isEmpty()) {
            binding.etCountry.error = "Country is required"
            isValid = false
        }

        if (elevation.isEmpty()) {
            binding.etElevation.error = "Elevation is required"
            isValid = false
        }

        return isValid
    }

    private fun saveMountain() {
        val base64Image = selectedImageUri?.let { uri ->
            ImageEncodeUtils.uriToBase64(this, uri)
        } ?: ""

        val mountain = Mountain(
            name = binding.etName.text.toString().trim(),
            province = binding.etProvince.text.toString().trim(),
            country = binding.etCountry.text.toString().trim(),
            elevation = binding.etElevation.text.toString().toIntOrNull() ?: 0,
            description = binding.etDescription.text.toString().trim(),
            routes = routes.toList(),
            isActive = true,
            imageUrl = base64Image
        )

        viewModel.saveMountain(mountain)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.success_mountain_saved), Toast.LENGTH_SHORT).show()
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
