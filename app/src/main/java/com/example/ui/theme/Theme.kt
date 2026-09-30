package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val GeometricDarkColorScheme = darkColorScheme(
    primary = GeoLightGrey,
    onPrimary = GeoBlack,
    primaryContainer = GeoSurfaceElevated,
    onPrimaryContainer = GeoWhite,
    secondary = GeoTextSecondary,
    onSecondary = GeoBlack,
    secondaryContainer = GeoDarkGrey,
    onSecondaryContainer = GeoTextPrimary,
    background = GeoBlack,
    onBackground = GeoTextPrimary,
    surface = GeoSurface,
    onSurface = GeoTextPrimary,
    surfaceVariant = GeoSurfaceElevated,
    onSurfaceVariant = GeoTextSecondary,
    outline = GeoBorder,
    outlineVariant = GeoBorderSubtle,
    error = GeoAccentError,
    onError = GeoBlack
)

val GeometricShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GeometricDarkColorScheme,
        typography = Typography,
        shapes = GeometricShapes,
        content = content
    )
}
