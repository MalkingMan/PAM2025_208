package com.example.mountadmin.ui.news

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mountadmin.R
import com.example.mountadmin.data.model.News
import com.example.mountadmin.databinding.ActivityAddNewsBinding
import com.example.mountadmin.utils.ImageEncodeUtils
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible

class AddNewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewsBinding
    private val viewModel: AddNewsViewModel by viewModels()

    private var selectedCategory: String = ""

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
        binding = ActivityAddNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategoryDropdown()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupCategoryDropdown() {
        val categories = listOf(
            "Hiking Tips",
            "Mountain News",
            "Safety Guidelines",
            "Events",
            "Equipment Reviews",
            "Conservation"
        )

        val adapter = ArrayAdapter(this, R.layout.item_dropdown, categories)
        binding.actvCategory.setAdapter(adapter)
        binding.actvCategory.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup)

        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = categories[position]
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Image upload click listeners (safe: handle both container + explicit button)
        binding.cardUploadPhoto.setOnClickListener {
            openImagePicker()
        }
        binding.btnUploadImage.setOnClickListener {
            openImagePicker()
        }
        binding.btnChangeImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnPublish.setOnClickListener {
            if (validateInput()) {
                saveNews(News.STATUS_PUBLISHED)
            }
        }

        binding.btnSaveDraft.setOnClickListener {
            if (validateInput()) {
                saveNews(News.STATUS_DRAFT)
            }
        }
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun displaySelectedImage(uri: Uri) {
        binding.ivCoverImage.setImageURI(uri)
        binding.ivCoverImage.visible()
        binding.btnChangeImage.visible()
        binding.layoutUploadPlaceholder.gone()
    }

    private fun validateInput(): Boolean {
        var isValid = true

        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (title.isEmpty()) {
            binding.tilTitle.error = "Title is required"
            isValid = false
        } else {
            binding.tilTitle.error = null
        }

        if (selectedCategory.isEmpty()) {
            binding.tilCategory.error = "Category is required"
            isValid = false
        } else {
            binding.tilCategory.error = null
        }

        if (content.isEmpty()) {
            binding.tilContent.error = "Content is required"
            isValid = false
        } else {
            binding.tilContent.error = null
        }

        return isValid
    }

    private fun saveNews(status: String) {
        val imageBase64 = selectedImageUri
            ?.let { uri -> ImageEncodeUtils.uriToBase64(this, uri) }
            .orEmpty()

        val news = News(
            title = binding.etTitle.text.toString().trim(),
            category = selectedCategory,
            content = binding.etContent.text.toString().trim(),
            tags = binding.etTags.text.toString().trim(),
            // NOTE: project already uses Base64 in coverImageUrl
            coverImageUrl = imageBase64,
            status = status
        )

        viewModel.saveNews(news)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visible()
                binding.btnPublish.isEnabled = false
                binding.btnSaveDraft.isEnabled = false
            } else {
                binding.progressBar.gone()
                binding.btnPublish.isEnabled = true
                binding.btnSaveDraft.isEnabled = true
            }
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.success_article_published), Toast.LENGTH_SHORT).show()
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
