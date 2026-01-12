package com.example.mountadmin.ui.gunungadmin.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mountadmin.R
import com.example.mountadmin.data.model.Registration
import com.example.mountadmin.databinding.DialogIdCardPreviewBinding
import com.example.mountadmin.databinding.FragmentGunungAdminRegistrationBinding
import com.example.mountadmin.utils.ImageDisplayUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class GunungAdminRegistrationFragment : Fragment() {

    private var _binding: FragmentGunungAdminRegistrationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GunungAdminRegistrationViewModel by viewModels()
    private lateinit var registrationAdapter: GunungAdminRegistrationAdapter

    private var mountainId: String = ""
    private var currentFilter: String = FILTER_ALL

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
        _binding = FragmentGunungAdminRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        setupFilterChips()
        setupObservers()
        updateChipSelection() // Initial selection

        if (mountainId.isNotEmpty()) {
            viewModel.loadRegistrations(mountainId)
        }
    }

    private fun setupUI() {
        binding.tvTitle.text = getString(R.string.registrations)
        binding.tvSubtitle.text = getString(R.string.manage_registrations)
    }

    private fun setupRecyclerView() {
        registrationAdapter = GunungAdminRegistrationAdapter(
            onApproveClick = { registration ->
                showConfirmDialog(
                    title = "Approve Registration",
                    message = "Are you sure you want to approve ${registration.fullName}'s registration?",
                    onConfirm = {
                        viewModel.updateRegistrationStatus(
                            registration.registrationId,
                            Registration.STATUS_APPROVED
                        )
                    }
                )
            },
            onRejectClick = { registration ->
                showConfirmDialog(
                    title = "Reject Registration",
                    message = "Are you sure you want to reject ${registration.fullName}'s registration?",
                    onConfirm = {
                        viewModel.updateRegistrationStatus(
                            registration.registrationId,
                            Registration.STATUS_REJECTED
                        )
                    }
                )
            },
            onViewIdCardClick = { registration ->
                showIdCardDialog(registration)
            }
        )

        binding.rvRegistrations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = registrationAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener { applyFilter(FILTER_ALL) }
        binding.chipPending.setOnClickListener { applyFilter(FILTER_PENDING) }
        binding.chipApproved.setOnClickListener { applyFilter(FILTER_APPROVED) }
        binding.chipRejected.setOnClickListener { applyFilter(FILTER_REJECTED) }
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        updateChipSelection()
        viewModel.filterRegistrations(filter)
    }

    private fun updateChipSelection() {
        val chips = listOf(
            Pair(binding.chipAll, currentFilter == FILTER_ALL),
            Pair(binding.chipPending, currentFilter == FILTER_PENDING),
            Pair(binding.chipApproved, currentFilter == FILTER_APPROVED),
            Pair(binding.chipRejected, currentFilter == FILTER_REJECTED)
        )

        chips.forEach { (chip, isSelected) ->
            if (isSelected) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(resources.getColor(R.color.white, null))
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                chip.setTextColor(resources.getColor(R.color.text_secondary, null))
            }
        }
    }

    private fun setupObservers() {
        viewModel.filteredRegistrations.observe(viewLifecycleOwner) { registrations ->
            registrationAdapter.submitList(registrations)
            binding.tvNoRegistrations.visibility =
                if (registrations.isEmpty()) View.VISIBLE else View.GONE
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

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        }
    }

    private fun showConfirmDialog(title: String, message: String, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showIdCardDialog(registration: Registration) {
        val idCardValue = registration.idCardUri.trim()
        if (idCardValue.isBlank()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("ID Card")
                .setMessage("No ID Card uploaded for ${registration.fullName}.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        // Legacy data: MountTrack previously saved a local content:// URI.
        // This cannot be opened on admin devices, so show a helpful message.
        if (idCardValue.startsWith("content://", ignoreCase = true)) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("ID Card")
                .setMessage(
                    "ID Card for ${registration.fullName} was uploaded using an old app version and cannot be viewed here. " +
                        "Please ask the user to re-upload their ID Card."
                )
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val dialogBinding = DialogIdCardPreviewBinding.inflate(layoutInflater)
        ImageDisplayUtils.loadInto(
            dialogBinding.ivIdCard,
            idCardValue,
            R.drawable.ic_person
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("ID Card - ${registration.fullName}")
            .setView(dialogBinding.root)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MOUNTAIN_ID = "arg_mountain_id"

        const val FILTER_ALL = "ALL"
        const val FILTER_PENDING = "PENDING"
        const val FILTER_APPROVED = "APPROVED"
        const val FILTER_REJECTED = "REJECTED"

        fun newInstance(mountainId: String): GunungAdminRegistrationFragment {
            return GunungAdminRegistrationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MOUNTAIN_ID, mountainId)
                }
            }
        }
    }
}
