package com.example.mountadmin.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mountadmin.databinding.FragmentNewsListBinding
import com.example.mountadmin.utils.gone
import com.example.mountadmin.utils.visible

class NewsListFragment : Fragment() {

    private var _binding: FragmentNewsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewsListViewModel by viewModels()
    private lateinit var adapter: NewsReadOnlyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        viewModel.loadPublishedNews()
    }

    private fun setupRecyclerView() {
        adapter = NewsReadOnlyAdapter { _ ->
            // Handle article click - read-only view
        }

        binding.rvNews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNews.adapter = adapter
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

