package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AppSettings(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ytmedia_prefs", Context.MODE_PRIVATE)

    private val defaultDownloadDir: String = try {
        val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDir = File(publicDownloads, "YtMedia")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        appDir.absolutePath
    } catch (_: Exception) {
        val fallback = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "YtMedia")
        fallback.mkdirs()
        fallback.absolutePath
    }

    private val _downloadPath = MutableStateFlow(
        prefs.getString(KEY_DOWNLOAD_DIR, defaultDownloadDir) ?: defaultDownloadDir
    )
    val downloadPath: StateFlow<String> = _downloadPath.asStateFlow()

    private val _autoOpen = MutableStateFlow(
        prefs.getBoolean(KEY_AUTO_OPEN, true)
    )
    val autoOpen: StateFlow<Boolean> = _autoOpen.asStateFlow()

    private val _language = MutableStateFlow(
        prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    )
    val language: StateFlow<String> = _language.asStateFlow()

    fun setDownloadPath(path: String) {
        prefs.edit().putString(KEY_DOWNLOAD_DIR, path).apply()
        _downloadPath.value = path
    }

    fun resetDownloadPath() {
        setDownloadPath(defaultDownloadDir)
    }

    fun setAutoOpen(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_OPEN, enabled).apply()
        _autoOpen.value = enabled
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    companion object {
        private const val KEY_DOWNLOAD_DIR = "key_download_dir"
        private const val KEY_AUTO_OPEN = "key_auto_open"
        private const val KEY_LANGUAGE = "key_language"
        const val YTDLP_VERSION = "2025.02.19"
        const val FFMPEG_VERSION = "7.1-mobile"
    }
}
