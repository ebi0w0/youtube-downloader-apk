package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedMediaDao {

    @Query("SELECT * FROM downloaded_media ORDER BY downloadTimestamp DESC")
    fun getAllMedia(): Flow<List<DownloadedMediaEntity>>

    @Query("SELECT * FROM downloaded_media WHERE id = :id LIMIT 1")
    suspend fun getMediaById(id: Long): DownloadedMediaEntity?

    @Query("SELECT * FROM downloaded_media WHERE videoId = :videoId AND mediaType = :mediaType LIMIT 1")
    suspend fun getMediaByVideoIdAndType(videoId: String, mediaType: String): DownloadedMediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: DownloadedMediaEntity): Long

    @Delete
    suspend fun deleteMedia(media: DownloadedMediaEntity)

    @Query("DELETE FROM downloaded_media WHERE id = :id")
    suspend fun deleteMediaById(id: Long)
}
