package com.example

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.NavigationTab
import com.example.ui.components.MediaPlayerModal
import com.example.ui.screens.ConvertScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.GeoBlack
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoBorderSubtle
import com.example.ui.theme.GeoDarkGrey
import com.example.ui.theme.GeoLightGrey
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoTextMuted
import com.example.ui.theme.GeoTextPrimary
import com.example.ui.theme.GeoTextSecondary
import com.example.ui.theme.GeoWhite
import com.example.ui.theme.MyApplicationTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appLanguage by viewModel.appSettings.language.collectAsState()
            var currentLanguageCode by remember(appLanguage) { mutableStateOf(appLanguage) }

            val localizedContext = remember(currentLanguageCode) {
                createLocalizedContext(this, currentLanguageCode)
            }
            val configuration = remember(currentLanguageCode) {
                Configuration(resources.configuration).apply {
                    val targetLocale = if (currentLanguageCode == "id" || currentLanguageCode == "in") {
                        Locale("in", "ID")
                    } else {
                        Locale.ENGLISH
                    }
                    setLocale(targetLocale)
                }
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides configuration,
                LocalActivityResultRegistryOwner provides this
            ) {
                MyApplicationTheme {
                    MainAppScaffold(
                        viewModel = viewModel,
                        onLanguageChange = { newLang ->
                            currentLanguageCode = newLang
                        }
                    )
                }
            }
        }
    }

    private fun createLocalizedContext(baseActivity: ComponentActivity, languageCode: String): Context {
        val targetLocale = if (languageCode == "id" || languageCode == "in") {
            Locale("in", "ID")
        } else {
            Locale.ENGLISH
        }
        Locale.setDefault(targetLocale)
        val config = Configuration(baseActivity.resources.configuration)
        config.setLocale(targetLocale)
        val configContext = baseActivity.createConfigurationContext(config)
        return object : ContextWrapper(baseActivity) {
            override fun getResources(): android.content.res.Resources {
                return configContext.resources
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    viewModel: MainViewModel,
    onLanguageChange: (String) -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val playerPlaybackState by viewModel.playerPlaybackState.collectAsState()

    // Back handler: return to Convert screen if in other tabs
    if (currentTab != NavigationTab.CONVERT) {
        BackHandler {
            viewModel.selectTab(NavigationTab.CONVERT)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBlack),
        containerColor = GeoBlack,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GeoBorderSubtle)
            ) {
                NavigationBar(
                    containerColor = GeoSurface,
                    contentColor = GeoTextPrimary,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(64.dp)
                ) {
                    // Downloader (Convert) tab
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.CONVERT,
                        onClick = { viewModel.selectTab(NavigationTab.CONVERT) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavigationTab.CONVERT) Icons.Filled.Download else Icons.Outlined.Download,
                                contentDescription = stringResource(R.string.nav_convert),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.nav_convert),
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == NavigationTab.CONVERT) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GeoBlack,
                            selectedTextColor = GeoWhite,
                            indicatorColor = GeoLightGrey,
                            unselectedIconColor = GeoTextSecondary,
                            unselectedTextColor = GeoTextMuted
                        )
                    )

                    // Downloads tab
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.DOWNLOADS,
                        onClick = { viewModel.selectTab(NavigationTab.DOWNLOADS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavigationTab.DOWNLOADS) Icons.Filled.Folder else Icons.Outlined.Folder,
                                contentDescription = stringResource(R.string.nav_downloads),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.nav_downloads),
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == NavigationTab.DOWNLOADS) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GeoBlack,
                            selectedTextColor = GeoWhite,
                            indicatorColor = GeoLightGrey,
                            unselectedIconColor = GeoTextSecondary,
                            unselectedTextColor = GeoTextMuted
                        )
                    )

                    // Settings tab
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.SETTINGS,
                        onClick = { viewModel.selectTab(NavigationTab.SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavigationTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.nav_settings),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.nav_settings),
                                fontSize = 11.sp,
                                fontWeight = if (currentTab == NavigationTab.SETTINGS) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GeoBlack,
                            selectedTextColor = GeoWhite,
                            indicatorColor = GeoLightGrey,
                            unselectedIconColor = GeoTextSecondary,
                            unselectedTextColor = GeoTextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.CONVERT -> ConvertScreen(viewModel = viewModel)
                NavigationTab.DOWNLOADS -> DownloadsScreen(viewModel = viewModel)
                NavigationTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    onLanguageChange = onLanguageChange
                )
            }
        }
    }

    // Built-in Media Player Modal
    if (playerPlaybackState.media != null) {
        MediaPlayerModal(
            playerState = playerPlaybackState,
            playerManager = viewModel.playerManager,
            onDismiss = { viewModel.closePlayer() }
        )
    }
}
