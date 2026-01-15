package com.example.mounttrack.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mounttrack.R
import com.example.mounttrack.databinding.ActivityLoginBinding
import com.example.mounttrack.ui.dashboard.DashboardActivity
import com.example.mounttrack.utils.ValidationUtils
import com.example.mounttrack.utils.gone
import com.example.mounttrack.utils.visible
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "LoginActivity"
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        if (viewModel.isLoggedIn()) {
            navigateToDashboard()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureGoogleSignIn()
        setupClickListeners()
        observeViewModel()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            validateAndLogin()
        }

        binding.btnGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        // Create dialog view
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null)
        val tilEmail = dialogView.findViewById<TextInputLayout>(R.id.tilEmail)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEmail)

        // Pre-fill with email from login field if available
        val loginEmail = binding.etEmail.text?.toString()?.trim()
        if (!loginEmail.isNullOrBlank()) {
            etEmail.setText(loginEmail)
        }

        MaterialAlertDialogBuilder(this, R.style.MountTrack_AlertDialog)
            .setTitle(getString(R.string.forgot_password))
            .setMessage(getString(R.string.forgot_password_message))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.send_reset_email)) { dialog, _ ->
                val email = etEmail.text?.toString()?.trim() ?: ""

                if (email.isEmpty()) {
                    tilEmail.error = getString(R.string.error_empty_email)
                    return@setPositiveButton
                }

                if (!ValidationUtils.isValidEmail(email)) {
                    tilEmail.error = getString(R.string.error_invalid_email)
                    return@setPositiveButton
                }

                // Send password reset email
                sendPasswordResetEmail(email)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun sendPasswordResetEmail(email: String) {
        binding.progressBar.visible()

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.progressBar.gone()

                if (task.isSuccessful) {
                    Log.d(TAG, "Password reset email sent to: $email")
                    Toast.makeText(
                        this,
                        getString(R.string.password_reset_email_sent),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.e(TAG, "Failed to send password reset email", task.exception)
                    val errorMessage = task.exception?.message ?: getString(R.string.error_send_reset_email)
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun validateAndLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Validate email
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_empty_email)
            return
        }

        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            return
        }
        binding.tilEmail.error = null

        // Validate password
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_empty_password)
            return
        }

        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.error = getString(R.string.error_password_short)
            return
        }
        binding.tilPassword.error = null

        // Perform login
        viewModel.login(email, password)
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visible()
                    binding.btnLogin.text = ""
                    binding.btnLogin.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.progressBar.gone()
                    binding.btnLogin.text = getString(R.string.login)
                    binding.btnLogin.isEnabled = true
                    navigateToDashboard()
                }
                is AuthState.Error -> {
                    binding.progressBar.gone()
                    binding.btnLogin.text = getString(R.string.login)
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                // Sign in with Firebase using the Google ID token
                viewModel.signInWithGoogle(idToken)
            } ?: run {
                Toast.makeText(this, "Google Sign-In failed: No ID token", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed", e)
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}


