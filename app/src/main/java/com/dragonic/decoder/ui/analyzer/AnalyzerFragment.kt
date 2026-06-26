package com.dragonic.decoder.ui.analyzer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.dragonic.decoder.databinding.FragmentAnalyzerBinding
import com.dragonic.decoder.ui.decoder.DecoderDetailActivity
import com.dragonic.decoder.utils.DecoderEngine
import com.dragonic.decoder.utils.FileUtils
import com.dragonic.decoder.utils.gone
import com.dragonic.decoder.utils.visible
import java.io.File

class AnalyzerFragment : Fragment() {

    private var _binding: FragmentAnalyzerBinding? = null
    private val binding get() = _binding!!
    private var currentFileContent: String? = null
    private var detectedType: DecoderEngine.DecoderType = DecoderEngine.DecoderType.BASE64

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { analyzeUri(it) } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnalyzerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.llUploadArea.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        binding.btnDecodeFile.setOnClickListener {
            val content = currentFileContent ?: return@setOnClickListener
            val intent = Intent(requireContext(), DecoderDetailActivity::class.java).apply {
                putExtra(DecoderDetailActivity.EXTRA_INPUT, content)
                putExtra(DecoderDetailActivity.EXTRA_TYPE, detectedType.name)
                putExtra(DecoderDetailActivity.EXTRA_IS_ENCODE, false)
            }
            startActivity(intent)
        }
    }

    private fun analyzeUri(uri: Uri) {
        try {
            // Get file name & size
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            var fileName = "Unknown File"
            var fileSize = 0L
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx >= 0) fileName = it.getString(nameIdx) ?: fileName
                    if (sizeIdx >= 0) fileSize = it.getLong(sizeIdx)
                }
            }

            // Read content
            val content = requireContext().contentResolver.openInputStream(uri)
                ?.bufferedReader()?.readText() ?: return

            currentFileContent = content

            // Update file info UI
            binding.tvFileName.text = fileName
            binding.tvFilePath.text = uri.path ?: ""
            binding.tvFileSize.text = FileUtils.formatFileSize(fileSize)

            // Analyze
            val detected = DecoderEngine.detectEncoding(content)
            val entropy = DecoderEngine.calculateEntropy(content)
            val lineCount = content.lines().size
            val charCount = content.length

            detectedType = mapToType(detected)

            // Show detection result
            binding.cardDetection.visible()
            binding.cardPreview.visible()

            binding.tvFileType.text = getFileType(fileName)
            binding.tvDetectedEncoding.text = detected
            binding.tvPossibleType.text = getPossibleType(detected)
            binding.tvEntropy.text = "${"%.2f".format(entropy)} (${getEntropyLevel(entropy)})"
            binding.tvLineCount.text = lineCount.toString()
            binding.tvCharCount.text = charCount.toString()
            binding.tvRecommendation.text = "$detected Decode"

            // Content preview
            binding.tvContentPreview.text = content.take(300)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mapToType(detected: String): DecoderEngine.DecoderType = when {
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

    private fun getFileType(name: String): String = when (name.substringAfterLast('.').lowercase()) {
        "txt"  -> "Text File"
        "json" -> "JSON File"
        "xml"  -> "XML File"
        "csv"  -> "CSV File"
        "log"  -> "Log File"
        "conf", "cfg" -> "Config File"
        "dat"  -> "Data File"
        "bin"  -> "Binary File"
        else   -> "Unknown File"
    }

    private fun getPossibleType(detected: String): String = when {
        detected.contains("Base64") -> "Encoded Text / Binary Data"
        detected.contains("Hex")    -> "Raw Bytes / Encoded Data"
        detected.contains("JWT")    -> "JSON Web Token"
        detected.contains("URL")    -> "URL Encoded String"
        detected.contains("Binary") -> "Binary String"
        detected.contains("Base32") -> "Base32 Encoded"
        else                        -> "Plain Text"
    }

    private fun getEntropyLevel(entropy: Double): String = when {
        entropy < 2.0 -> "Very Low"
        entropy < 4.0 -> "Low"
        entropy < 5.5 -> "Medium"
        entropy < 7.0 -> "High"
        else          -> "Very High"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
