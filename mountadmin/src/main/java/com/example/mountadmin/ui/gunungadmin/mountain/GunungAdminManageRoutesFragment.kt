package com.example.mountadmin.ui.gunungadmin.mountain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mountadmin.R
import com.example.mountadmin.data.model.HikingRoute
import com.example.mountadmin.databinding.DialogAddRouteBinding
import com.example.mountadmin.databinding.FragmentGunungAdminManageRoutesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class GunungAdminManageRoutesFragment : Fragment() {

    private var _binding: FragmentGunungAdminManageRoutesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GunungAdminManageRoutesViewModel by viewModels()

    private lateinit var adapter: GunungAdminManageRoutesAdapter

    private var mountainId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mountainId = arguments?.getString(ARG_MOUNTAIN_ID, "").orEmpty()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGunungAdminManageRoutesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClicks()
        setupObservers()

        if (mountainId.isNotBlank()) {
            viewModel.loadRoutes(mountainId)
        }
    }

    private fun setupRecyclerView() {
        adapter = GunungAdminManageRoutesAdapter(
            onEdit = { showRouteDialog(it) },
            onDelete = { confirmDelete(it) },
            onToggleStatus = { viewModel.toggleRouteStatus(mountainId, it) }
        )

        binding.rvRoutes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@GunungAdminManageRoutesFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupClicks() {
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnAddRoute.setOnClickListener { showRouteDialog(null) }
    }

    private fun setupObservers() {
        viewModel.routes.observe(viewLifecycleOwner) { routes ->
            adapter.submitList(routes)
            binding.tvNoRoutes.visibility = if (routes.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnAddRoute.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }
    }

    private fun showRouteDialog(existing: HikingRoute?) {
        val dialogBinding = DialogAddRouteBinding.inflate(LayoutInflater.from(requireContext()))

        val difficulties = listOf("Easy", "Moderate", "Hard", "Expert")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, difficulties)
        dialogBinding.actvDifficulty.setAdapter(adapter)
        dialogBinding.actvDifficulty.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup)

        val isEdit = existing != null
        dialogBinding.etRouteName.setText(existing?.name.orEmpty())
        dialogBinding.actvDifficulty.setText(existing?.difficulty.orEmpty(), false)
        dialogBinding.etMaxCapacity.setText((existing?.maxCapacity ?: 100).toString())

        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_MountAdmin_AlertDialog)
            .setTitle(if (isEdit) "Edit Route" else "Add Route")
            .setView(dialogBinding.root)
            .setPositiveButton(if (isEdit) "Save" else "Add") { _, _ ->
                val routeName = dialogBinding.etRouteName.text.toString().trim()
                val difficulty = dialogBinding.actvDifficulty.text.toString().trim()
                val maxCapacity = dialogBinding.etMaxCapacity.text.toString().toIntOrNull() ?: 0

                if (routeName.isBlank() || difficulty.isBlank()) return@setPositiveButton

                val usedCapacity = existing?.usedCapacity ?: 0
                if (maxCapacity in 1 until usedCapacity) {
                    Snackbar.make(binding.root, "Max capacity cannot be less than used capacity ($usedCapacity)", Snackbar.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val route = HikingRoute(
                    routeId = existing?.routeId?.takeIf { it.isNotBlank() } ?: java.util.UUID.randomUUID().toString(),
                    name = routeName,
                    difficulty = difficulty,
                    maxCapacity = if (maxCapacity > 0) maxCapacity else 100,
                    usedCapacity = usedCapacity,
                    status = existing?.status ?: HikingRoute.STATUS_OPEN,
                    estimatedTime = existing?.estimatedTime.orEmpty(),
                    distance = existing?.distance.orEmpty()
                )

                if (isEdit) {
                    viewModel.updateRoute(mountainId, route)
                } else {
                    viewModel.addRoute(mountainId, route)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete(route: HikingRoute) {
        if (route.usedCapacity > 0) {
            Snackbar.make(binding.root, "Route has used capacity (${route.usedCapacity}). Close it instead of deleting.", Snackbar.LENGTH_LONG).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_MountAdmin_AlertDialog)
            .setTitle("Delete Route")
            .setMessage("Delete route \"${route.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteRoute(mountainId, route)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MOUNTAIN_ID = "arg_mountain_id"

        fun newInstance(mountainId: String): GunungAdminManageRoutesFragment {
            return GunungAdminManageRoutesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MOUNTAIN_ID, mountainId)
                }
            }
        }
    }
}

