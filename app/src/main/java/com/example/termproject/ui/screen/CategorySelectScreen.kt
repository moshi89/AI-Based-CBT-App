package com.example.termproject.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.termproject.ui.component.*
import androidx.compose.ui.res.stringResource
import com.example.termproject.R

// ══════════════════════════════════════════════════════════════
// STEP 2 — 분기 카테고리 기로 선택 구간 (실천할 작업 선택)
// ══════════════════════════════════════════════════════════════

@Composable
fun CategorySelectScreen(
    currentLanguage: String,
    onSelectAction: (String) -> Unit
) {
    // HTML과 동일하게 행동적 작업을 기본 선택값으로 세팅
    var selectedOption by remember { mutableStateOf("behavioral") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F8)) // HTML bg-surface
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ThemeSectionHeader(
                    stepLabel = stringResource(id = R.string.cat_step_label),
                    title = stringResource(id = R.string.cat_title),
                    subtitle = stringResource(id = R.string.cat_subtitle)
                )
            }

            // 1) 인지적 작업 카드
            item {
                val isCognitiveSelected = selectedOption == "cognitive"
                val tintColor = Color(0xFF4A3B5C)
                val bgBadgeColor = Color(0xFFE8E0F0)
                val cardShape = RoundedCornerShape(16.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isCognitiveSelected) bgBadgeColor.copy(alpha = 0.2f) else Color.White,
                            shape = cardShape
                        )
                        .border(
                            width = if (isCognitiveSelected) 2.dp else 1.dp,
                            color = if (isCognitiveSelected) tintColor else Color.LightGray.copy(alpha = 0.25f),
                            shape = cardShape
                        )
                        .clip(cardShape)
                        .clickable { selectedOption = "cognitive" }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(bgBadgeColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = stringResource(id = R.string.cat_cognitive_title),
                                tint = tintColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.cat_cognitive_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = tintColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(id = R.string.cat_cognitive_desc),
                                fontSize = 13.sp,
                                color = DarkCharcoal,
                                lineHeight = 18.sp
                            )
                        }
                        // Selection Indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCognitiveSelected) tintColor else Color.Transparent
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isCognitiveSelected) tintColor else Color.LightGray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCognitiveSelected) {
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

            // 2) 행동적 작업 카드 (Recommended)
            item {
                val isBehavioralSelected = selectedOption == "behavioral"
                val tintColor = Color(0xFF2E4A2E)
                val bgBadgeColor = Color(0xFFE8F5E9)
                val cardShape = RoundedCornerShape(16.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isBehavioralSelected) bgBadgeColor.copy(alpha = 0.2f) else Color.White,
                            shape = cardShape
                        )
                        .border(
                            width = if (isBehavioralSelected) 2.dp else 1.dp,
                            color = if (isBehavioralSelected) tintColor else Color.LightGray.copy(alpha = 0.25f),
                            shape = cardShape
                        )
                        .clip(cardShape)
                        .clickable { selectedOption = "behavioral" }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(bgBadgeColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = stringResource(id = R.string.cat_behavioral_title),
                                tint = tintColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                  Text(
                                      text = stringResource(id = R.string.cat_behavioral_title),
                                      fontSize = 16.sp,
                                      fontWeight = FontWeight.Bold,
                                      color = tintColor
                                  )
                                  Spacer(modifier = Modifier.width(8.dp))
                                  Box(
                                      modifier = Modifier
                                          .background(Color(0xFF566342), RoundedCornerShape(99.dp))
                                          .padding(horizontal = 8.dp, vertical = 2.dp)
                                  ) {
                                      Text(
                                          text = stringResource(id = R.string.cat_behavioral_recommended),
                                          fontSize = 9.sp,
                                          fontWeight = FontWeight.Bold,
                                          color = Color.White
                                      )
                                  }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(id = R.string.cat_behavioral_desc),
                                fontSize = 13.sp,
                                color = DarkCharcoal,
                                lineHeight = 18.sp
                            )
                        }
                        // Selection Indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isBehavioralSelected) tintColor else Color.Transparent
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isBehavioralSelected) tintColor else Color.LightGray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isBehavioralSelected) {
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

            // 3) 나의 빛나는 모습 찾기 카드
            item {
                val isExposureSelected = selectedOption == "exposure"
                val tintColor = Color(0xFF880E4F)
                val bgBadgeColor = Color(0xFFFCE4EC)
                val cardShape = RoundedCornerShape(16.dp)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isExposureSelected) bgBadgeColor.copy(alpha = 0.2f) else Color.White,
                            shape = cardShape
                        )
                        .border(
                            width = if (isExposureSelected) 2.dp else 1.dp,
                            color = if (isExposureSelected) tintColor else Color.LightGray.copy(alpha = 0.25f),
                            shape = cardShape
                        )
                        .clip(cardShape)
                        .clickable { selectedOption = "exposure" }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(bgBadgeColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = stringResource(id = R.string.cat_exposure_title),
                                tint = tintColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.cat_exposure_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = tintColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(id = R.string.cat_exposure_desc),
                                fontSize = 13.sp,
                                color = DarkCharcoal,
                                lineHeight = 18.sp
                            )
                        }
                        // Selection Indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isExposureSelected) tintColor else Color.Transparent
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isExposureSelected) tintColor else Color.LightGray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isExposureSelected) {
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
                    onClick = { onSelectAction(selectedOption) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Step",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}
