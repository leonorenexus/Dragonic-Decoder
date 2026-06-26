package com.dragonic.decoder.ui.history

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragonic.decoder.R
import com.dragonic.decoder.data.db.HistoryEntry
import com.dragonic.decoder.databinding.FragmentHistoryBinding
import com.dragonic.decoder.ui.decoder.DecoderDetailActivity
import com.dragonic.decoder.utils.DecoderEngine
import com.dragonic.decoder.utils.animateCounter
import com.dragonic.decoder.utils.gone
import com.dragonic.decoder.utils.visible

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter
    private var prevTotal = 0; private var prevSuccess = 0; private var prevFailed = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        observeData()

        binding.tvClearHistory.setOnClickListener { showClearConfirmation() }
        binding.ivExportHistory.setOnClickListener { exportHistory() }
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            onItemClick = { entry -> openEntry(entry) },
            onDeleteClick = { entry -> viewModel.deleteEntry(entry) }
        )
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearchHistory.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().trim()
                if (q.isEmpty()) {
                    viewModel.allHistory.observe(viewLifecycleOwner) { adapter.submitList(it) }
                } else {
                    viewModel.searchHistory(q).observe(viewLifecycleOwner) { adapter.submitList(it) }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observeData() {
        viewModel.allHistory.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isEmpty()) {
                binding.llEmptyHistory.visible()
                binding.rvHistory.gone()
            } else {
                binding.llEmptyHistory.gone()
                binding.rvHistory.visible()
            }
        }

        viewModel.stats.observe(viewLifecycleOwner) { (total, success, failed) ->
            animateCounter(prevTotal, total) { binding.tvTotalCount.text = it.toString() }
            animateCounter(prevSuccess, success) { binding.tvSuccessCount.text = it.toString() }
            animateCounter(prevFailed, failed) { binding.tvFailedCount.text = it.toString() }
            binding.tvPendingCount.text = "0"
            prevTotal = total; prevSuccess = success; prevFailed = failed
        }
    }

    private fun openEntry(entry: HistoryEntry) {
        val intent = Intent(requireContext(), DecoderDetailActivity::class.java).apply {
            putExtra(DecoderDetailActivity.EXTRA_INPUT, entry.fullInput)
            putExtra(DecoderDetailActivity.EXTRA_TYPE, entry.encodingType)
            putExtra(DecoderDetailActivity.EXTRA_IS_ENCODE, false)
        }
        startActivity(intent)
    }

    private fun showClearConfirmation() {
        AlertDialog.Builder(requireContext(), R.style.Theme_DragonicDecoder)
            .setTitle("Clear History")
            .setMessage("Delete all decode history? This cannot be undone.")
            .setPositiveButton("Clear") { _, _ -> viewModel.clearAll() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportHistory() {
        val items = adapter.currentList
        if (items.isEmpty()) return
        val sb = StringBuilder("Dragonic Decoder - History Export\n\n")
        items.forEach { entry ->
            sb.appendLine("Type: ${entry.encodingType}")
            sb.appendLine("Status: ${entry.status}")
            sb.appendLine("Duration: ${entry.durationMs}ms")
            sb.appendLine("Input: ${entry.inputPreview}")
            sb.appendLine("Output: ${entry.outputPreview}")
            sb.appendLine("---")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            putExtra(Intent.EXTRA_SUBJECT, "Dragonic Decoder History")
        }
        startActivity(Intent.createChooser(intent, "Export History"))
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
