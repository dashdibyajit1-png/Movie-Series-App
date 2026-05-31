package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.CuratedReleases
import com.example.data.ReleaseItem
import com.example.data.TrackedRelease
import com.example.data.TrackedReleaseRepository
import com.example.data.GeminiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AssistantState {
    object Idle : AssistantState
    object Loading : AssistantState
    data class Success(val releases: List<ReleaseItem>) : AssistantState
    data class Error(val message: String) : AssistantState
}

class StreamTrackerViewModel(application: Application) : AndroidViewModel(application) {

    // Database Setup
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "stream_tracker_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = TrackedReleaseRepository(db.trackedReleaseDao)

    // Flow of all tracked releases from ROOM
    val allTrackedReleases: StateFlow<List<TrackedRelease>> = repository.allTrackedReleases
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- RELEASES FEED TAB STATES ---
    var feedPlatformFilter by mutableStateOf("All") // "All", "Netflix", "Prime Video"
    var feedSearchQuery by mutableStateOf("")

    // --- WATCHLIST TRACKER TAB STATES ---
    var watchlistStatusFilter by mutableStateOf("All") // "All", "To Watch", "Watching", "Completed"
    var watchlistSearchQuery by mutableStateOf("")


    // --- AI ASSISTANT SPECS ---
    var aiQueryText by mutableStateOf("")
    var assistantState by mutableStateOf<AssistantState>(AssistantState.Idle)

    // --- DIALOG & SHEET SELECTIONS ---
    var editingRelease by mutableStateOf<TrackedRelease?>(null)
    var showAddCustomDialog by mutableStateOf(false)

    // --- DATABASE ACTIONS ---
    fun trackRelease(item: ReleaseItem) {
        viewModelScope.launch {
            val alreadyTracked = repository.getByTitle(item.title)
            if (alreadyTracked == null) {
                repository.insert(
                    TrackedRelease(
                        title = item.title,
                        platform = item.platform,
                        releaseDate = item.releaseDate,
                        imageUrl = item.imageUrl,
                        genre = item.genre,
                        synopsis = item.synopsis,
                        status = "To Watch"
                    )
                )
            }
        }
    }

    fun untrackRelease(title: String) {
        viewModelScope.launch {
            repository.deleteByTitle(title)
        }
    }

    fun untrackRelease(release: TrackedRelease) {
        viewModelScope.launch {
            repository.delete(release)
        }
    }

    fun updateReleaseDetails(id: Int, status: String, rating: Float?, notes: String?) {
        viewModelScope.launch {
            val matching = allTrackedReleases.value.find { it.id == id }
            if (matching != null) {
                repository.update(
                    matching.copy(
                        status = status,
                        rating = rating,
                        userNotes = notes
                    )
                )
            }
        }
    }

    fun addCustomRelease(title: String, platform: String, genre: String, releaseDate: String, synopsis: String) {
        viewModelScope.launch {
            repository.insert(
                TrackedRelease(
                    title = title,
                    platform = platform,
                    genre = genre,
                    releaseDate = releaseDate,
                    synopsis = synopsis,
                    status = "To Watch",
                    isCustom = true
                )
            )
        }
    }

    // --- AI SERVICE TRIGGER ---
    fun searchWithAI() {
        if (aiQueryText.trim().isEmpty()) return
        viewModelScope.launch {
            assistantState = AssistantState.Loading
            try {
                val results = GeminiClient.askAssistant(aiQueryText)
                assistantState = AssistantState.Success(results)
            } catch (e: Exception) {
                assistantState = AssistantState.Error("Failed to fetch response: ${e.localizedMessage}")
            }
        }
    }

    fun setAIQueryAndSearch(suggestedQuery: String) {
        aiQueryText = suggestedQuery
        searchWithAI()
    }
}
