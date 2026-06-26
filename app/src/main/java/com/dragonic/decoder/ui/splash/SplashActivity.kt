package com.dragonic.decoder.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.appcompat.app.AppCompatActivity
import com.dragonic.decoder.databinding.ActivitySplashBinding
import com.dragonic.decoder.ui.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animateLogo()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2400L)
    }

    private fun animateLogo() {
        // Scale + fade in for logo
        val scaleAnim = ScaleAnimation(0.6f, 1f, 0.6f, 1f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f)
        scaleAnim.duration = 700

        val fadeAnim = AlphaAnimation(0f, 1f)
        fadeAnim.duration = 700

        val logoSet = AnimationSet(true).apply {
            addAnimation(scaleAnim)
            addAnimation(fadeAnim)
            fillAfter = true
        }
        binding.ivDragonLogo.startAnimation(logoSet)

        // Staggered text animations
        Handler(Looper.getMainLooper()).postDelayed({
            val textFade = AlphaAnimation(0f, 1f).apply { duration = 500; fillAfter = true }
            binding.tvAppName.startAnimation(textFade)
        }, 400)

        Handler(Looper.getMainLooper()).postDelayed({
            val textFade = AlphaAnimation(0f, 1f).apply { duration = 500; fillAfter = true }
            binding.tvDecoder.startAnimation(textFade)
        }, 600)

        Handler(Looper.getMainLooper()).postDelayed({
            val textFade = AlphaAnimation(0f, 1f).apply { duration = 400; fillAfter = true }
            binding.tvTagline.startAnimation(textFade)
        }, 900)

        Handler(Looper.getMainLooper()).postDelayed({
            val textFade = AlphaAnimation(0f, 1f).apply { duration = 400; fillAfter = true }
            binding.llLoading.startAnimation(textFade)
        }, 1100)
    }
}
