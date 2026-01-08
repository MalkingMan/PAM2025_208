package com.example.mounttrack.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mounttrack.R
import com.example.mounttrack.databinding.ActivityRegisterBinding
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

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "RegisterActivity"
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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
        binding.btnRegister.setOnClickListener {
            validateAndRegister()
        }

        binding.btnGoogleSignup.setOnClickListener {
            signUpWithGoogle()
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateAndRegister() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val termsAccepted = binding.cbTerms.isChecked

        // Validate full name
        if (fullName.isEmpty()) {
            binding.tilFullName.error = getString(R.string.error_empty_name)
            return
        }
        binding.tilFullName.error = null

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

        // Validate confirm password
        if (!ValidationUtils.passwordsMatch(password, confirmPassword)) {
            binding.tilConfirmPassword.error = getString(R.string.error_passwords_not_match)
            return
        }
        binding.tilConfirmPassword.error = null

        // Validate terms
        if (!termsAccepted) {
            Toast.makeText(this, getString(R.string.error_terms_not_accepted), Toast.LENGTH_SHORT).show()
            return
        }

        // Perform registration
        viewModel.register(fullName, email, password)
    }

    private fun observeViewModel() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visible()
                    binding.btnRegister.text = ""
                    binding.btnRegister.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.progressBar.gone()
                    binding.btnRegister.text = getString(R.string.register)
                    binding.btnRegister.isEnabled = true
                    navigateToDashboard()
                }
                is AuthState.Error -> {
                    binding.progressBar.gone()
                    binding.btnRegister.text = getString(R.string.register)
                    binding.btnRegister.isEnabled = true
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

    private fun signUpWithGoogle() {
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
                Toast.makeText(this, "Google Sign-Up failed: No ID token", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-Up failed", e)
            Toast.makeText(this, "Google Sign-Up failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}


