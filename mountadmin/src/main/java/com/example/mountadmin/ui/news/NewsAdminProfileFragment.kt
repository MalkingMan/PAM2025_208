package com.example.mountadmin.ui.news

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.mountadmin.R
import com.example.mountadmin.data.repository.AuthRepository
import com.example.mountadmin.databinding.FragmentNewsAdminProfileBinding
import com.example.mountadmin.ui.auth.LoginActivity
import com.example.mountadmin.ui.settings.EditProfileActivity
import com.google.firebase.auth.FirebaseAuth

class NewsAdminProfileFragment : Fragment() {

    private var _binding: FragmentNewsAdminProfileBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsAdminProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        bindProfileHeader()
    }

    private fun bindProfileHeader() {
        val user = FirebaseAuth.getInstance().currentUser
        binding.tvAdminName.text = user?.displayName ?: (user?.email?.substringBefore("@") ?: "Admin")
        binding.tvAdminEmail.text = user?.email ?: ""
        binding.tvAdminRole.text = "NEWS ADMIN"
    }

    private fun setupClickListeners() {
        binding.cardEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.cardLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.confirm_logout))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                logout()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun logout() {
        authRepository.logout()
        Toast.makeText(requireContext(), getString(R.string.success_logout), Toast.LENGTH_SHORT).show()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onResume() {
        super.onResume()
        // Refresh after EditProfileActivity
        bindProfileHeader()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
