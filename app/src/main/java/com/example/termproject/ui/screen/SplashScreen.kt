package com.example.termproject.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import com.example.termproject.R

/**
 * CBT Sanctuary — 스플래시 화면 (Splash Screen)
 *
 * 평화로운 연녹색/세이지 힐링 그라데이션 바탕과
 * 숨쉬듯 부드럽게 고조되는 유기적인 아이콘 애니메이션,
 * 4초 동안 마음 안정을 돕는 프로그레스 게이지바 연출.
 */
@Composable
fun SplashScreen(
    currentLanguage: String = "ko",
    onTimeout: () -> Unit
) {
    // 숨쉬는 듯한 로고 스케일 애니메이션 (Breathe Animation)
    val infiniteTransition = rememberInfiniteTransition(label = "BreatheTransition")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoScale"
    )

    // 2초 타이머
    LaunchedEffect(Unit) {
        delay(2000L)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F0E8), // 극치유 세이지 라이트
                        Color(0xFFF9F7F3)  // 내추럴 린넨 무드
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // 원을 둘러싼 부드러운 글로우 아이콘 브랜디드 박스
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(logoScale)
                    .clip(CircleShape)
                    .background(Color(0xFFD3E2D3))
                    .border(1.dp, Color(0xFFB0CDBC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "CBT Heart logo",
                    tint = Color(0xFF6B8E7B), // 세이지 포레스트 그린 코어
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "CBT Sanctuary",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF32463B), // 차콜 그린
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.splash_subtitle),
                fontSize = 14.sp,
                color = Color(0xFF6C7A70),
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

        }
    }
}
