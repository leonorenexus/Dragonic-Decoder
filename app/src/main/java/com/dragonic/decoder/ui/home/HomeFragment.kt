package com.dragonic.decoder.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.dragonic.decoder.databinding.FragmentHomeBinding
import com.dragonic.decoder.ui.decoder.DecoderDetailActivity
import com.dragonic.decoder.utils.DecoderEngine
import com.dragonic.decoder.utils.getClipboardText

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { readFileFromUri(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupQuickTools()
        setupButtons()
    }

    private fun setupButtons() {
        binding.btnPaste.setOnClickListener {
            val text = requireContext().getClipboardText()
            if (!text.isNullOrEmpty()) {
                binding.etInput.setText(text)
            }
        }

        binding.btnUpload.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        binding.btnDecode.setOnClickListener {
            val input = binding.etInput.text.toString().trim()
            if (input.isEmpty()) return@setOnClickListener
            val detected = DecoderEngine.detectEncoding(input)
            val type = mapDetectedToType(detected)
            openDecoderDetail(input, type)
        }

        binding.tvViewAll.setOnClickListener {
            openAllTools()
        }
    }

    private fun setupQuickTools() {
        data class ToolConfig(val nameRes: String, val actionRes: String, val type: DecoderEngine.DecoderType)

        val tools = listOf(
            Triple(binding.toolBase64Decode,  "Base64",   DecoderEngine.DecoderType.BASE64),
            Triple(binding.toolBase64Encode,  "Base64",   DecoderEngine.DecoderType.BASE64),
            Triple(binding.toolBase32,        "Base32",   DecoderEngine.DecoderType.BASE32),
            Triple(binding.toolHex,           "Hex",      DecoderEngine.DecoderType.HEX),
            Triple(binding.toolUrl,           "URL",      DecoderEngine.DecoderType.URL),
            Triple(binding.toolJwt,           "JWT",      DecoderEngine.DecoderType.JWT),
            Triple(binding.toolUnicode,       "Unicode",  DecoderEngine.DecoderType.UNICODE),
            Triple(binding.toolBinary,        "Binary",   DecoderEngine.DecoderType.BINARY),
            Triple(binding.toolRot13,         "ROT13",    DecoderEngine.DecoderType.ROT13)
        )

        tools.forEachIndexed { index, (toolView, name, type) ->
            toolView.findViewById<android.widget.TextView>(
                com.dragonic.decoder.R.id.tv_tool_name
            )?.text = name

            val action = if (index == 1) "Encode" else "Decode"
            toolView.findViewById<android.widget.TextView>(
                com.dragonic.decoder.R.id.tv_tool_action
            )?.text = action

            toolView.setOnClickListener {
                val input = binding.etInput.text.toString()
                openDecoderDetail(input, type, isEncode = (index == 1))
            }
        }
    }

    private fun openDecoderDetail(input: String, type: DecoderEngine.DecoderType, isEncode: Boolean = false) {
        val intent = Intent(requireContext(), DecoderDetailActivity::class.java).apply {
            putExtra(DecoderDetailActivity.EXTRA_INPUT, input)
            putExtra(DecoderDetailActivity.EXTRA_TYPE, type.name)
            putExtra(DecoderDetailActivity.EXTRA_IS_ENCODE, isEncode)
        }
        startActivity(intent)
        requireActivity().overridePendingTransition(
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
    }

    private fun openAllTools() {
        val intent = Intent(requireContext(), DecoderDetailActivity::class.java).apply {
            putExtra(DecoderDetailActivity.EXTRA_INPUT, "")
            putExtra(DecoderDetailActivity.EXTRA_TYPE, DecoderEngine.DecoderType.BASE64.name)
            putExtra(DecoderDetailActivity.EXTRA_IS_ENCODE, false)
        }
        startActivity(intent)
    }

    private fun readFileFromUri(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                val content = stream.bufferedReader().readText()
                binding.etInput.setText(content.take(5000))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mapDetectedToType(detected: String): DecoderEngine.DecoderType {
        return when {
            detected.contains("Base64")  -> DecoderEngine.DecoderType.BASE64
            detected.contains("Base32")  -> DecoderEngine.DecoderType.BASE32
            detected.contains("Hex")     -> DecoderEngine.DecoderType.HEX
            detected.contains("Binary")  -> DecoderEngine.DecoderType.BINARY
            detected.contains("Octal")   -> DecoderEngine.DecoderType.OCTAL
            detected.contains("URL")     -> DecoderEngine.DecoderType.URL
            detected.contains("JWT")     -> DecoderEngine.DecoderType.JWT
            detected.contains("Unicode") -> DecoderEngine.DecoderType.UNICODE
            detected.contains("HTML")    -> DecoderEngine.DecoderType.HTML_ENTITY
            else                         -> DecoderEngine.DecoderType.BASE64
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
