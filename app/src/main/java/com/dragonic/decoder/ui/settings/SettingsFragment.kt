package com.dragonic.decoder.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dragonic.decoder.R
import com.dragonic.decoder.data.repository.DecoderRepository
import com.dragonic.decoder.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val repository = DecoderRepository(application)
    fun clearHistory() { viewModelScope.launch { repository.clearAllHistory() } }
    fun clearDownloads() {
        viewModelScope.launch {
            repository.clearAllFiles()
            // Delete files from disk
            val dir = File(application.filesDir, "decoded_output")
            dir.listFiles()?.forEach { it.delete() }
        }
    }
}

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    private val PREFS = "dragonic_prefs"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPrefs()
        setupListeners()
    }

    private fun loadPrefs() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        binding.switchAutoSave.isChecked   = prefs.getBoolean("auto_save", true)
        binding.switchSaveHistory.isChecked = prefs.getBoolean("save_history", true)
        binding.switchVibrate.isChecked    = prefs.getBoolean("vibrate", false)
    }

    private fun savePrefs() {
        requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            putBoolean("auto_save",    binding.switchAutoSave.isChecked)
            putBoolean("save_history", binding.switchSaveHistory.isChecked)
            putBoolean("vibrate",      binding.switchVibrate.isChecked)
            apply()
        }
    }

    private fun setupListeners() {
        binding.switchAutoSave.setOnCheckedChangeListener   { _, _ -> savePrefs() }
        binding.switchSaveHistory.setOnCheckedChangeListener { _, _ -> savePrefs() }
        binding.switchVibrate.setOnCheckedChangeListener    { _, _ -> savePrefs() }

        binding.llClearHistory.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.Theme_DragonicDecoder)
                .setTitle("Clear History")
                .setMessage("Delete all decode history?")
                .setPositiveButton("Clear") { _, _ ->
                    viewModel.clearHistory()
                    toast("History cleared")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.llClearDownloads.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.Theme_DragonicDecoder)
                .setTitle("Clear Downloads")
                .setMessage("Delete all saved files?")
                .setPositiveButton("Clear") { _, _ ->
                    viewModel.clearDownloads()
                    toast("Downloads cleared")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.llResetSettings.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.Theme_DragonicDecoder)
                .setTitle("Reset Settings")
                .setMessage("Reset all settings to defaults? History and downloads will also be cleared.")
                .setPositiveButton("Reset") { _, _ ->
                    requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
                    viewModel.clearHistory()
                    viewModel.clearDownloads()
                    loadPrefs()
                    toast("All settings reset")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.llAboutApp.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.Theme_DragonicDecoder)
                .setTitle("Dragonic Decoder")
                .setMessage(
                    "Version 1.0.0\n\n" +
                    "A powerful offline encoding/decoding tool with cyberpunk aesthetics.\n\n" +
                    "Supports: Base64, Base32, Base16, Hex, URL, JWT, Unicode, HTML Entity, Binary, Octal, ROT13, Caesar Cipher.\n\n" +
                    "Developed by Leonore Tech Team.\n" +
                    "All decoding runs 100% offline, no data leaves your device."
                )
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
