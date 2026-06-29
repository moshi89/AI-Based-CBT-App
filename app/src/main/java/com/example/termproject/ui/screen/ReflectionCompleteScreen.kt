package com.example.termproject.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.termproject.model.BehavioralActivity
import com.example.termproject.ui.component.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.example.termproject.R

// ══════════════════════════════════════════════════════════════
// STEP 9 — [Path B] 행동 완료 피드백 및 감정 기록
// ══════════════════════════════════════════════════════════════

@Composable
fun ReflectionCompleteScreen(
    activity: BehavioralActivity,
    durationSecs: Int,
    walkDistanceMeters: Int,
    selectedMood: String?,
    reflectionText: String,
    currentLanguage: String,
    onMoodSelect: (String) -> Unit,
    onReflectionChange: (String) -> Unit,
    onSaveJournalClick: () -> Unit
) {
    val context = LocalContext.current
    val displayMin = durationSecs / 60
    val displaySec = durationSecs % 60
    val formattedTime = String.format("%02d:%02d", displayMin, displaySec)
    val distanceStr = String.format("%.2fkm", walkDistanceMeters / 1000.0)

    // 로컬라이징된 필드 바인딩 (Context 기반 동적 획득)
    val resNameId = context.resources.getIdentifier("act_${activity.id}_name", "string", context.packageName)
    val displayName = if (resNameId != 0) context.getString(resNameId) else activity.name

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SanctuaryStepIndicator(currentStep = 5) }

        item {
            ThemeSectionHeader(
                stepLabel = stringResource(id = R.string.rc_step_label),
                title = stringResource(id = R.string.rc_title),
                subtitle = stringResource(id = R.string.rc_subtitle_a)
            )
        }

        // Summary of walking states
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFF004D40).copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(id = R.string.rc_mission),
                            fontSize = 10.sp,
                            color = MediumGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(displayName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(id = R.string.rc_walk_time),
                            fontSize = 10.sp,
                            color = MediumGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(formattedTime, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(id = R.string.rc_distance),
                            fontSize = 10.sp,
                            color = MediumGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(distanceStr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                    }
                }
            }
        }

        // Mood evaluation buttons
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.rc_mood_index),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val moods = listOf(
                    stringResource(id = R.string.rc_mood_refreshed) to "😊",
                    stringResource(id = R.string.rc_mood_calm) to "🕊️",
                    stringResource(id = R.string.rc_mood_relieved) to "🍃",
                    stringResource(id = R.string.rc_mood_neutral) to "😐"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moods.forEach { (moodName, emoji) ->
                        val isSelected = selectedMood == moodName
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) Color(0xFFE0F2F1) else Color.White,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF004D40) else Color.LightGray.copy(alpha = 0.25f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onMoodSelect(moodName) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(emoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    moodName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF004D40) else DarkCharcoal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reflection write back
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.rc_walk_note_title),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).padding(12.dp)) {
                        BasicTextField(
                            value = reflectionText,
                            onValueChange = onReflectionChange,
                            textStyle = TextStyle(color = DarkCharcoal, fontSize = 13.sp, lineHeight = 18.sp),
                            modifier = Modifier.fillMaxSize(),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (reflectionText.isEmpty()) {
                                        Text(
                                            text = stringResource(id = R.string.rc_walk_note_placeholder),
                                            color = Color.LightGray,
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            SanctuaryButton(
                text = stringResource(id = R.string.rc_save_complete_btn),
                enabled = selectedMood != null && reflectionText.trim().isNotEmpty(),
                onClick = onSaveJournalClick,
                backgroundColor = Color(0xFF004D40),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = stringResource(id = R.string.rc_save_complete_btn),
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}
