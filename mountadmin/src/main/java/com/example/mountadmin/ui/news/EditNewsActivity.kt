package com.example.mountadmin.ui.news

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mountadmin.R
import com.example.mountadmin.data.model.News
import com.example.mountadmin.databinding.ActivityAddNewsBinding
import com.example.mountadmin.utils.ImageEncodeUtils
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible

class EditNewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewsBinding
    private val viewModel: EditNewsViewModel by viewModels()

    private var newsId: String = ""
    private var currentNews: News? = null
    private var selectedCategory: String = ""
    private var selectedImageUri: Uri? = null
    private var currentImageBase64: String = ""

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            displaySelectedImage(it)
        }
    }

    companion object {
        const val EXTRA_NEWS_ID = "extra_news_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        newsId = intent.getStringExtra(EXTRA_NEWS_ID) ?: run {
            finish()
            return
        }

        // Update UI for edit mode
        binding.tvTitle.text = getString(R.string.edit_article)
        binding.btnPublish.text = getString(R.string.save_changes)
        binding.btnDelete.visible()

        setupCategoryDropdown()
        setupClickListeners()
        observeViewModel()

        viewModel.loadNews(newsId)
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

        // Image upload click listeners
        binding.cardUploadPhoto.setOnClickListener {
            openImagePicker()
        }

        // Ensure the placeholder upload button also opens picker (was missing)
        runCatching {
            binding.btnUploadImage.setOnClickListener {
                openImagePicker()
            }
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

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
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

    private fun displayExistingImage(base64String: String) {
        if (base64String.isNotEmpty()) {
            try {
                val bytes = Base64.decode(base64String, Base64.NO_WRAP)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    binding.ivCoverImage.setImageBitmap(bitmap)
                    binding.ivCoverImage.visible()
                    binding.btnChangeImage.visible()
                    binding.layoutUploadPlaceholder.gone()
                }
            } catch (e: Exception) {
                // Failed to decode, show placeholder
            }
        }
    }

    private fun populateNewsData(news: News) {
        currentNews = news
        selectedCategory = news.category
        currentImageBase64 = news.coverImageUrl

        binding.etTitle.setText(news.title)
        binding.actvCategory.setText(news.category, false)
        binding.etContent.setText(news.content)
        binding.etTags.setText(news.tags)

        // Display existing cover image if available
        displayExistingImage(news.coverImageUrl)
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_article))
            .setMessage(getString(R.string.confirm_delete_article))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteNews(newsId)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
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
        currentNews?.let { news ->
            // Use new image if selected, otherwise keep existing
            val imageBase64 = if (selectedImageUri != null) {
                ImageEncodeUtils.uriToBase64(this, selectedImageUri!!) ?: currentImageBase64
            } else {
                currentImageBase64
            }

            val updatedNews = news.copy(
                title = binding.etTitle.text.toString().trim(),
                category = selectedCategory,
                content = binding.etContent.text.toString().trim(),
                tags = binding.etTags.text.toString().trim(),
                coverImageUrl = imageBase64,
                status = status
            )

            viewModel.updateNews(updatedNews)
        }
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

        viewModel.news.observe(this) { news ->
            news?.let { populateNewsData(it) }
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.success_article_saved), Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.deleteSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Article deleted successfully", Toast.LENGTH_SHORT).show()
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

