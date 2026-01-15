package com.example.mounttrack.ui.registration

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mounttrack.R
import com.example.mounttrack.databinding.ActivityMountainRegistrationBinding
import com.example.mounttrack.ui.dashboard.DashboardActivity
import com.example.mounttrack.ui.mountains.MountainsActivity
import com.example.mounttrack.ui.news.NewsActivity
import com.example.mounttrack.ui.settings.SettingsActivity
import com.example.mounttrack.ui.status.StatusActivity
import com.example.mounttrack.utils.Constants
import com.example.mounttrack.utils.ImageDisplayUtils
import com.example.mounttrack.utils.ImageEncodeUtils
import com.example.mounttrack.utils.gone
import com.example.mounttrack.utils.visible
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class MountainRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMountainRegistrationBinding
    private val viewModel: RegistrationViewModel by viewModels()

    private var mountainId: String = ""
    private var mountainName: String = ""
    private var selectedRoute: String = ""
    private var selectedRouteId: String = ""  // Added for capacity tracking
    private var idCardUri: String = ""

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            handleImageSelected(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMountainRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get mountain data from intent
        mountainId = intent.getStringExtra(Constants.EXTRA_MOUNTAIN_ID) ?: ""
        mountainName = intent.getStringExtra(Constants.EXTRA_MOUNTAIN_NAME) ?: ""

        setupViews()
        setupClickListeners()
        setupBottomNavigation()
        observeViewModel()

        // Load data
        viewModel.loadUserProfile()
        viewModel.loadMountainRoutes(mountainId)
    }

    private fun setupViews() {
        // Set title with mountain name
        binding.tvTitle.text = getString(R.string.mountain_registration)
    }

    private fun setupClickListeners() {
        // Date of Birth picker
        binding.etDob.setOnClickListener {
            showDatePicker()
        }

        binding.tilDob.setEndIconOnClickListener {
            showDatePicker()
        }

        // Upload ID Card
        binding.uploadContainer.setOnClickListener {
            openImagePicker()
        }

        // Register button
        binding.btnRegister.setOnClickListener {
            validateAndSubmit()
        }

        // Cancel button
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker() {
        // Set constraints - only allow dates up to today (for date of birth)
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .setEnd(MaterialDatePicker.todayInUtcMilliseconds())

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date of Birth")
            .setSelection(calendar.timeInMillis)
            .setCalendarConstraints(constraintsBuilder.build())
            .setTheme(R.style.MountTrack_MaterialDatePicker)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            // Convert UTC to local time
            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCalendar.timeInMillis = selection

            calendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
            calendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
            calendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))

            binding.etDob.setText(dateFormat.format(calendar.time))
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun handleImageSelected(uri: Uri) {
        // NOTE: We intentionally do NOT store the content:// uri in Firestore because it cannot be
        // accessed from other devices. We encode the image to Base64 and store that string.

        val base64 = ImageEncodeUtils.uriToBase64(this, uri)
        if (base64.isNullOrBlank()) {
            Toast.makeText(this, getString(R.string.error_upload_id), Toast.LENGTH_SHORT).show()
            return
        }

        idCardUri = base64

        // Update UI text
        binding.tvUploadText.text = getString(R.string.id_card_uploaded)
        binding.tvUploadHint.text = getString(R.string.tap_to_change)

        // Show preview image and hide the icon/text
        binding.ivIdCardPreview.visible()
        binding.ivUploadIcon.gone()
        binding.tvUploadText.gone()
        binding.tvUploadHint.gone()

        ImageDisplayUtils.loadInto(
            binding.ivIdCardPreview,
            idCardUri,
            R.drawable.ic_upload
        )

        Toast.makeText(this, getString(R.string.id_card_uploaded_success), Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_mountains

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_mountains -> {
                    startActivity(Intent(this, MountainsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_news -> {
                    startActivity(Intent(this, NewsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_status -> {
                    startActivity(Intent(this, StatusActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(this) { user ->
            user?.let {
                binding.etEmail.setText(it.email)
                if (it.fullName.isNotBlank()) {
                    binding.etFullName.setText(it.fullName)
                }
                if (it.phone.isNotBlank()) {
                    binding.etPhone.setText(it.phone)
                }
                if (it.address.isNotBlank()) {
                    binding.etAddress.setText(it.address)
                }
                if (it.dob.isNotBlank()) {
                    binding.etDob.setText(it.dob)
                }
            }
        }

        viewModel.mountainRoutes.observe(this) { routes ->
            if (routes.isNotEmpty()) {
                val adapter = RouteAdapter(this, routes)
                binding.actvRoute.setAdapter(adapter)
                binding.actvRoute.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup)
                binding.actvRoute.setOnItemClickListener { _, _, position, _ ->
                    val selectedRouteObj = routes[position]
                    selectedRoute = selectedRouteObj.name
                    selectedRouteId = selectedRouteObj.routeId  // Store routeId for capacity tracking
                    // Set display text to only show route name
                    binding.actvRoute.setText(selectedRouteObj.name, false)
                }
            }
        }

        viewModel.registrationState.observe(this) { state ->
            when (state) {
                is RegistrationState.Loading -> {
                    binding.progressBar.visible()
                    binding.btnRegister.isEnabled = false
                }
                is RegistrationState.Success -> {
                    binding.progressBar.gone()
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(
                        this,
                        getString(R.string.success_registration),
                        Toast.LENGTH_SHORT
                    ).show()
                    // Navigate to Status screen
                    val intent = Intent(this, StatusActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
                is RegistrationState.Error -> {
                    binding.progressBar.gone()
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateAndSubmit() {
        val fullName = binding.etFullName.text.toString().trim()
        val dob = binding.etDob.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val route = binding.actvRoute.text.toString().trim()

        // Validate full name
        if (fullName.isEmpty()) {
            binding.tilFullName.error = getString(R.string.error_empty_name)
            return
        }
        binding.tilFullName.error = null

        // Validate date of birth
        if (dob.isEmpty()) {
            binding.tilDob.error = getString(R.string.error_empty_dob)
            return
        }
        binding.tilDob.error = null

        // Validate address
        if (address.isEmpty()) {
            binding.tilAddress.error = getString(R.string.error_empty_address)
            return
        }
        binding.tilAddress.error = null

        // Validate phone
        if (phone.isEmpty()) {
            binding.tilPhone.error = getString(R.string.error_empty_phone)
            return
        }
        binding.tilPhone.error = null

        // Validate route
        if (route.isEmpty()) {
            binding.tilRoute.error = getString(R.string.error_select_route)
            return
        }
        binding.tilRoute.error = null

        // Validate ID card
        if (idCardUri.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_upload_id), Toast.LENGTH_SHORT).show()
            return
        }

        // Submit registration
        viewModel.submitRegistration(
            mountainId = mountainId,
            mountainName = mountainName,
            fullName = fullName,
            dob = dob,
            address = address,
            phone = phone,
            email = email,
            route = route,
            routeId = selectedRouteId,  // Pass routeId for capacity tracking
            idCardUri = idCardUri
        )
    }
}
