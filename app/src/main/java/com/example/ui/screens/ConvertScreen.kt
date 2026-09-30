package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.domain.model.AudioQuality
import com.example.domain.model.DownloadState
import com.example.domain.model.MediaType
import com.example.domain.model.YtDlpVideoInfo
import com.example.ui.MainViewModel
import com.example.ui.components.GeoBadge
import com.example.ui.components.GeoButton
import com.example.ui.components.GeoCard
import com.example.ui.components.GeoOutlinedButton
import com.example.ui.components.GeoSegmentedControl
import com.example.ui.components.GeometricShape
import com.example.ui.theme.GeoAccentError
import com.example.ui.theme.GeoAccentSuccess
import com.example.ui.theme.GeoBlack
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoBorderSubtle
import com.example.ui.theme.GeoDarkGrey
import com.example.ui.theme.GeoLightGrey
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceElevated
import com.example.ui.theme.GeoTextMuted
import com.example.ui.theme.GeoTextPrimary
import com.example.ui.theme.GeoTextSecondary
import com.example.ui.theme.GeoWhite
import com.example.utils.FormatUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConvertScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val urlInput by viewModel.urlInput.collectAsState()
    val selectedMediaType by viewModel.selectedMediaType.collectAsState()
    val selectedResolution by viewModel.selectedResolution.collectAsState()
    val selectedAudioQuality by viewModel.selectedAudioQuality.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()

    // Permissions to request for storage and media access
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val anyGranted = permissionsMap.values.any { it }
        if (anyGranted || permissionsMap.isEmpty()) {
            viewModel.startDownload()
        } else {
            Toast.makeText(
                context,
                "Media and storage permissions are required to save and manage downloads",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun startDownloadWithPermissionCheck() {
        val hasPermissions = permissionsToRequest.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }

        if (hasPermissions || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            viewModel.startDownload()
        } else {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeoBlack)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP: Greetings! & App Identity
        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = stringResource(R.string.greeting),
                color = GeoWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = stringResource(R.string.subtitle),
                color = GeoTextSecondary,
                fontSize = 13.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // MAIN: URL Input Field
        GeoCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { viewModel.onUrlChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("url_input_field"),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.paste_url_hint),
                            color = GeoTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = GeoTextPrimary, fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeoLightGrey,
                        unfocusedBorderColor = GeoBorder,
                        focusedContainerColor = GeoSurfaceElevated,
                        unfocusedContainerColor = GeoSurfaceElevated,
                        cursorColor = GeoLightGrey
                    ),
                    shape = GeometricShape,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            viewModel.analyzeUrl()
                        }
                    ),
                    trailingIcon = {
                        if (urlInput.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onClearUrl() }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear_input),
                                    tint = GeoTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            IconButton(onClick = { viewModel.onPasteFromClipboard(context) }) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = stringResource(R.string.paste_from_clipboard),
                                    tint = GeoTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                )

                // Single full-width prominent Analyze URL button (random sample button removed completely)
                GeoButton(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.analyzeUrl()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analyze_button"),
                    enabled = urlInput.isNotBlank() && downloadState !is DownloadState.Analyzing && downloadState !is DownloadState.Downloading
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.btn_analyze),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // STATE HANDLERS
        when (val state = downloadState) {
            is DownloadState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.state_idle),
                        color = GeoTextMuted,
                        fontSize = 13.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            is DownloadState.Analyzing -> {
                GeoCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = GeoLightGrey,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = stringResource(R.string.state_analyzing),
                            color = GeoTextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            is DownloadState.Ready -> {
                MediaReadySection(
                    info = state.info,
                    selectedMediaType = selectedMediaType,
                    selectedResolution = selectedResolution,
                    selectedAudioQuality = selectedAudioQuality,
                    onSelectMediaType = { viewModel.selectMediaType(it) },
                    onSelectResolution = { viewModel.selectResolution(it) },
                    onSelectAudioQuality = { viewModel.selectAudioQuality(it) },
                    onStartDownload = { startDownloadWithPermissionCheck() }
                )
            }

            is DownloadState.Downloading -> {
                GeoCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.state_downloading),
                                color = GeoLightGrey,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${(state.progress.percent * 100).toInt()}%",
                                color = GeoLightGrey,
                                fontSize = 13.sp,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        LinearProgressIndicator(
                            progress = { state.progress.percent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = GeoLightGrey,
                            trackColor = GeoDarkGrey
                        )

                        // Compact metrics: size, speed, ETA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val downloadedStr = FormatUtils.formatBytes(state.progress.downloadedBytes)
                            val totalStr = if (state.progress.totalBytes > 0) FormatUtils.formatBytes(state.progress.totalBytes) else "--"
                            Text(
                                text = "$downloadedStr / $totalStr",
                                color = GeoTextSecondary,
                                fontSize = 11.sp,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = FormatUtils.formatSpeed(state.progress.speedBytesPerSec),
                                color = GeoTextSecondary,
                                fontSize = 11.sp,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "ETA: ${FormatUtils.formatEta(state.progress.etaSeconds)}",
                                color = GeoTextMuted,
                                fontSize = 11.sp,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        // Cancel Button
                        GeoOutlinedButton(
                            onClick = { viewModel.cancelDownload() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = GeoTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.btn_cancel),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            is DownloadState.Converting -> {
                GeoCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = GeoLightGrey,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = state.message,
                            color = GeoTextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            is DownloadState.Completed -> {
                GeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = GeoBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GeoAccentSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.state_completed),
                                color = GeoWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = state.media.title,
                            color = GeoTextPrimary,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${state.media.mediaType.name} • ${state.media.resolutionOrQuality} • ${FormatUtils.formatBytes(state.media.fileSizeBytes)}",
                            color = GeoTextSecondary,
                            fontSize = 11.sp,
                            style = MaterialTheme.typography.labelSmall
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GeoButton(
                                onClick = { viewModel.playMedia(state.media) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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

                            GeoOutlinedButton(
                                onClick = { FormatUtils.openMediaFile(context, state.media) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.btn_open),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is DownloadState.Failed -> {
                var showDetails by remember { mutableStateOf(false) }

                GeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = GeoBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = GeoAccentError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.state_failed),
                                color = GeoAccentError,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = state.message,
                            color = GeoTextPrimary,
                            fontSize = 13.sp
                        )

                        if (!state.details.isNullOrBlank()) {
                            Text(
                                text = if (showDetails) "Hide details" else stringResource(R.string.btn_details),
                                color = GeoTextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.clickable { showDetails = !showDetails }
                            )

                            AnimatedVisibility(visible = showDetails) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(GeoSurfaceElevated, RoundedCornerShape(2.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = state.details,
                                        color = GeoTextMuted,
                                        fontSize = 10.sp,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        GeoButton(
                            onClick = { viewModel.analyzeUrl() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.btn_retry),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            is DownloadState.Cancelled -> {
                GeoCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.state_cancelled),
                            color = GeoTextSecondary,
                            fontSize = 13.sp
                        )
                        GeoOutlinedButton(
                            onClick = { viewModel.resetDownloadState() }
                        ) {
                            Text(text = stringResource(R.string.btn_retry), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MediaReadySection(
    info: YtDlpVideoInfo,
    selectedMediaType: MediaType,
    selectedResolution: String?,
    selectedAudioQuality: AudioQuality,
    onSelectMediaType: (MediaType) -> Unit,
    onSelectResolution: (String) -> Unit,
    onSelectAudioQuality: (AudioQuality) -> Unit,
    onStartDownload: () -> Unit
) {
    GeoCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Media Preview: Thumbnail + Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thumbnail with duration badge
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .aspectRatio(16f / 9f)
                        .clip(GeometricShape)
                        .background(GeoSurfaceElevated)
                        .border(1.dp, GeoBorderSubtle, GeometricShape)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(info.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = info.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (info.durationSeconds > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .background(GeoBlack.copy(alpha = 0.85f), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = FormatUtils.formatDuration(info.durationSeconds),
                                color = GeoWhite,
                                fontSize = 9.sp,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                // Title and Channel
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = info.title,
                        color = GeoTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = info.uploader,
                        color = GeoTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Output Media Type Selector: [ MP4 ] [ MP3 ]
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.media_type_label),
                    color = GeoTextSecondary,
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.labelMedium
                )

                GeoSegmentedControl(
                    items = listOf("MP4", "MP3"),
                    selectedIndex = if (selectedMediaType == MediaType.MP4) 0 else 1,
                    onItemSelected = { index ->
                        onSelectMediaType(if (index == 0) MediaType.MP4 else MediaType.MP3)
                    }
                )
            }

            // Specific options depending on format
            if (selectedMediaType == MediaType.MP4) {
                // Resolution Selector (only available resolutions)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.resolution_label),
                        color = GeoTextSecondary,
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.labelMedium
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        info.availableResolutions.forEach { res ->
                            val isSelected = res == selectedResolution
                            Box(
                                modifier = Modifier
                                    .clip(GeometricShape)
                                    .background(if (isSelected) GeoLightGrey else GeoSurfaceElevated)
                                    .border(1.dp, if (isSelected) GeoLightGrey else GeoBorder, GeometricShape)
                                    .clickable { onSelectResolution(res) }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = res,
                                    color = if (isSelected) GeoBlack else GeoTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            } else {
                // MP3 Audio Quality Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.audio_quality_label),
                        color = GeoTextSecondary,
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.labelMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AudioQuality.entries.forEach { quality ->
                            val isSelected = quality == selectedAudioQuality
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(GeometricShape)
                                    .background(if (isSelected) GeoLightGrey else GeoSurfaceElevated)
                                    .border(1.dp, if (isSelected) GeoLightGrey else GeoBorder, GeometricShape)
                                    .clickable { onSelectAudioQuality(quality) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = quality.label,
                                    color = if (isSelected) GeoBlack else GeoTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Primary Download Button
            GeoButton(
                onClick = onStartDownload,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_download_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_download),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
