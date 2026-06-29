package com.example.termproject.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==========================================
// 🎨 DESIGN SYSTEM COLOR TOKENS
// ==========================================
val LinenBackground = Color(0xFFFAF9F6)
val SagePrimary = Color(0xFF566342)
val SoftSageLight = Color(0xFFDDE7C2)
val DarkCharcoal = Color(0xFF1B1C1C)
val MediumGray = Color(0xFF5A5F5A)
val CardBorderColor = Color(0x33B5B8B0)

// Category Sub-tag Distortions Accent Colors
val ColorMindReadingBg = Color(0xFFE8DDF2)
val ColorMindReadingText = Color(0xFF533E6E)
val ColorCatastrophizingBg = Color(0xFFFCE4EC)
val ColorCatastrophizingText = Color(0xFF880E4F)
val ColorGeneralBg = Color(0xFFE0F2F1)
val ColorGeneralText = Color(0xFF004D40)

// ==========================================
// 🌿 COMPONENT MODULES
// ==========================================

/**
 * Custom modern Card for beautiful elevation and spacing.
 */
@Composable
fun SanctuaryCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = CardBorderColor,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    elevation: androidx.compose.ui.unit.Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

/**
 * Elegant badge for cognitive distortions tag.
 */
@Composable
fun DistortionBadge(
    tag: String,
    englishTag: String,
    currentLanguage: String = "ko",
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when {
        englishTag.contains("mind_reading") || englishTag.contains("mind") -> ColorMindReadingBg to ColorMindReadingText
        englishTag.contains("catas") || englishTag.contains("catastrophizing") -> ColorCatastrophizingBg to ColorCatastrophizingText
        else -> ColorGeneralBg to ColorGeneralText
    }

    val displayTag = if (currentLanguage == "en") {
        when {
            englishTag.contains("mind_reading") || englishTag.contains("mind") -> "Mind Reading"
            englishTag.contains("catas") || englishTag.contains("catastrophizing") -> "Catastrophizing"
            englishTag.contains("overgeneralization") -> "Overgeneralization"
            englishTag.contains("mental_filter") -> "Mental Filter"
            englishTag.contains("personalization") -> "Personalization"
            englishTag.contains("emotional_reasoning") -> "Emotional Reasoning"
            englishTag.contains("all_or_nothing") -> "All-or-Nothing Thinking"
            else -> tag
        }
    } else {
        tag
    }

    Box(
        modifier = modifier
            .background(bgColor, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "#$displayTag",
            fontSize = 11.sp,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )
    }
}

/**
 * Compact step tracker with tiny progress lines.
 */
@Composable
fun SanctuaryStepIndicator(
    currentStep: Int,
    totalSteps: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            val isActive = i == currentStep
            val isPassed = i < currentStep
            val barColor = when {
                isActive -> SagePrimary
                isPassed -> SagePrimary.copy(alpha = 0.5f)
                else -> Color.LightGray.copy(alpha = 0.3f)
            }
            val barWidth = if (isActive) 28.dp else 8.dp

            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(barWidth)
                    .height(8.dp)
                    .background(barColor, CircleShape)
            )
        }
    }
}

/**
 * Polished, curved Socratic button for screen-to-screen transitions.
 */
@Composable
fun SanctuaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = SagePrimary,
    textColor: Color = Color.White,
    icon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = Color.LightGray.copy(alpha = 0.4f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) textColor else Color.Gray
            )
            if (icon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                icon()
            }
        }
    }
}

/**
 * Beautiful Section title and descriptive subtitle context helper.
 */
@Composable
fun ThemeSectionHeader(
    stepLabel: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(SagePrimary.copy(alpha = 0.08f), CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = stepLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SagePrimary,
                fontFamily = FontFamily.SansSerif
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkCharcoal,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.SansSerif
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MediumGray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
