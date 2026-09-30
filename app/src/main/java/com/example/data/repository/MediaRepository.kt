package com.example.data.repository

import com.example.data.local.DownloadedMediaDao
import com.example.data.local.DownloadedMediaEntity
import com.example.domain.model.DownloadedMedia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class MediaRepository(private val mediaDao: DownloadedMediaDao) {

    val allMedia: Flow<List<DownloadedMedia>> = mediaDao.getAllMedia().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getMediaById(id: Long): DownloadedMedia? {
        return mediaDao.getMediaById(id)?.toDomain()
    }

    suspend fun saveMedia(media: DownloadedMedia): Long {
        return mediaDao.insertMedia(DownloadedMediaEntity.fromDomain(media))
    }

    suspend fun deleteMedia(media: DownloadedMedia) {
        // Also delete actual physical file from storage if it exists
        try {
            val file = File(media.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {
        }
        mediaDao.deleteMediaById(media.id)
    }
}
