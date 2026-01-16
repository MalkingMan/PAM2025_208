package com.example.mounttrack.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.mounttrack.R
import com.example.mounttrack.data.firebase.FirebaseHelper
import com.example.mounttrack.databinding.ActivitySplashBinding
import com.example.mounttrack.ui.auth.LoginActivity
import com.example.mounttrack.ui.dashboard.DashboardActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DURATION = 2500L // 2.5 seconds
    }

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set version text
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            binding.tvVersion.text = getString(R.string.version_format, pInfo.versionName)
        } catch (e: Exception) {
            binding.tvVersion.text = getString(R.string.version_format, "1.0.0")
        }

        // Start animations
        startAnimations()

        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, SPLASH_DURATION)
    }

    private fun startAnimations() {
        // Logo scale animation
        val logoAnim = AnimationUtils.loadAnimation(this, R.anim.anim_logo_scale)
        binding.ivLogo.startAnimation(logoAnim)

        // App name slide up
        val appNameAnim = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up_fade)
        binding.tvAppName.startAnimation(appNameAnim)

        // Tagline slide up with more delay
        val taglineAnim = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up_fade).apply {
            startOffset = 150L
        }
        binding.tvTagline.startAnimation(taglineAnim)

        // Mountain silhouette rise
        val mountainAnim = AnimationUtils.loadAnimation(this, R.anim.anim_mountain_rise)
        binding.ivMountainSilhouette.startAnimation(mountainAnim)

        // Progress bar fade in
        val progressAnim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in_delay)
        binding.progressBar.startAnimation(progressAnim)
    }

    private fun navigateToNextScreen() {
        // Check if user is already logged in
        val currentUser = FirebaseHelper.auth.currentUser
        
        val intent = if (currentUser != null) {
            // User is logged in, go to Dashboard
            Intent(this, DashboardActivity::class.java)
        } else {
            // User not logged in, go to Login
            Intent(this, LoginActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        
        // Fade out transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
