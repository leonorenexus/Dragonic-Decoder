package com.dragonic.decoder.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    private val DATE_FORMAT = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun saveToFile(context: Context, content: String, encodingType: String, extension: String = "txt"): File? {
        return try {
            val dir = getOutputDir(context)
            val safeName = encodingType.lowercase().replace("[^a-z0-9]".toRegex(), "_")
            val timestamp = DATE_FORMAT.format(Date())
            val file = File(dir, "${safeName}_$timestamp.$extension")
            FileWriter(file).use { it.write(content) }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun readFileContent(file: File): String? {
        return try {
            file.readText()
        } catch (e: Exception) {
            null
        }
    }

    fun getOutputDir(context: Context): File {
        val dir = File(context.filesDir, "decoded_output")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024      -> "$bytes B"
            bytes < 1024 * 1024 -> "${"%.2f".format(bytes / 1024.0)} KB"
            else              -> "${"%.2f".format(bytes / (1024.0 * 1024))} MB"
        }
    }

    fun getFileExtension(fileName: String): String =
        fileName.substringAfterLast('.', "").uppercase()

    fun createShareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createShareTextIntent(content: String): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
        }

    fun getContentUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
