package com.example.player

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.domain.model.DownloadedMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class PlayerPlaybackState(
    val media: DownloadedMedia? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isMuted: Boolean = false,
    val isFullscreen: Boolean = false,
    val isEnded: Boolean = false
)

class MediaPlayerManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
                }

                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        val dur = exoPlayer.duration
                        if (dur > 0 && dur != C.TIME_UNSET) {
                            _playbackState.value = _playbackState.value.copy(durationMs = dur)
                        }
                    } else if (state == Player.STATE_ENDED) {
                        _playbackState.value = _playbackState.value.copy(
                            isPlaying = false,
                            isEnded = true,
                            currentPositionMs = _playbackState.value.durationMs
                        )
                    }
                }
            })
        }
    }

    private val _playbackState = MutableStateFlow(PlayerPlaybackState())
    val playbackState: StateFlow<PlayerPlaybackState> = _playbackState.asStateFlow()

    private var progressTrackerJob: Job? = null

    fun playMedia(media: DownloadedMedia) {
        val file = File(media.filePath)
        val uri = if (file.exists()) {
            Uri.fromFile(file)
        } else {
            Uri.parse(media.sourceUrl)
        }

        var resolvedDurationMs = if (media.durationSeconds > 0) media.durationSeconds * 1000L else 0L

        // Extract metadata duration directly from file headers using Android MediaMetadataRetriever
        if (file.exists()) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(file.absolutePath)
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val parsedDur = durStr?.toLongOrNull() ?: 0L
                if (parsedDur > 0) {
                    resolvedDurationMs = parsedDur
                }
                retriever.release()
            } catch (_: Exception) {
            }
        }

        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        _playbackState.value = PlayerPlaybackState(
            media = media,
            isPlaying = true,
            currentPositionMs = 0L,
            durationMs = resolvedDurationMs
        )

        startProgressTracking()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            _playbackState.value = _playbackState.value.copy(isPlaying = false)
        } else {
            if (_playbackState.value.isEnded) {
                exoPlayer.seekTo(0)
                _playbackState.value = _playbackState.value.copy(isEnded = false)
            }
            exoPlayer.play()
            _playbackState.value = _playbackState.value.copy(isPlaying = true)
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
    }

    fun toggleMute() {
        val newMuted = !_playbackState.value.isMuted
        exoPlayer.volume = if (newMuted) 0f else 1f
        _playbackState.value = _playbackState.value.copy(isMuted = newMuted)
    }

    fun toggleFullscreen() {
        _playbackState.value = _playbackState.value.copy(
            isFullscreen = !_playbackState.value.isFullscreen
        )
    }

    fun stop() {
        progressTrackerJob?.cancel()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _playbackState.value = PlayerPlaybackState()
    }

    fun release() {
        progressTrackerJob?.cancel()
        exoPlayer.release()
    }

    private fun startProgressTracking() {
        progressTrackerJob?.cancel()
        progressTrackerJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                val isPlaying = exoPlayer.isPlaying
                val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                val dur = exoPlayer.duration
                val validDur = if (dur > 0 && dur != C.TIME_UNSET) {
                    dur
                } else {
                    _playbackState.value.durationMs
                }

                _playbackState.value = _playbackState.value.copy(
                    isPlaying = isPlaying,
                    currentPositionMs = pos,
                    durationMs = validDur
                )
                delay(200)
            }
        }
    }
}
