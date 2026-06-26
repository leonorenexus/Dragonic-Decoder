package com.dragonic.decoder.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_files")
data class SavedFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val encodingType: String,
    val timestamp: Long = System.currentTimeMillis()
)
