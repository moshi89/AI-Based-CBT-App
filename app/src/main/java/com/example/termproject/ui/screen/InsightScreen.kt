package com.example.termproject.ui.screen

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.termproject.model.SavedJournal
import com.example.termproject.ui.component.*
import com.example.termproject.viewmodel.CbtViewModel
import com.example.termproject.viewmodel.DistortionCount
import androidx.compose.ui.res.stringResource
import com.example.termproject.R
import java.text.SimpleDateFormat
import java.util.*

// ══════════════════════════════════════════════════════════════
// InsightScreen — 실제 SavedJournal 데이터 기반 통계 대시보드
// ══════════════════════════════════════════════════════════════

@Composable
fun InsightScreen(
    viewModel: CbtViewModel,
    totalJournalsCount: Int,
    savedJournals: List<SavedJournal>,
    todayMoodRating: Int,
    topDistortions: List<DistortionCount>,
    currentLanguage: String,
    onTodayMoodRatingChange: (Int) -> Unit,
    onNavigateToCbt: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.fetchAppUsageStats(context) }

    // ── 실제 데이터 계산 ──
    val weeklyTrend    = remember(savedJournals) { calculateWeeklyTrend(savedJournals) }
    val pathTypeCounts = remember(savedJournals) { calculatePathTypeCounts(savedJournals) }
    val streak         = remember(savedJournals) { calculateStreak(savedJournals) }
    val topDists       = remember(savedJournals) {
        val all = savedJournals.flatMap { it.cognitiveDistortions }
        val total = all.size.coerceAtLeast(1)
        all.groupingBy { (it.tag ?: it.englishTag ?: "Unknown").ifBlank { "Unknown" } }
            .eachCount()
            .map { (tag, cnt) -> DistortionCount(tag, cnt, cnt.toFloat() / total) }
            .sortedByDescending { it.count }
            .take(3)
    }

    val isEmpty = savedJournals.isEmpty()

    Log.d("InsightScreen", "journals=${savedJournals.size}, streak=$streak, trend=$weeklyTrend")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── 헤더 ──
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.insight_weekly_insights),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkCharcoal
                )
                Text(
                    text = stringResource(id = R.string.insight_based_on_journals, savedJournals.size),
                    fontSize = 13.sp,
                    color = MediumGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // ── 빈 상태 ──
        if (isEmpty) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE4E2E2))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SagePrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.insight_no_journals),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.insight_complete_first),
                            fontSize = 13.sp,
                            color = MediumGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        SanctuaryButton(
                            text = stringResource(id = R.string.insight_start_session),
                            onClick = onNavigateToCbt
                        )
                    }
                }
            }
        } else {

            // ── 요약 카드 3개 ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Star,
                        label = stringResource(id = R.string.insight_total_records),
                        value = "$totalJournalsCount",
                        iconColor = Color(0xFFFFB300)
                    )
                    SummaryChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Star,
                        label = stringResource(id = R.string.profile_streak),
                        value = stringResource(id = R.string.insight_streak_format, streak),
                        iconColor = Color(0xFFE53935)
                    )
                    SummaryChip(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CheckCircle,
                        label = stringResource(id = R.string.insight_cognitive),
                        value = "${pathTypeCounts["cognitive"] ?: 0}",
                        iconColor = SagePrimary
                    )
                }
            }

            // ── 주간 활동 차트 (실제 날짜별 저널 수) ──
            item {
                WeeklyActivityCard(
                    weeklyTrend = weeklyTrend,
                    currentLanguage = currentLanguage
                )
            }

            // ── 인지 왜곡 Top3 바 차트 ──
            item {
                DistortionChartCard(
                    topDistortions = topDists,
                    currentLanguage = currentLanguage
                )
            }

            // ── 경로 타입 분포 ──
            item {
                PathTypeCard(
                    pathTypeCounts = pathTypeCounts,
                    total = savedJournals.size,
                    currentLanguage = currentLanguage
                )
            }

        }
    }
}

// ══════════════════════════════════════════════════════════════
// 서브 컴포넌트
// ══════════════════════════════════════════════════════════════

