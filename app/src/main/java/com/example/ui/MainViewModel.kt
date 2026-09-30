package com.example.ui

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.downloader.DownloadManager
import com.example.data.downloader.YtDlpService
import com.example.data.local.AppDatabase
import com.example.data.repository.MediaRepository
import com.example.domain.model.AudioQuality
import com.example.domain.model.DownloadState
import com.example.domain.model.DownloadedMedia
import com.example.domain.model.MediaType
import com.example.player.MediaPlayerManager
import com.example.player.PlayerPlaybackState
import com.example.settings.AppSettings
import com.example.utils.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavigationTab {
    CONVERT,
    DOWNLOADS,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val appSettings = AppSettings(application)
    private val database = AppDatabase.getInstance(application)
    val mediaRepository = MediaRepository(database.downloadedMediaDao())
    val ytDlpService = YtDlpService(application)

    val downloadManager = DownloadManager(
        context = application,
        ytDlpService = ytDlpService,
        mediaRepository = mediaRepository,
        appSettings = appSettings,
        scope = viewModelScope
    )

    val playerManager = MediaPlayerManager(
        context = application,
        scope = viewModelScope
    )

    // Navigation
    private val _currentTab = MutableStateFlow(NavigationTab.CONVERT)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    // URL input
    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    // Random funny word status
    private val _randomFunnyWord = MutableStateFlow("")
    val randomFunnyWord: StateFlow<String> = _randomFunnyWord.asStateFlow()

    // Format choices
    private val _selectedMediaType = MutableStateFlow(MediaType.MP4)
    val selectedMediaType: StateFlow<MediaType> = _selectedMediaType.asStateFlow()

    private val _selectedResolution = MutableStateFlow<String?>("720p")
    val selectedResolution: StateFlow<String?> = _selectedResolution.asStateFlow()

    private val _selectedAudioQuality = MutableStateFlow(AudioQuality.HIGH)
    val selectedAudioQuality: StateFlow<AudioQuality> = _selectedAudioQuality.asStateFlow()

    // Download state from manager
    val downloadState: StateFlow<DownloadState> = downloadManager.downloadState

    // Library from repository
    val downloadedMediaList: StateFlow<List<DownloadedMedia>> = mediaRepository.allMedia
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Player state
    val playerPlaybackState: StateFlow<PlayerPlaybackState> = playerManager.playbackState

    init {
        // Observe download state: if autoOpen is enabled and download completed, launch player
        viewModelScope.launch {
            downloadState.collect { state ->
                if (state is DownloadState.Ready) {
                    val available = state.info.availableResolutions
                    if (available.isNotEmpty() && !available.contains(_selectedResolution.value)) {
                        _selectedResolution.value = available.first()
                    }
                } else if (state is DownloadState.Completed) {
                    if (appSettings.autoOpen.value) {
                        playerManager.playMedia(state.media)
                    }
                }
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun onUrlChange(newUrl: String) {
        _urlInput.value = newUrl
    }

    fun onPasteFromClipboard(context: Context) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val pasted = clip.getItemAt(0).text?.toString() ?: ""
                if (pasted.isNotBlank()) {
                    _urlInput.value = pasted.trim()
                    analyzeUrl()
                }
            }
        } catch (_: Exception) {
        }
    }

    fun onClearUrl() {
        _urlInput.value = ""
        downloadManager.resetState()
    }

    fun onGenerateRandomWords() {
        val sample = SampleData.getRandomSample()
        _randomFunnyWord.value = sample.funnyPhrase
        _urlInput.value = sample.url
        downloadManager.analyzeUrl(sample.url)
    }

    fun analyzeUrl() {
        val url = _urlInput.value.trim()
        if (url.isNotEmpty()) {
            downloadManager.analyzeUrl(url)
        }
    }

    fun selectMediaType(mediaType: MediaType) {
        _selectedMediaType.value = mediaType
    }

    fun selectResolution(resolution: String) {
        _selectedResolution.value = resolution
    }

    fun selectAudioQuality(quality: AudioQuality) {
        _selectedAudioQuality.value = quality
    }

    fun startDownload() {
        downloadManager.startDownload(
            mediaType = _selectedMediaType.value,
            selectedResolution = _selectedResolution.value,
            audioQuality = _selectedAudioQuality.value
        )
    }

    fun cancelDownload() {
        downloadManager.cancelDownload()
    }

    fun resetDownloadState() {
        downloadManager.resetState()
    }

    fun playMedia(media: DownloadedMedia) {
        playerManager.playMedia(media)
    }

    fun closePlayer() {
        playerManager.stop()
    }

    fun deleteMedia(media: DownloadedMedia) {
        viewModelScope.launch {
            if (playerPlaybackState.value.media?.id == media.id) {
                playerManager.stop()
            }
            mediaRepository.deleteMedia(media)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
