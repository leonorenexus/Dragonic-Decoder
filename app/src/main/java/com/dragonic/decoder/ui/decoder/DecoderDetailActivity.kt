package com.dragonic.decoder.ui.decoder

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dragonic.decoder.data.db.HistoryEntry
import com.dragonic.decoder.data.repository.DecoderRepository
import com.dragonic.decoder.databinding.ActivityDecoderDetailBinding
import com.dragonic.decoder.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ---- ViewModel ----
class DecoderViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val repository = DecoderRepository(application)

    private val _result = MutableLiveData<DecoderEngine.DecodeResult>()
    val result: LiveData<DecoderEngine.DecodeResult> = _result

    fun decode(input: String, type: DecoderEngine.DecoderType) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = DecoderEngine.decode(input, type)
            _result.postValue(res)
            if (shouldSaveHistory(application)) {
                repository.insertHistory(
                    HistoryEntry(
                        encodingType  = type.label,
                        inputPreview  = input.truncate(80),
                        outputPreview = res.output.truncate(80),
                        fullInput     = input,
                        fullOutput    = res.output,
                        status        = if (res.success) "SUCCESS" else "FAILED",
                        durationMs    = res.durationMs
                    )
                )
            }
        }
    }

    fun encode(input: String, type: DecoderEngine.DecoderType) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = DecoderEngine.encode(input, type)
            _result.postValue(res)
        }
    }

    suspend fun saveResult(content: String, type: String) =
        withContext(Dispatchers.IO) {
            repository.saveDecodedResult(content, type)
        }

    private fun shouldSaveHistory(ctx: android.app.Application): Boolean {
        val prefs = ctx.getSharedPreferences("dragonic_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("save_history", true)
    }
}

// ---- Activity ----
class DecoderDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_INPUT     = "extra_input"
        const val EXTRA_TYPE      = "extra_type"
        const val EXTRA_IS_ENCODE = "extra_is_encode"
    }

    private lateinit var binding: ActivityDecoderDetailBinding
    private val viewModel: DecoderViewModel by viewModels()

    private var currentType   = DecoderEngine.DecoderType.BASE64
    private var isEncodeMode  = false
    private var currentOutput = ""

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { readAndSetInput(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDecoderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init from extras
        val inputText = intent.getStringExtra(EXTRA_INPUT) ?: ""
        val typeName  = intent.getStringExtra(EXTRA_TYPE) ?: DecoderEngine.DecoderType.BASE64.name
        isEncodeMode  = intent.getBooleanExtra(EXTRA_IS_ENCODE, false)

        currentType = runCatching {
            DecoderEngine.DecoderType.valueOf(typeName)
        }.getOrDefault(DecoderEngine.DecoderType.BASE64)

        binding.etDecoderInput.setText(inputText)
        updateTitle()
        updateModeUI()

        if (inputText.isNotEmpty()) runOperation()

        setupListeners()
        observeResult()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.tvModeDecode.setOnClickListener {
            isEncodeMode = false
            updateModeUI()
            runOperation()
        }

        binding.tvModeEncode.setOnClickListener {
            isEncodeMode = true
            updateModeUI()
            runOperation()
        }

        binding.ivPasteInput.setOnClickListener {
            val text = getClipboardText()
            if (!text.isNullOrEmpty()) {
                binding.etDecoderInput.setText(text)
                runOperation()
            }
        }

        binding.ivClearInput.setOnClickListener {
            binding.etDecoderInput.text?.clear()
            binding.tvOutput.text = ""
            binding.cardInfo.gone()
            binding.tvOutputStatus.gone()
        }

        binding.ivUploadInput.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        // Decode on text change with small delay
        binding.etDecoderInput.addTextChangedListener(object : android.text.TextWatcher {
            private val handler = android.os.Handler(android.os.Looper.getMainLooper())
            private val debounce = Runnable { runOperation() }
            override fun afterTextChanged(s: android.text.Editable?) {
                handler.removeCallbacks(debounce)
                handler.postDelayed(debounce, 600)
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        binding.btnCopyResult.setOnClickListener {
            if (currentOutput.isNotEmpty()) {
                copyToClipboard(currentOutput)
                vibrateShort()
            }
        }

        binding.btnShareResult.setOnClickListener {
            if (currentOutput.isNotEmpty()) {
                startActivity(
                    Intent.createChooser(
                        FileUtils.createShareTextIntent(currentOutput), "Share Result"
                    )
                )
            }
        }

        binding.btnSaveResult.setOnClickListener {
            if (currentOutput.isNotEmpty()) {
                lifecycleScope.launch {
                    val saved = viewModel.saveResult(currentOutput, currentType.label)
                    if (saved != null) {
                        Toast.makeText(
                            this@DecoderDetailActivity,
                            "Saved: ${saved.fileName}", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.ivCloseInfo.setOnClickListener { binding.cardInfo.gone() }
    }

    private fun observeResult() {
        viewModel.result.observe(this) { res ->
            currentOutput = res.output
            if (res.success) {
                binding.tvOutput.text = res.output
                binding.tvOutputStatus.visible()
                binding.tvOutputStatus.text = if (isEncodeMode) "ENCODED" else "DECODED"
                // Show info card
                binding.cardInfo.visible()
                binding.tvInfoEncoding.text = currentType.label
                binding.tvInfoChars.text    = res.charCount.toString()
                binding.tvInfoTime.text     = "${res.durationMs} ms"
            } else {
                binding.tvOutput.text = "Error: ${res.errorMessage}"
                binding.tvOutputStatus.visible()
                binding.tvOutputStatus.text = "FAILED"
                binding.tvOutputStatus.setTextColor(getColor(com.dragonic.decoder.R.color.status_failed))
                binding.tvOutputStatus.setBackgroundResource(com.dragonic.decoder.R.drawable.bg_badge_failed)
            }
        }
    }

    private fun runOperation() {
        val input = binding.etDecoderInput.text.toString()
        if (input.isEmpty()) return
        if (isEncodeMode) viewModel.encode(input, currentType)
        else              viewModel.decode(input, currentType)
    }

    private fun updateTitle() {
        val mode = if (isEncodeMode) "ENCODE" else "DECODE"
        binding.tvDecoderTitle.text = "${currentType.label.uppercase()} $mode"
        binding.tvDecoderTypelabel.text = if (isEncodeMode) "ENCODE:" else "DECODE:"
    }

    private fun updateModeUI() {
        updateTitle()
        if (isEncodeMode) {
            binding.tvModeEncode.setBackgroundResource(com.dragonic.decoder.R.drawable.bg_decode_button)
            binding.tvModeEncode.setTextColor(getColor(android.R.color.black))
            binding.tvModeDecode.setBackgroundResource(android.R.color.transparent)
            binding.tvModeDecode.setTextColor(getColor(com.dragonic.decoder.R.color.text_secondary))
        } else {
            binding.tvModeDecode.setBackgroundResource(com.dragonic.decoder.R.drawable.bg_decode_button)
            binding.tvModeDecode.setTextColor(getColor(android.R.color.black))
            binding.tvModeEncode.setBackgroundResource(android.R.color.transparent)
            binding.tvModeEncode.setTextColor(getColor(com.dragonic.decoder.R.color.text_secondary))
        }
    }

    private fun readAndSetInput(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val content = stream.bufferedReader().readText()
                binding.etDecoderInput.setText(content.take(10_000))
                runOperation()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not read file", Toast.LENGTH_SHORT).show()
        }
    }
}
