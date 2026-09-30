package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.DownloadedMedia
import com.example.domain.model.MediaType
import com.example.ui.MainViewModel
import com.example.ui.components.GeoBadge
import com.example.ui.components.GeoButton
import com.example.ui.components.GeoCard
import com.example.ui.components.GeometricShape
import com.example.ui.theme.GeoAccentError
import com.example.ui.theme.GeoBlack
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoBorderSubtle
import com.example.ui.theme.GeoLightGrey
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceElevated
import com.example.ui.theme.GeoTextMuted
import com.example.ui.theme.GeoTextPrimary
import com.example.ui.theme.GeoTextSecondary
import com.example.ui.theme.GeoWhite
import com.example.utils.FormatUtils

@Composable
fun DownloadsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mediaList by viewModel.downloadedMediaList.collectAsState()
    var mediaToDelete by remember { mutableStateOf<DownloadedMedia?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeoBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Title header
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = stringResource(R.string.library_title),
                color = GeoWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = "${mediaList.size} item${if (mediaList.size != 1) "s" else ""}",
                color = GeoTextSecondary,
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelSmall
            )
        }

        if (mediaList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(GeoSurfaceElevated, GeometricShape)
                            .border(1.dp, GeoBorder, GeometricShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = GeoTextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.library_empty_title),
                        color = GeoTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.library_empty_desc),
                        color = GeoTextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = mediaList,
                    key = { it.id }
                ) { media ->
                    DownloadedMediaItem(
                        media = media,
                        onPlay = { viewModel.playMedia(media) },
                        onOpen = { FormatUtils.openMediaFile(context, media) },
                        onShare = { FormatUtils.shareMediaFile(context, media) },
                        onDelete = { mediaToDelete = media }
                    )
                }
            }
        }
    }

    // Confirmation dialog before deleting
    mediaToDelete?.let { media ->
        AlertDialog(
            onDismissRequest = { mediaToDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.dialog_delete_title),
                    color = GeoWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_delete_confirm, media.fileName),
                    color = GeoTextSecondary,
                    fontSize = 13.sp
                )
            },
            containerColor = GeoSurface,
            shape = GeometricShape,
            confirmButton = {
                GeoButton(
                    onClick = {
                        viewModel.deleteMedia(media)
                        mediaToDelete = null
                    }
                ) {
                    Text(text = stringResource(R.string.btn_delete), color = GeoBlack, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { mediaToDelete = null }) {
                    Text(text = stringResource(R.string.btn_cancel), color = GeoTextSecondary, fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
private fun DownloadedMediaItem(
    media: DownloadedMedia,
    onPlay: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    GeoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Type badge, filename, and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    GeoBadge(
                        text = if (media.mediaType == MediaType.MP4) "MP4" else "MP3",
                        backgroundColor = if (media.mediaType == MediaType.MP4) GeoSurfaceElevated else GeoBorderSubtle,
                        textColor = GeoLightGrey
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = media.fileName,
                        color = GeoTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = FormatUtils.formatDate(media.downloadTimestamp),
                    color = GeoTextMuted,
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Subtitle: Resolution/Quality, file size, channel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${media.resolutionOrQuality} • ${FormatUtils.formatBytes(media.fileSizeBytes)}",
                    color = GeoTextSecondary,
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = media.channel,
                    color = GeoTextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action Row: Play, Open, Share, Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play Primary Action
                GeoButton(
                    onClick = onPlay,
                    modifier = Modifier.height(38.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.btn_play),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Secondary icon actions (touch target >= 48dp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onOpen,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = stringResource(R.string.btn_open),
                            tint = GeoTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.btn_share),
                            tint = GeoTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = stringResource(R.string.btn_delete),
                            tint = GeoAccentError,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
