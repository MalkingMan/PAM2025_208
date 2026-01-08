package com.example.mountadmin.ui.news

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mountadmin.databinding.FragmentNewsManageBinding
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible

class NewsManageFragment : Fragment() {

    private var _binding: FragmentNewsManageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ManageNewsViewModel by viewModels()
    private lateinit var adapter: NewsManageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        observeViewModel()
        viewModel.loadNews()
    }

    private fun setupRecyclerView() {
        adapter = NewsManageAdapter(
            onEditClick = { news ->
                val intent = Intent(requireContext(), EditNewsActivity::class.java)
                intent.putExtra(EditNewsActivity.EXTRA_NEWS_ID, news.newsId)
                startActivity(intent)
            },
            onDeleteClick = { news ->
                showDeleteConfirmation(news.newsId, news.title)
            }
        )

        binding.rvNews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNews.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchNews(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupClickListeners() {
        binding.btnAddArticle.setOnClickListener {
            startActivity(Intent(requireContext(), AddNewsActivity::class.java))
        }
    }

    private fun showDeleteConfirmation(newsId: String, title: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Article")
            .setMessage("Are you sure you want to delete \"$title\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteNews(newsId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visible()
                binding.rvNews.gone()
                binding.layoutEmpty.gone()
            } else {
                binding.progressBar.gone()
            }
        }

        viewModel.newsList.observe(viewLifecycleOwner) { news ->
            adapter.submitList(news)
            if (news.isEmpty()) {
                binding.layoutEmpty.visible()
                binding.rvNews.gone()
            } else {
                binding.layoutEmpty.gone()
                binding.rvNews.visible()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadNews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

