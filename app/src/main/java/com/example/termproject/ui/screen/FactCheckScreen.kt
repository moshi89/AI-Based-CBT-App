package com.example.termproject.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PsychologyAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.termproject.model.CbtAnalysis
import com.example.termproject.ui.component.*
import androidx.compose.ui.res.stringResource
import com.example.termproject.R

// ══════════════════════════════════════════════════════════════
// STEP 3 — [Path A] 소크라테스 반문 질문 화면 (Fact Check)
// ══════════════════════════════════════════════════════════════

@Composable
fun FactCheckScreen(
    analysisResult: CbtAnalysis?,
    onOptionSelected: (String) -> Unit,
    onNextClick: () -> Unit,
    thoughtText: String = "",
    currentLanguage: String = "ko",
    onLoadDynamicSocraticData: suspend (String, String) -> Unit = { _, _ -> }
) {
    // 🎨 Color Palette Tokens (DistortionReviewScreen과 조화롭게 통일)
    val colorTertiary = Color(0xFF566342) // Sage brand green
    val colorBackground = Color(0xFFFBF9F8) // Light linen canvas background
    val colorSurfaceContainerLowest = Color(0xFFFFFFFF) // Crisp white cards
    val colorPrimaryContainer = Color(0xFFE8F5E9).copy(alpha = 0.2f) // 선택 시 연한 초록 톤 배경
    val colorOutline = Color.LightGray.copy(alpha = 0.25f) // 미선택 외곽선

    val questionText = analysisResult?.socraticQuestion ?: ""
    val options = remember(analysisResult) {
        analysisResult?.options?.filter { it.isNotBlank() } ?: emptyList()
    }

    var selectedOptionIndex by remember { mutableStateOf(-1) }

    // API 보호 및 로딩 안전 가드
    LaunchedEffect(analysisResult) {
        if (questionText.isBlank() || options.isEmpty()) {
            onLoadDynamicSocraticData(thoughtText, currentLanguage)
        }
    }

    LaunchedEffect(analysisResult) {
        selectedOptionIndex = -1
    }

    if (questionText.isBlank() || options.size < 2) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = colorTertiary, strokeWidth = 3.5.dp)
                Text(
                    text = stringResource(id = R.string.generating_socratic_question),
                    fontSize = 14.sp,
                    color = colorTertiary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorBackground)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 상단 통일된 헤더 타이출 노출
                item {
                    ThemeSectionHeader(
                        stepLabel = stringResource(id = R.string.sc_step_label),
                        title = stringResource(id = R.string.sc_title),
                        subtitle = stringResource(id = R.string.sc_subtitle)
                    )
                }

                // 소크라테스 반문 메인 질문 카드
                item {
                    Card(
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = colorSurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(colorTertiary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PsychologyAlt,
                                    contentDescription = "Socratic Questioning",
                                    tint = colorTertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = questionText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DarkCharcoal,
                                textAlign = TextAlign.Center,
                                lineHeight = 26.sp
                            )
                        }
                    }
                }

                // 3-대안 질문 리스트
                itemsIndexed(options) { index, option ->
                    val isSelected = selectedOptionIndex == index
                    val cardShape = RoundedCornerShape(16.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSelected) colorPrimaryContainer else colorSurfaceContainerLowest,
                                shape = cardShape
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) colorTertiary else colorOutline,
                                shape = cardShape
                            )
                            .clip(cardShape)
                            .clickable {
                                selectedOptionIndex = index
                                onOptionSelected(option)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) colorTertiary else DarkCharcoal,
                                modifier = Modifier.weight(1f),
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) colorTertiary else Color.Transparent
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) colorTertiary else Color.LightGray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SanctuaryButton(
                        text = stringResource(id = R.string.cat_next_btn),
                        enabled = selectedOptionIndex != -1,
                        onClick = onNextClick,
                        backgroundColor = colorTertiary,
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Navigate forward",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}