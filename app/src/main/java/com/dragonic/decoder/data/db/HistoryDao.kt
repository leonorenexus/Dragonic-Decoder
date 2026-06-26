package com.dragonic.decoder.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): LiveData<List<HistoryEntry>>

    @Query("SELECT * FROM history WHERE encodingType LIKE '%' || :query || '%' OR inputPreview LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): LiveData<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: HistoryEntry): Long

    @Delete
    suspend fun deleteHistory(entry: HistoryEntry)

    @Query("DELETE FROM history")
    suspend fun clearAllHistory()

    @Query("SELECT COUNT(*) FROM history")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM history WHERE status = 'SUCCESS'")
    suspend fun getSuccessCount(): Int

    @Query("SELECT COUNT(*) FROM history WHERE status = 'FAILED'")
    suspend fun getFailedCount(): Int

    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEntry(): HistoryEntry?
}
