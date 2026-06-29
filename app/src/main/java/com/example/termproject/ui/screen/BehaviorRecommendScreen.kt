package com.example.termproject.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.termproject.model.BehavioralActivity
import com.example.termproject.ui.component.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.example.termproject.R

// ══════════════════════════════════════════════════════════════
// STEP 3 — [Path B] 마음챙김 활동 추천선택 (Behavioral Activation)
// ══════════════════════════════════════════════════════════════

@Composable
fun BehaviorRecommendScreen(
    activities: List<BehavioralActivity>,
    selectedId: String?,
    currentLanguage: String,
    onActivitySelect: (BehavioralActivity) -> Unit,
    onNextClick: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val colorTertiary = Color(0xFF566342) // Sage brand green
    val colorBackground = Color(0xFFFBF9F8) // bg-surface
    val colorSurfaceContainerLowest = Color(0xFFFFFFFF) // White cards
    val colorPrimaryContainer = Color(0xFFE8F5E9).copy(alpha = 0.2f) // 선택 시 연한 초록 톤 배경
    val colorOutline = Color.LightGray.copy(alpha = 0.25f) // 미선택 외곽선

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
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        ThemeSectionHeader(
                            stepLabel = stringResource(id = R.string.br_step_label),
                            title = stringResource(id = R.string.br_title),
                            subtitle = stringResource(id = R.string.br_subtitle)
                        )
                    }
                    
                    // 우측 상단 미니멀한 새로고침 버튼 배치
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(colorTertiary.copy(alpha = 0.08f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Nearby",
                            tint = colorTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            items(activities) { act ->
                val isSelected = selectedId == act.id
                val cardShape = RoundedCornerShape(16.dp)
                
                // displayName / displayPurpose binding using Android Resource ID resolution
                val resNameId = context.resources.getIdentifier("act_${act.id}_name", "string", context.packageName)
                val displayName = if (resNameId != 0) context.getString(resNameId) else act.name

                val resPurposeId = context.resources.getIdentifier("act_${act.id}_purpose", "string", context.packageName)
                val displayPurpose = if (resPurposeId != 0) context.getString(resPurposeId) else act.purpose

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
                        .clickable { onActivitySelect(act) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (isSelected) colorTertiary.copy(alpha = 0.15f) else Color(0xFFF3F2EE),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val actIcon = when {
                                act.id.contains("forest") || act.name.contains("숲") || act.name.contains("산") || act.name.contains("공원") || act.name.contains("성") -> Icons.Default.Forest
                                act.id.contains("sunlight") || act.name.contains("햇빛") || act.name.contains("햇살") || act.name.contains("광합성") -> Icons.Default.WbSunny
                                act.id.contains("stream") || act.name.contains("하천") || act.name.contains("강") || act.name.contains("개울") || act.name.contains("바다") || act.name.contains("천") || act.name.contains("수변") -> Icons.Default.Water
                                else -> Icons.Default.DirectionsWalk
                            }
                            Icon(
                                imageVector = actIcon,
                                contentDescription = displayName,
                                tint = if (isSelected) colorTertiary else Color(0xFF757874),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = displayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) colorTertiary else DarkCharcoal
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(colorTertiary.copy(alpha = 0.1f), RoundedCornerShape(99.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = act.distance,
                                        fontSize = 9.sp,
                                        color = colorTertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = displayPurpose,
                                fontSize = 13.sp,
                                color = if (isSelected) colorTertiary.copy(alpha = 0.8f) else Color(0xFF757874),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SanctuaryButton(
                    text = stringResource(id = R.string.br_next_btn),
                    enabled = selectedId != null,
                    onClick = onNextClick,
                    backgroundColor = colorTertiary,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = stringResource(id = R.string.br_next_btn),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}
