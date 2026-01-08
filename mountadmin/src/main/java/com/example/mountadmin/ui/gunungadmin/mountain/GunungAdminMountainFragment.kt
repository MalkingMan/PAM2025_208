package com.example.mountadmin.ui.gunungadmin.mountain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mountadmin.R
import com.example.mountadmin.databinding.FragmentGunungAdminMountainBinding
import com.google.android.material.snackbar.Snackbar

class GunungAdminMountainFragment : Fragment() {

    private var _binding: FragmentGunungAdminMountainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GunungAdminMountainViewModel by viewModels()
    private lateinit var routesAdapter: MountainRoutesAdapter

    private var mountainId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mountainId = it.getString(ARG_MOUNTAIN_ID, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGunungAdminMountainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        binding.btnManageRoutes.setOnClickListener {
            if (mountainId.isNotBlank()) {
                val fragment = GunungAdminManageRoutesFragment.newInstance(mountainId)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        if (mountainId.isNotEmpty()) {
            viewModel.loadMountainData(mountainId)
        }
    }

    private fun setupRecyclerView() {
        routesAdapter = MountainRoutesAdapter()
        binding.rvHikingRoutes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = routesAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.mountain.observe(viewLifecycleOwner) { mountain ->
            mountain?.let {
                binding.tvMountainName.text = it.name
                binding.tvMountainElevation.text = "${it.elevation} m"
                binding.tvMountainLocation.text = it.location
                binding.tvMountainDescription.text = it.description

                // Load mountain image
                if (it.imageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(it.imageUrl)
                        .placeholder(R.drawable.ic_mountain_placeholder)
                        .error(R.drawable.ic_mountain_placeholder)
                        .centerCrop()
                        .into(binding.ivMountainImage)
                }
            }
        }

        viewModel.routes.observe(viewLifecycleOwner) { routes ->
            routesAdapter.submitList(routes)
            binding.tvNoRoutes.visibility = if (routes.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MOUNTAIN_ID = "arg_mountain_id"

        fun newInstance(mountainId: String): GunungAdminMountainFragment {
            return GunungAdminMountainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MOUNTAIN_ID, mountainId)
                }
            }
        }
    }
}
