package com.example.termproject.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.termproject.ui.component.MediumGray
import com.example.termproject.ui.component.SagePrimary

// ══════════════════════════════════════════════════════════════
// STEP 1 — 생각 분석 로딩 화면
// ══════════════════════════════════════════════════════════════

@Composable
fun AnalysingScreen(
    title: String = "마음 오염 왜곡 필터 스캔 중..",
    description: String = "자동으로 피어난 부정적 비관 왜곡(ANT)을 식별하고 정성적으로 조율하기 위해 치료 규칙 데이터베이스 엔진을 대칭 분석하는 중입니다."
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = SagePrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(52.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SagePrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = MediumGray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}
