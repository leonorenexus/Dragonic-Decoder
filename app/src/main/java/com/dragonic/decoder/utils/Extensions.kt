package com.dragonic.decoder.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.copyToClipboard(text: String, label: String = "Decoded Text") {
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}

fun Context.getClipboardText(): String? {
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return cm.primaryClip?.getItemAt(0)?.text?.toString()
}

fun Context.vibrateShort() {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(50)
            }
        }
    } catch (_: Exception) {}
}

fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    return when {
        diff < 60_000L        -> "Just now"
        diff < 3_600_000L     -> "${diff / 60_000} minutes ago"
        diff < 86_400_000L    -> "${diff / 3_600_000} hours ago"
        diff < 604_800_000L   -> "${diff / 86_400_000} days ago"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(this))
    }
}

fun Long.toFormattedDate(): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(this))

fun String.truncate(maxLength: Int): String =
    if (length > maxLength) substring(0, maxLength) + "…" else this

fun animateCounter(
    startVal: Int,
    endVal: Int,
    duration: Long = 800L,
    onUpdate: (Int) -> Unit
) {
    val handler = android.os.Handler(android.os.Looper.getMainLooper())
    val steps = 30
    val stepDelay = duration / steps
    var current = startVal
    val increment = (endVal - startVal) / steps.coerceAtLeast(1)
    var step = 0
    val runnable = object : Runnable {
        override fun run() {
            step++
            current = if (step >= steps) endVal else startVal + increment * step
            onUpdate(current)
            if (step < steps) handler.postDelayed(this, stepDelay)
        }
    }
    handler.post(runnable)
}
