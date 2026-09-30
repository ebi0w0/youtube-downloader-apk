package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.settings.AppSettings
import com.example.ui.MainViewModel
import com.example.ui.components.GeoButton
import com.example.ui.components.GeoCard
import com.example.ui.components.GeoOutlinedButton
import com.example.ui.components.GeometricShape
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
import java.io.File

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val downloadPath by viewModel.appSettings.downloadPath.collectAsState()
    val autoOpen by viewModel.appSettings.autoOpen.collectAsState()
    val currentLang by viewModel.appSettings.language.collectAsState()

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            val resolvedPath = uri.path ?: uri.toString()
            viewModel.appSettings.setDownloadPath(resolvedPath)
            Toast.makeText(context, "Download location updated", Toast.LENGTH_SHORT).show()
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeoBlack)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Settings Header
        Text(
            text = stringResource(R.string.settings_title),
            color = GeoWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.displaySmall
        )

        // SECTION 1: DOWNLOADS
        SettingsSection(
            title = stringResource(R.string.settings_section_downloads),
            icon = Icons.Default.Tune
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Download Location
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.settings_download_dir_title),
                        color = GeoTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Path Display Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(GeometricShape)
                            .background(GeoSurfaceElevated)
                            .border(1.dp, GeoBorderSubtle, GeometricShape)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = downloadPath,
                            color = GeoTextSecondary,
                            fontSize = 11.sp,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // Folder Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GeoOutlinedButton(
                            onClick = { folderPickerLauncher.launch(null) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.settings_btn_choose_folder),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        GeoOutlinedButton(
                            onClick = { viewModel.appSettings.resetDownloadPath() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.settings_btn_reset_folder),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = GeoBorderSubtle, thickness = 1.dp)

                // Auto-Open toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_auto_open_title),
                            color = GeoTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.settings_auto_open_desc),
                            color = GeoTextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = autoOpen,
                        onCheckedChange = { viewModel.appSettings.setAutoOpen(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GeoBlack,
                            checkedTrackColor = GeoLightGrey,
                            uncheckedThumbColor = GeoTextSecondary,
                            uncheckedTrackColor = GeoDarkGrey,
                            uncheckedBorderColor = GeoBorder
                        )
                    )
                }
            }
        }

        // SECTION 2: LANGUAGE
        SettingsSection(
            title = stringResource(R.string.settings_section_language),
            icon = Icons.Default.Language
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LanguageOptionItem(
                    label = stringResource(R.string.settings_lang_en),
                    code = "en",
                    isSelected = currentLang == "en",
                    onSelect = {
                        viewModel.appSettings.setLanguage("en")
                        onLanguageChange("en")
                    }
                )

                HorizontalDivider(color = GeoBorderSubtle, thickness = 1.dp)

                LanguageOptionItem(
                    label = stringResource(R.string.settings_lang_id),
                    code = "id",
                    isSelected = currentLang == "id" || currentLang == "in",
                    onSelect = {
                        viewModel.appSettings.setLanguage("id")
                        onLanguageChange("id")
                    }
                )
            }
        }

        // SECTION 4: ABOUT
        SettingsSection(
            title = stringResource(R.string.settings_section_about),
            icon = Icons.Default.Info
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AboutInfoRow(
                    label = stringResource(R.string.settings_app_name),
                    value = stringResource(R.string.settings_version_value)
                )

                HorizontalDivider(color = GeoBorderSubtle, thickness = 1.dp)

                AboutInfoRow(
                    label = stringResource(R.string.settings_ytdlp_title),
                    value = "yt-dlp ${AppSettings.YTDLP_VERSION}"
                )

                AboutInfoRow(
                    label = stringResource(R.string.settings_ffmpeg_title),
                    value = "FFmpeg ${AppSettings.FFMPEG_VERSION}"
                )

                // Check updates button
                GeoOutlinedButton(
                    onClick = {
                        val message = viewModel.ytDlpService.checkYtDlpUpdates()
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.settings_ytdlp_btn_check),
                            fontSize = 12.sp
                        )
                    }
                }

                HorizontalDivider(color = GeoBorderSubtle, thickness = 1.dp)

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.settings_licenses_title),
                        color = GeoTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.settings_licenses_desc),
                        color = GeoTextMuted,
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GeoTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = GeoLightGrey,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        GeoCard(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun LanguageOptionItem(
    label: String,
    code: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (isSelected) GeoWhite else GeoTextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = GeoLightGrey,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AboutInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = GeoTextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = GeoTextPrimary,
            fontSize = 12.sp,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
