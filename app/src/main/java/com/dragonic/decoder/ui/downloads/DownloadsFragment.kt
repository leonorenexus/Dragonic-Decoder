package com.dragonic.decoder.ui.downloads

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragonic.decoder.R
import com.dragonic.decoder.data.db.SavedFile
import com.dragonic.decoder.data.repository.DecoderRepository
import com.dragonic.decoder.databinding.FragmentDownloadsBinding
import com.dragonic.decoder.utils.FileUtils
import com.dragonic.decoder.utils.gone
import com.dragonic.decoder.utils.visible
import kotlinx.coroutines.launch
import java.io.File

// ---- ViewModel ----
class DownloadsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DecoderRepository(application)
    val allFiles: LiveData<List<SavedFile>> = repository.getAllFiles()

    fun searchFiles(query: String): LiveData<List<SavedFile>> = repository.searchFiles(query)
    fun deleteFile(file: SavedFile) { viewModelScope.launch { repository.deleteFile(file) } }
    fun clearAll() { viewModelScope.launch { repository.clearAllFiles() } }
}

// ---- Fragment ----
class DownloadsFragment : Fragment() {

    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DownloadsViewModel by viewModels()
    private lateinit var adapter: DownloadsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
        setupSearch()
    }

    private fun setupRecyclerView() {
        adapter = DownloadsAdapter(
            onDownloadClick = { file -> openFile(file) },
            onDeleteClick   = { file -> confirmDelete(file) }
        )
        binding.rvDownloads.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDownloads.adapter = adapter
    }

    private fun observeData() {
        viewModel.allFiles.observe(viewLifecycleOwner) { files ->
            adapter.submitList(files)
            if (files.isEmpty()) {
                binding.llEmptyDownloads.visible()
                binding.rvDownloads.gone()
            } else {
                binding.llEmptyDownloads.gone()
                binding.rvDownloads.visible()
            }
        }
    }

    private fun setupSearch() {
        binding.etSearchFiles.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().trim()
                if (q.isEmpty()) viewModel.allFiles.observe(viewLifecycleOwner) { adapter.submitList(it) }
                else viewModel.searchFiles(q).observe(viewLifecycleOwner) { adapter.submitList(it) }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun openFile(saved: SavedFile) {
        val file = File(saved.filePath)
        if (!file.exists()) return
        val uri = FileUtils.getContentUri(requireContext(), file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/plain")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Open file"))
    }

    private fun confirmDelete(file: SavedFile) {
        AlertDialog.Builder(requireContext(), R.style.Theme_DragonicDecoder)
            .setTitle("Delete File")
            .setMessage("Delete \"${file.fileName}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteFile(file)
                // Also delete from disk
                File(file.filePath).delete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
