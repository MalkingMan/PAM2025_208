package com.example.mountadmin.ui.gunungadmin.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mountadmin.R
import com.example.mountadmin.databinding.FragmentGunungAdminProfileBinding
import com.example.mountadmin.ui.auth.LoginActivity
import com.example.mountadmin.ui.settings.EditProfileActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class GunungAdminProfileFragment : Fragment() {

    private var _binding: FragmentGunungAdminProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GunungAdminProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGunungAdminProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()

        viewModel.loadAdminProfile()
    }

    private fun setupUI() {
        binding.cardEditProfile.setOnClickListener {
            // Navigate to Edit Profile Activity
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.cardLogout.setOnClickListener {
            showLogoutConfirmDialog()
        }
    }

    private fun setupObservers() {
        viewModel.adminProfile.observe(viewLifecycleOwner) { admin ->
            admin?.let {
                binding.tvAdminName.text = it.fullName
                binding.tvAdminEmail.text = it.email
                binding.tvAdminRole.text = it.role.replace("_", " ")
            }
        }
    }

    private fun showLogoutConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.confirm_logout))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                logout()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(requireContext(), getString(R.string.success_logout), Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onResume() {
        super.onResume()
        // Reload profile data when returning from EditProfileActivity
        viewModel.loadAdminProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): GunungAdminProfileFragment {
            return GunungAdminProfileFragment()
        }
    }
}

