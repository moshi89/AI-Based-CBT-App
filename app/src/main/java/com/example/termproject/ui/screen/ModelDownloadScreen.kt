package com.example.termproject.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 앱 첫 실행 시 ML 모델을 Firebase Storage에서 다운로드하는 화면.
 *
 * 상태:
 *  - 대기 중 (onStart 아직 호출 전)
 *  - 다운로드 중 (progress 0~1)
 *  - 완료 (progress == 1f)
 *  - 오류 (error != null)
 */
@Composable
fun ModelDownloadScreen(
    progress: Float,
    statusText: String,
    error: String?,
    onStart: () -> Unit,
    onRetry: () -> Unit
) {
    // 자동 시작: 화면이 표시되자마자 다운로드 시작
    LaunchedEffect(Unit) {
        if (error == null && progress == 0f) {
            onStart()
        }
    }

    // 아이콘 호흡 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8F0E8), Color(0xFFF9F7F3))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
        ) {
            // ── 아이콘 ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(if (error == null && progress < 1f) iconScale else 1f)
                    .clip(CircleShape)
                    .background(
                        if (error != null) Color(0xFFFFEBEB) else Color(0xFFD3E2D3)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (error != null) Icons.Default.ErrorOutline
                                  else Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = if (error != null) Color(0xFFE57373) else Color(0xFF6B8E7B),
                    modifier = Modifier.size(44.dp)
                )
            }

            // ── 타이틀 ──────────────────────────────────────────────────
            Text(
                text = if (error != null) "다운로드 실패" else "AI 모델 준비 중",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF32463B),
                textAlign = TextAlign.Center
            )

            // ── 설명 ────────────────────────────────────────────────────
            Text(
                text = if (error != null) {
                    "네트워크 연결을 확인하고 다시 시도해주세요.\n\n오류: $error"
                } else {
                    "감정 분석을 위한 다국어 AI 모델을\n다운로드하고 있습니다.\n처음 한 번만 진행됩니다."
                },
                fontSize = 14.sp,
                color = Color(0xFF6C7A70),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            // ── 진행률 바 ────────────────────────────────────────────────
            if (error == null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 진행률 텍스트
                    Text(
                        text = statusText,
                        fontSize = 13.sp,
                        color = Color(0xFF6B8E7B),
                        fontWeight = FontWeight.Medium
                    )

                    // 진행률 바
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF6B8E7B),
                        trackColor = Color(0xFFD3E2D3),
                        strokeCap = StrokeCap.Round
                    )

                    // 퍼센트
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = Color(0xFFA0B0A6)
                    )
                }
            }

            // ── 재시도 버튼 (오류 시에만) ─────────────────────────────────
            if (error != null) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6B8E7B)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "다시 시도",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // ── 용량 안내 ────────────────────────────────────────────────
            if (error == null && progress < 1f) {
                Text(
                    text = "⚠ 약 570MB 다운로드 필요 · Wi-Fi 권장",
                    fontSize = 11.sp,
                    color = Color(0xFFA0B0A6),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
