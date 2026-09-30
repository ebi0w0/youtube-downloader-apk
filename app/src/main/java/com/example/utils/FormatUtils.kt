package com.example.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.domain.model.DownloadedMedia
import com.example.domain.model.MediaType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[index])
    }

    fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec <= 0) return "-- KB/s"
        return "${formatBytes(bytesPerSec)}/s"
    }

    fun formatDuration(totalSeconds: Long): String {
        if (totalSeconds <= 0) return "00:00"
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatDurationMs(millis: Long): String {
        return formatDuration(millis / 1000)
    }

    fun formatEta(seconds: Long): String {
        if (seconds <= 0) return "--:--"
        return formatDuration(seconds)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun openMediaFile(context: Context, media: DownloadedMedia) {
        val file = File(media.filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist: ${media.fileName}", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val authority = "${context.packageName}.provider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
            val mimeType = if (media.mediaType == MediaType.MP4) "video/mp4" else "audio/mpeg"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                clipData = ClipData.newRawUri("media", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Open media").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val resolveInfoList = context.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resolveInfoList) {
                val pkgName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(pkgName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareMediaFile(context: Context, media: DownloadedMedia) {
        val file = File(media.filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist: ${media.fileName}", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val authority = "${context.packageName}.provider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
            val mimeType = if (media.mediaType == MediaType.MP4) "video/mp4" else "audio/mpeg"

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                clipData = ClipData.newRawUri("media", uri)
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, media.title)
                putExtra(Intent.EXTRA_TEXT, "${media.title} - downloaded via YtMedia")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Share media file").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val resolveInfoList = context.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resolveInfoList) {
                val pkgName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(pkgName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