@Composable
private fun SummaryChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE4E2E2))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DarkCharcoal)
            Text(text = label, fontSize = 10.sp, color = MediumGray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun WeeklyActivityCard(
    weeklyTrend: List<Int>,
    currentLanguage: String
) {
    val maxVal = weeklyTrend.maxOrNull()?.coerceAtLeast(1) ?: 1
    val totalThisWeek = weeklyTrend.sum()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE4E2E2), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.insight_weekly_activity),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal
                    )
                    Text(
                        text = stringResource(id = R.string.insight_sessions_this_week, totalThisWeek),
                        fontSize = 12.sp,
                        color = MediumGray
                    )
                }
                Box(
                    modifier = Modifier
                        .background(SagePrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "7d",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SagePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 차트: 막대 그래프 (실제 데이터)
            Row(
                modifier = Modifier.fillMaxWidth().height(90.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyTrend.forEachIndexed { index, count ->
                    val heightFrac = if (maxVal > 0) count.toFloat() / maxVal else 0f
                    val isToday = index == 6

                    var triggered by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { triggered = true }
                    val animatedHeight by animateFloatAsState(
                        targetValue = if (triggered) heightFrac else 0f,
                        animationSpec = tween(700, delayMillis = index * 60, easing = EaseOutQuart),
                        label = "bar$index"
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (count > 0) {
                            Text(
                                text = "$count",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isToday) SagePrimary else MediumGray
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height((70 * animatedHeight.coerceAtLeast(0.05f)).dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (isToday) SagePrimary
                                    else if (count > 0) SagePrimary.copy(alpha = 0.4f)
                                    else Color(0xFFF0EFEB)
                                )
                        )
                    }
                }
            }

            // 요일 레이블
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val todayLabel = stringResource(id = R.string.insight_today)
                val dayLabels = (6 downTo 0).map { daysAgo ->
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
                    if (daysAgo == 0) {
                        todayLabel
                    } else {
                        SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
                    }
                }
                dayLabels.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 9.sp,
                        color = if (day == "Today" || day == "오늘") SagePrimary else MediumGray,
                        fontWeight = if (day == "Today" || day == "오늘") FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.width(28.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun DistortionChartCard(
    topDistortions: List<DistortionCount>,
    currentLanguage: String
) {
    val colors = listOf(Color(0xFF4A3B5C), Color(0xFF880E4F), Color(0xFF2E4A2E))
    val bgColors = listOf(Color(0xFFE8E0F0), Color(0xFFFCE4EC), Color(0xFFE8F5E9))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE4E2E2))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = SagePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.insight_top_distortions),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal
                )
            }

            if (topDistortions.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.insight_complete_more),
                    fontSize = 13.sp,
                    color = MediumGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Spacer(modifier = Modifier.height(20.dp))
                topDistortions.forEachIndexed { index, dist ->
                    val color = colors.getOrElse(index) { SagePrimary }
                    val bgColor = bgColors.getOrElse(index) { SagePrimary.copy(alpha = 0.1f) }
                    val percentage = (dist.percentage * 100).toInt()

                    var triggered by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { triggered = true }
                    val animatedWidth by animateFloatAsState(
                        targetValue = if (triggered) dist.percentage else 0f,
                        animationSpec = tween(900, delayMillis = index * 150, easing = EaseOutQuart),
                        label = "dist$index"
                    )

                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(bgColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = color
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = dist.tag,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    modifier = Modifier.widthIn(max = 180.dp)
                                )
                            }
                            Text(
                                text = "$percentage% (${dist.count}회)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = color
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F2EE))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedWidth)
                                    .background(color, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PathTypeCard(
    pathTypeCounts: Map<String, Int>,
    total: Int,
    currentLanguage: String
) {
    val cognitive  = pathTypeCounts["cognitive"]  ?: 0
    val behavioral = pathTypeCounts["behavioral"] ?: 0
    val exposure   = pathTypeCounts["exposure"]   ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE4E2E2))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PsychologyAlt,
                    contentDescription = null,
                    tint = SagePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.insight_session_types),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            listOf(
                Triple(
                    stringResource(id = R.string.insight_cognitive_restructuring),
                    cognitive,
                    Color(0xFF4A3B5C)
                ),
                Triple(
                    stringResource(id = R.string.insight_behavioral_activation),
                    behavioral,
                    Color(0xFF1565C0)
                ),
                Triple(
                    stringResource(id = R.string.insight_exposure_therapy),
                    exposure,
                    Color(0xFFE65100)
                )
            ).forEach { (label, count, color) ->
                val frac = if (total > 0) count.toFloat() / total else 0f
                var triggered by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { triggered = true }
                val animW by animateFloatAsState(
                    targetValue = if (triggered) frac else 0f,
                    animationSpec = tween(800, easing = EaseOutQuart),
                    label = "path$label"
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkCharcoal,
                        modifier = Modifier.width(130.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F2EE))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animW)
                                .background(color, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$count",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = color,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentJournalsCard(
    journals: List<SavedJournal>,
    currentLanguage: String
) {
    if (journals.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE4E2E2))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = SagePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.insight_recent_sessions),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            journals.forEach { journal ->
                val distTag = journal.cognitiveDistortions.firstOrNull()?.tag ?: "-"
                val pathIcon = when (journal.pathType) {
                    "behavioral" -> "🏃"
                    "exposure"   -> "💪"
                    else         -> "🧠"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SagePrimary.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = pathIcon, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = journal.originalThought.take(40) + if (journal.originalThought.length > 40) "…" else "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = DarkCharcoal
                        )
                        Text(
                            text = "#$distTag  •  ${journal.date.take(16)}",
                            fontSize = 11.sp,
                            color = MediumGray
                        )
                    }
                }
                if (journal != journals.last()) {
                    Divider(color = Color(0xFFF3F2EE), thickness = 1.dp)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// 데이터 계산 헬퍼 함수들
// ══════════════════════════════════════════════════════════════

private fun calculateWeeklyTrend(journals: List<SavedJournal>): List<Int> {
    val formatter = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREA)
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    return (6 downTo 0).map { daysAgo ->
        val targetDay = Calendar.getInstance().apply {
            timeInMillis = today.timeInMillis
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }
        journals.count { journal ->
            try {
                val date = formatter.parse(journal.date) ?: return@count false
                val jDay = Calendar.getInstance().apply {
                    time = date
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                jDay.timeInMillis == targetDay.timeInMillis
            } catch (e: Exception) { false }
        }
    }
}

private fun calculatePathTypeCounts(journals: List<SavedJournal>): Map<String, Int> {
    return journals.groupingBy { (it.pathType ?: "cognitive").ifBlank { "cognitive" } }.eachCount()
}

private fun calculateStreak(journals: List<SavedJournal>): Int {
    if (journals.isEmpty()) return 0
    val formatter = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREA)
    val uniqueDays = journals.mapNotNull {
        try {
            val date = formatter.parse(it.date) ?: return@mapNotNull null
            Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } catch (e: Exception) { null }
    }.toSortedSet().toList().reversed()

    var streak = 0
    val msPerDay = 86_400_000L
    val todayMs = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    uniqueDays.forEachIndexed { idx, dayMs ->
        val expected = todayMs - idx * msPerDay
        if (dayMs == expected) streak++ else return streak
    }
    return streak
}

// 레거시 호환 (사용 안 함 — 위 함수들로 대체)
private fun calculateMainDistortion(journals: List<SavedJournal>): Pair<String, Int> {
    val counts = journals.flatMap { it.cognitiveDistortions }
        .map { it.tag.ifBlank { it.englishTag } }
        .filter { it.isNotBlank() }
        .groupingBy { it }.eachCount()
    val top = counts.maxByOrNull { it.value }
    return Pair(top?.key ?: "No data yet", top?.value ?: 0)
}

@Composable
fun DistortionBar(label: String, percentage: Int, color: Color, bgColor: Color) {
    val animatedWidth by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(1000, easing = EaseOutQuart),
        label = "DistortionAnimation"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Text("$percentage%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color(0xFFF3F2EE))) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animatedWidth).background(color, CircleShape))
        }
    }
}
