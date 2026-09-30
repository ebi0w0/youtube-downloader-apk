package com.example.data.downloader

import android.content.Context
import com.example.data.repository.MediaRepository
import com.example.domain.model.AudioQuality
import com.example.domain.model.DownloadProgress
import com.example.domain.model.DownloadState
import com.example.domain.model.DownloadedMedia
import com.example.domain.model.MediaType
import com.example.domain.model.YtDlpVideoInfo
import com.example.settings.AppSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class DownloadManager(
    private val context: Context,
    private val ytDlpService: YtDlpService,
    private val mediaRepository: MediaRepository,
    private val appSettings: AppSettings,
    private val scope: CoroutineScope
) {

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private var activeJob: Job? = null
    private var currentVideoInfo: YtDlpVideoInfo? = null

    fun analyzeUrl(url: String) {
        if (url.isBlank()) {
            _downloadState.value = DownloadState.Failed("Please enter a valid YouTube URL.")
            return
        }

        activeJob?.cancel()
        _downloadState.value = DownloadState.Analyzing

        activeJob = scope.launch(Dispatchers.IO) {
            val result = ytDlpService.extractVideoInfo(url)
            result.fold(
                onSuccess = { info ->
                    currentVideoInfo = info
                    _downloadState.value = DownloadState.Ready(info)
                },
                onFailure = { error ->
                    _downloadState.value = DownloadState.Failed(
                        message = error.message ?: "Failed to analyze URL",
                        details = error.localizedMessage
                    )
                }
            )
        }
    }

    fun startDownload(
        mediaType: MediaType,
        selectedResolution: String?,
        audioQuality: AudioQuality
    ) {
        val info = currentVideoInfo ?: return

        activeJob?.cancel()
        _downloadState.value = DownloadState.Downloading(
            info = info,
            progress = DownloadProgress(percent = 0.05f, statusText = "Initializing yt-dlp…")
        )

        activeJob = scope.launch(Dispatchers.IO) {
            try {
                val targetDir = File(appSettings.downloadPath.value)
                val request = DownloadRequest(
                    videoInfo = info,
                    mediaType = mediaType,
                    selectedResolution = selectedResolution,
                    audioQuality = audioQuality,
                    targetDirectory = targetDir
                )

                val result = ytDlpService.downloadAndConvert(request) { progress ->
                    _downloadState.value = DownloadState.Downloading(info, progress)
                }

                result.fold(
                    onSuccess = { file ->
                        var trueDuration = if (info.durationSeconds > 0) info.durationSeconds else 15L
                        try {
                            val retriever = android.media.MediaMetadataRetriever()
                            retriever.setDataSource(file.absolutePath)
                            val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                            val ms = durStr?.toLongOrNull() ?: 0L
                            if (ms > 0) {
                                trueDuration = ms / 1000L
                            }
                            retriever.release()
                        } catch (_: Exception) {
                        }

                        val downloadedMedia = DownloadedMedia(
                            videoId = info.id,
                            title = info.title,
                            channel = info.uploader,
                            durationSeconds = trueDuration,
                            thumbnailUrl = info.thumbnailUrl,
                            filePath = file.absolutePath,
                            fileName = file.name,
                            mediaType = mediaType,
                            resolutionOrQuality = if (mediaType == MediaType.MP4) (selectedResolution ?: "720p") else audioQuality.label,
                            fileSizeBytes = file.length(),
                            downloadTimestamp = System.currentTimeMillis(),
                            sourceUrl = info.webpageUrl
                        )

                        // Save to database
                        val savedId = mediaRepository.saveMedia(downloadedMedia)
                        val finalMedia = downloadedMedia.copy(id = savedId)

                        _downloadState.value = DownloadState.Completed(finalMedia)
                    },
                    onFailure = { error ->
                        if (error is CancellationException) {
                            _downloadState.value = DownloadState.Cancelled
                        } else {
                            _downloadState.value = DownloadState.Failed(
                                message = error.message ?: "Download failed",
                                details = error.localizedMessage
                            )
                        }
                    }
                )
            } catch (e: CancellationException) {
                _downloadState.value = DownloadState.Cancelled
            } catch (e: Exception) {
                _downloadState.value = DownloadState.Failed(
                    message = e.message ?: "An error occurred during download",
                    details = e.localizedMessage
                )
            }
        }
    }

    fun cancelDownload() {
        activeJob?.cancel()
        _downloadState.value = DownloadState.Cancelled
    }

    fun resetState() {
        activeJob?.cancel()
        currentVideoInfo = null
        _downloadState.value = DownloadState.Idle
    }
}
