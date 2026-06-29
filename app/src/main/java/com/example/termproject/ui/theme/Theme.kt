package com.example.termproject.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * CBT Sanctuary 앱 전체의 Material3 테마 래퍼.
 */
@Composable
fun CbtSanctuaryTheme(content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = SagePrimary,
        secondary = SoftSageLight,
        background = LinenBackground,
        surface = Color.White,
        onPrimary = Color.White,
        onBackground = DarkCharcoal,
        onSurface = DarkCharcoal
    )
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
