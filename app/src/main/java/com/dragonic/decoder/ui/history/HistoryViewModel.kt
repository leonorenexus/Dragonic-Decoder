package com.dragonic.decoder.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dragonic.decoder.data.db.HistoryEntry
import com.dragonic.decoder.data.repository.DecoderRepository
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DecoderRepository(application)

    val allHistory: LiveData<List<HistoryEntry>> = repository.getAllHistory()

    private val _searchResults = MutableLiveData<LiveData<List<HistoryEntry>>>()
    val searchResults: MutableLiveData<LiveData<List<HistoryEntry>>> = _searchResults

    private val _stats = MutableLiveData<Triple<Int, Int, Int>>()
    val stats: LiveData<Triple<Int, Int, Int>> = _stats

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _stats.value = repository.getHistoryStats()
        }
    }

    fun searchHistory(query: String): LiveData<List<HistoryEntry>> =
        repository.searchHistory(query)

    fun deleteEntry(entry: HistoryEntry) {
        viewModelScope.launch {
            repository.deleteHistory(entry)
            loadStats()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _stats.value = Triple(0, 0, 0)
        }
    }
}
