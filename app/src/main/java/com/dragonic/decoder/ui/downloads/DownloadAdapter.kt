package com.dragonic.decoder.ui.downloads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dragonic.decoder.data.db.SavedFile
import com.dragonic.decoder.databinding.ItemDownloadFileBinding
import com.dragonic.decoder.utils.FileUtils
import com.dragonic.decoder.utils.toRelativeTime

class DownloadsAdapter(
    private val onDownloadClick: (SavedFile) -> Unit,
    private val onDeleteClick: (SavedFile) -> Unit
) : ListAdapter<SavedFile, DownloadsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDownloadFileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemDownloadFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(file: SavedFile) {
            binding.tvFileName.text  = file.fileName
            binding.tvFileSize.text  = FileUtils.formatFileSize(file.fileSize)
            binding.tvFileDate.text  = file.timestamp.toRelativeTime()

            binding.root.setOnClickListener { onDownloadClick(file) }
            binding.ivDownload.setOnClickListener { onDownloadClick(file) }
            binding.ivDeleteFile.setOnClickListener { onDeleteClick(file) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SavedFile>() {
        override fun areItemsTheSame(old: SavedFile, new: SavedFile) = old.id == new.id
        override fun areContentsTheSame(old: SavedFile, new: SavedFile) = old == new
    }
}
