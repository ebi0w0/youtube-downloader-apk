package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeoBlack
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoBorderSubtle
import com.example.ui.theme.GeoDarkGrey
import com.example.ui.theme.GeoLightGrey
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceElevated
import com.example.ui.theme.GeoTextPrimary
import com.example.ui.theme.GeoTextSecondary

val GeometricCornerRadius = 4.dp
val GeometricShape: Shape = RoundedCornerShape(GeometricCornerRadius)

@Composable
fun GeoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        enabled = enabled,
        shape = GeometricShape,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = GeoLightGrey,
            contentColor = GeoBlack,
            disabledContainerColor = GeoDarkGrey,
            disabledContentColor = GeoTextSecondary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        content()
    }
}

@Composable
fun GeoOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    content: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        enabled = enabled,
        shape = GeometricShape,
        contentPadding = contentPadding,
        border = BorderStroke(1.dp, if (enabled) GeoBorder else GeoBorderSubtle),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = GeoSurface,
            contentColor = GeoTextPrimary,
            disabledContainerColor = GeoSurface,
            disabledContentColor = GeoTextSecondary
        )
    ) {
        content()
    }
}

@Composable
fun GeoSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(GeometricShape)
            .background(GeoSurfaceElevated)
            .border(1.dp, GeoBorder, GeometricShape)
            .padding(2.dp)
    ) {
        items.forEachIndexed { index, title ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(GeometricShape)
                    .background(if (isSelected) GeoLightGrey else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = GeoLightGrey)
                    ) { onItemSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) GeoBlack else GeoTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun GeoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = GeoSurface,
    borderColor: Color = GeoBorder,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .border(1.dp, borderColor, GeometricShape)
            .clip(GeometricShape),
        color = backgroundColor,
        shape = GeometricShape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        content()
    }
}

@Composable
fun GeoBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = GeoSurfaceElevated,
    textColor: Color = GeoTextPrimary
) {
    Box(
        modifier = modifier
            .border(1.dp, GeoBorderSubtle, RoundedCornerShape(2.dp))
            .background(backgroundColor, RoundedCornerShape(2.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
