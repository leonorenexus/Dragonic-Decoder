package com.dragonic.decoder.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SavedFileDao {

    @Query("SELECT * FROM saved_files ORDER BY timestamp DESC")
    fun getAllFiles(): LiveData<List<SavedFile>>

    @Query("SELECT * FROM saved_files WHERE fileName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchFiles(query: String): LiveData<List<SavedFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: SavedFile): Long

    @Delete
    suspend fun deleteFile(file: SavedFile)

    @Query("DELETE FROM saved_files")
    suspend fun clearAllFiles()

    @Query("SELECT COUNT(*) FROM saved_files")
    suspend fun getFileCount(): Int
}
