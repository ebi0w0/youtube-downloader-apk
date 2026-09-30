package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.DownloadedMedia
import com.example.domain.model.MediaType

@Entity(tableName = "downloaded_media")
data class DownloadedMediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val videoId: String,
    val title: String,
    val channel: String,
    val durationSeconds: Long,
    val thumbnailUrl: String,
    val filePath: String,
    val fileName: String,
    val mediaType: String, // "MP4" or "MP3"
    val resolutionOrQuality: String,
    val fileSizeBytes: Long,
    val downloadTimestamp: Long,
    val sourceUrl: String
) {
    fun toDomain(): DownloadedMedia = DownloadedMedia(
        id = id,
        videoId = videoId,
        title = title,
        channel = channel,
        durationSeconds = durationSeconds,
        thumbnailUrl = thumbnailUrl,
        filePath = filePath,
        fileName = fileName,
        mediaType = if (mediaType == "MP3") MediaType.MP3 else MediaType.MP4,
        resolutionOrQuality = resolutionOrQuality,
        fileSizeBytes = fileSizeBytes,
        downloadTimestamp = downloadTimestamp,
        sourceUrl = sourceUrl
    )

    companion object {
        fun fromDomain(domain: DownloadedMedia): DownloadedMediaEntity = DownloadedMediaEntity(
            id = domain.id,
            videoId = domain.videoId,
            title = domain.title,
            channel = domain.channel,
            durationSeconds = domain.durationSeconds,
            thumbnailUrl = domain.thumbnailUrl,
            filePath = domain.filePath,
            fileName = domain.fileName,
            mediaType = domain.mediaType.name,
            resolutionOrQuality = domain.resolutionOrQuality,
            fileSizeBytes = domain.fileSizeBytes,
            downloadTimestamp = domain.downloadTimestamp,
            sourceUrl = domain.sourceUrl
        )
    }
}
