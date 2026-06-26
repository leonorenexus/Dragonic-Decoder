package com.dragonic.decoder.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dragonic.decoder.R
import com.dragonic.decoder.data.db.HistoryEntry
import com.dragonic.decoder.databinding.ItemHistoryBinding
import com.dragonic.decoder.utils.toRelativeTime
import com.dragonic.decoder.utils.truncate

class HistoryAdapter(
    private val onItemClick: (HistoryEntry) -> Unit,
    private val onDeleteClick: (HistoryEntry) -> Unit
) : ListAdapter<HistoryEntry, HistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: HistoryEntry) {
            binding.tvHistoryType.text = "${entry.encodingType} Decode"
            binding.tvHistoryPreview.text = entry.outputPreview.truncate(60)
            binding.tvHistoryTime.text = entry.timestamp.toRelativeTime()
            binding.tvHistoryDuration.text = "${entry.durationMs} ms"

            when (entry.status) {
                "SUCCESS" -> {
                    binding.tvHistoryStatus.text = "SUCCESS"
                    binding.tvHistoryStatus.setTextColor(
                        binding.root.context.getColor(R.color.status_success))
                    binding.tvHistoryStatus.setBackgroundResource(R.drawable.bg_badge_success)
                }
                "FAILED" -> {
                    binding.tvHistoryStatus.text = "FAILED"
                    binding.tvHistoryStatus.setTextColor(
                        binding.root.context.getColor(R.color.status_failed))
                    binding.tvHistoryStatus.setBackgroundResource(R.drawable.bg_badge_failed)
                }
                else -> {
                    binding.tvHistoryStatus.text = "RUNNING"
                    binding.tvHistoryStatus.setTextColor(
                        binding.root.context.getColor(R.color.status_running))
                    binding.tvHistoryStatus.setBackgroundResource(R.drawable.bg_badge_running)
                }
            }

            binding.root.setOnClickListener { onItemClick(entry) }
            binding.root.setOnLongClickListener { onDeleteClick(entry); true }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryEntry>() {
        override fun areItemsTheSame(old: HistoryEntry, new: HistoryEntry) = old.id == new.id
        override fun areContentsTheSame(old: HistoryEntry, new: HistoryEntry) = old == new
    }
}
