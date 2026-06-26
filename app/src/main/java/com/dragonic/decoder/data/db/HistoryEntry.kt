package com.dragonic.decoder.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val encodingType: String,
    val inputPreview: String,
    val outputPreview: String,
    val fullInput: String,
    val fullOutput: String,
    val status: String,      // SUCCESS, FAILED
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)
