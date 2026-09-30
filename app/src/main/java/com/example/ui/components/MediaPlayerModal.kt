package com.example.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.domain.model.MediaType
import com.example.player.MediaPlayerManager
import com.example.player.PlayerPlaybackState
import com.example.ui.theme.GeoBlack
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoBorderSubtle
import com.example.ui.theme.GeoLightGrey
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceElevated
import com.example.ui.theme.GeoTextMuted
import com.example.ui.theme.GeoTextPrimary
import com.example.ui.theme.GeoTextSecondary
import com.example.utils.FormatUtils

@OptIn(UnstableApi::class)
@Composable
fun MediaPlayerModal(
    playerState: PlayerPlaybackState,
    playerManager: MediaPlayerManager,
    onDismiss: () -> Unit
) {
    val media = playerState.media ?: return
    val isVideo = media.mediaType == MediaType.MP4
    val isFullscreen = playerState.isFullscreen

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = !isFullscreen,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = if (isFullscreen) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, GeoBorder, RoundedCornerShape(8.dp))
            },
            color = GeoBlack
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GeoSurface)
                        .border(1.dp, GeoBorderSubtle)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        GeoBadge(
                            text = if (isVideo) "MP4" else "MP3",
                            backgroundColor = GeoSurfaceElevated,
                            textColor = GeoLightGrey
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = media.title,
                            color = GeoTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close player",
                            tint = GeoTextSecondary
                        )
                    }
                }

                // Media Display Area
                if (isVideo) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isFullscreen) Modifier.weight(1f) else Modifier.aspectRatio(16f / 9f)
                            )
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        val context = LocalContext.current
                        val playerView = remember {
                            PlayerView(context).apply {
                                useController = false
                                player = playerManager.exoPlayer
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        }

                        DisposableEffect(playerManager.exoPlayer) {
                            playerView.player = playerManager.exoPlayer
                            onDispose {
                                playerView.player = null
                            }
                        }

                        AndroidView(
                            factory = { playerView },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // MP3 Audio View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(GeoSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(GeoSurfaceElevated)
                                    .border(1.dp, GeoBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = GeoLightGrey,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = media.channel,
                                color = GeoTextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Quality: ${media.resolutionOrQuality}",
                                color = GeoTextMuted,
                                fontSize = 11.sp,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                // Controls Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GeoSurface)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    // Timeline Slider
                    val dur = playerState.durationMs.coerceAtLeast(1L)
                    val pos = playerState.currentPositionMs.coerceIn(0L, dur)
                    val progressRatio = (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)

                    Slider(
                        value = progressRatio,
                        onValueChange = { newRatio ->
                            playerManager.seekTo((newRatio * dur).toLong())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = GeoLightGrey,
                            activeTrackColor = GeoLightGrey,
                            inactiveTrackColor = GeoBorder
                        )
                    )

                    // Time display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = FormatUtils.formatDurationMs(pos),
                            color = GeoTextSecondary,
                            fontSize = 11.sp,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = FormatUtils.formatDurationMs(dur),
                            color = GeoTextMuted,
                            fontSize = 11.sp,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buttons: Mute, Play/Pause, Fullscreen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute button
                        IconButton(
                            onClick = { playerManager.toggleMute() },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (playerState.isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Mute/Unmute",
                                tint = GeoTextSecondary
                            )
                        }

                        // Play/Pause button
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(GeometricShape)
                                .background(GeoLightGrey)
                                .border(1.dp, GeoBorder, GeometricShape),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { playerManager.togglePlayPause() },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                    tint = GeoBlack,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Fullscreen button (for video)
                        if (isVideo) {
                            IconButton(
                                onClick = { playerManager.toggleFullscreen() },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Toggle Fullscreen",
                                    tint = GeoTextSecondary
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(44.dp))
                        }
                    }
                }
            }
        }
    }
}
