package com.example.domain.model

enum class MediaType {
    MP4,
    MP3
}

enum class AudioQuality(val bitrateKbps: Int, val label: String) {
    HIGH(320, "320 kbps"),
    STANDARD(192, "192 kbps"),
    COMPACT(128, "128 kbps")
}

data class YtDlpFormat(
    val formatId: String,
    val ext: String,
    val resolution: String,
    val width: Int? = null,
    val height: Int? = null,
    val fps: Double? = null,
    val vcodec: String? = null,
    val acodec: String? = null,
    val filesize: Long? = null,
    val url: String? = null,
    val formatNote: String? = null
) {
    val isVideoOnly: Boolean
        get() = vcodec != null && vcodec != "none" && (acodec == null || acodec == "none")

    val isAudioOnly: Boolean
        get() = acodec != null && acodec != "none" && (vcodec == null || vcodec == "none")

    val hasVideo: Boolean
        get() = vcodec != null && vcodec != "none"

    val hasAudio: Boolean
        get() = acodec != null && acodec != "none"
}

data class YtDlpVideoInfo(
    val id: String,
    val title: String,
    val uploader: String,
    val durationSeconds: Long,
    val thumbnailUrl: String,
    val webpageUrl: String,
    val description: String = "",
    val formats: List<YtDlpFormat> = emptyList(),
    val availableResolutions: List<String> = emptyList()
)

data class DownloadProgress(
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val etaSeconds: Long = 0L,
    val percent: Float = 0f,
    val statusText: String = ""
)

sealed class DownloadState {
    data object Idle : DownloadState()
    data object Analyzing : DownloadState()
    data class Ready(val info: YtDlpVideoInfo) : DownloadState()
    data class Downloading(val info: YtDlpVideoInfo, val progress: DownloadProgress) : DownloadState()
    data class Converting(val info: YtDlpVideoInfo, val message: String) : DownloadState()
    data class Completed(val media: DownloadedMedia) : DownloadState()
    data class Failed(val message: String, val details: String? = null) : DownloadState()
    data object Cancelled : DownloadState()
}

data class DownloadedMedia(
    val id: Long = 0,
    val videoId: String,
    val title: String,
    val channel: String,
    val durationSeconds: Long,
    val thumbnailUrl: String,
    val filePath: String,
    val fileName: String,
    val mediaType: MediaType,
    val resolutionOrQuality: String,
    val fileSizeBytes: Long,
    val downloadTimestamp: Long = System.currentTimeMillis(),
    val sourceUrl: String
)
