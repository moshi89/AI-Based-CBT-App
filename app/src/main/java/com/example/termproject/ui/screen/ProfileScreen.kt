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
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import java.io.File
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.termproject.R
import coil.compose.AsyncImage
import com.example.termproject.model.SavedJournal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * CBT Sanctuary — 마인드 디바이스 프로필 탭 (Profile Screen)
 */
@Composable
fun ProfileScreen(
    photoUri: String,
    nickname: String,
    jobTitle: String,
    email: String,
    profilePhotoPath: String = photoUri,
    savedJournals: List<SavedJournal>,
    currentLanguage: String = "ko",
    onSignOut: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onJournalClick: (SavedJournal) -> Unit
) {
    var showAllActivity by remember { mutableStateOf(false) }

    // 💡 LIVE CALCULATION: Compute active consecutive streak and total entries dynamically
    val totalSessionsCount = savedJournals.size
    val activeStreakDays = remember(savedJournals) { calculateConsecutiveStreak(savedJournals) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Profile Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFDDE7C2), Color(0xFFDAE8BE))
                            ),
                            shape = CircleShape
                        )
                        .padding(4.dp)
                        .clickable { onNavigateToEditProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFFE2EBE5)),
                        contentAlignment = Alignment.Center
                    ) {

                        val file = if (photoUri.isNotBlank()) File(photoUri) else null

                        // 2. Check if the file actually exists on the device storage
                        if (file != null && file.exists()) {
                            AsyncImage(
                                model = file, // Coil automatically handles File objects
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // 3. Fallback to icon if no file exists or path is empty
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar Placeholder",
                                tint = Color(0xFF6B8E7B),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp) // Add padding so the icon isn't cramped
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = nickname.ifBlank { "Elena Vance" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1C1C)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (jobTitle.isNotBlank()) "@${jobTitle.replace(" ", "_").lowercase()}" else "@serene_soul",
                    fontSize = 14.sp,
                    color = Color(0xFF757874),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 2. Quick Stats Row Bento Card Duo (Streak Day, Total Sessions - Activated!)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Streak Day Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(112.dp)
                        .border(1.dp, Color(0xFFE4E2E2), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = activeStreakDays.toString(), // 💡 Activated live count
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF596245)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(id = R.string.profile_streak),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757874)
                        )
                    }
                }

                // Sessions Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(112.dp)
                        .border(1.dp, Color(0xFFE4E2E2), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = totalSessionsCount.toString(), // 💡 Activated live dynamic size counter
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF566342)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(id = R.string.profile_sessions),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757874)
                        )
                    }
                }
            }
        }

        // 3. Recent Activity Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_recent_activity) + " ($totalSessionsCount)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1C1C)
                    )
                    if (totalSessionsCount > 3) {
                        Text(
                            text = if (showAllActivity) stringResource(id = R.string.profile_show_less) else stringResource(id = R.string.profile_view_all),
                            fontSize = 12.sp,
                            color = Color(0xFF596245),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showAllActivity = !showAllActivity }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val activitiesToShow = if (showAllActivity) savedJournals else savedJournals.take(3)

                if (activitiesToShow.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE4E2E2).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.profile_no_journals),
                                fontSize = 13.sp,
                                color = Color(0xFF757874)
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        activitiesToShow.forEach { journal ->
                            val icon = when (journal.pathType) {
                                "behavioral" -> Icons.Default.DirectionsWalk
                                "exposure" -> Icons.Default.Face
                                else -> Icons.Default.EnergySavingsLeaf
                            }
                            val iconBg = when (journal.pathType) {
                                "behavioral", "exposure" -> Color(0xFFDDE7C2)
                                else -> Color(0xFFF0FFD3)
                            }
                            val iconColor = when (journal.pathType) {
                                "behavioral" -> Color(0xFF596245)
                                "exposure" -> Color(0xFF424A2F)
                                else -> Color(0xFF566342)
                            }
                            val title = when (journal.pathType) {
                                "behavioral" -> journal.behavioralInfo?.activityName ?: "Mindful Walking"
                                "exposure" -> stringResource(id = R.string.profile_exposure_title)
                                else -> stringResource(id = R.string.profile_cognitive_title)
                            }
                            val subtitle = when (journal.pathType) {
                                "behavioral" -> stringResource(id = R.string.profile_behavioral_sub) + (journal.behavioralInfo?.duration ?: "15 min")
                                "exposure" -> stringResource(id = R.string.profile_exposure_sub) + (journal.exposureInfo?.compliments?.size ?: 0) + stringResource(id = R.string.profile_behavioral_compliments_suffix)
                                else -> stringResource(id = R.string.profile_cognitive_sub) + journal.originalThought
                            }

                            ProfileActivityItem(
                                icon = icon,
                                iconBg = iconBg,
                                iconColor = iconColor,
                                title = title,
                                subtitle = subtitle,
                                time = journal.date.split(" ").firstOrNull() ?: journal.date,
                                onClick = { onJournalClick(journal) }
                            )
                        }
                    }
                }
            }
        }

        // 4. On-device Encryption & Client Settings Sub-card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE4E2E2), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(imageVector = Icons.Default.Mail, contentDescription = "Mail", tint = Color(0xFF757874), modifier = Modifier.size(18.dp))
                            Text(stringResource(id = R.string.profile_email), fontSize = 12.sp, color = Color(0xFF757874))
                        }
                        Text(
                            text = email.ifBlank { "-" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1C1C)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = "Security", tint = Color(0xFF757874), modifier = Modifier.size(18.dp))
                            Text(stringResource(id = R.string.profile_encryption), fontSize = 12.sp, color = Color(0xFF757874))
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE2F0D9), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.profile_encryption_active),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF385723)
                            )
                        }
                    }
                }
            }
        }

        // Profile Configuration Trigger Button
        item {
            OutlinedButton(
                onClick = onNavigateToEditProfile,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE4E2E2)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF566342)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Configure Profile", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(id = R.string.profile_edit_btn), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 5. 로그아웃 버튼
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSignOut() }
                    .border(1.dp, Color(0xFFFFE3E3), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Sign Out",
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.profile_logout_btn),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                }
            }
        }
    }
}

/**
 * Recenct Activity Item matching the CSS styled list items perfectly.
 */
@Composable
fun ProfileActivityItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconColor: Color,
    title: String,
    subtitle: String,
    time: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, Color(0xFFE4E2E2).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(iconBg, CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1C1C)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF757874)
                    )
                }
            }
            Text(
                text = time,
                fontSize = 11.sp,
                color = Color(0xFFC5C7C3),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 💡 UTILITY ALGORITHM: Processes journal completion timestamp records
 * to dynamically extract active day streak length metrics.
 */
private fun calculateConsecutiveStreak(journals: List<SavedJournal>): Int {
    if (journals.isEmpty()) return 0

    val dateFormats = listOf(
        SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREA),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    )

    fun parseJournalDay(dateText: String): Long? {
        val parsedDate = dateFormats.firstNotNullOfOrNull { format ->
            try {
                format.parse(dateText)
            } catch (e: Exception) {
                null
            }
        } ?: return null

        return Calendar.getInstance().apply {
            time = parsedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val journalDays = journals
        .mapNotNull { parseJournalDay(it.date) }
        .distinct()
        .sortedDescending()

    if (journalDays.isEmpty()) return 0

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val yesterday = Calendar.getInstance().apply {
        timeInMillis = today.timeInMillis
        add(Calendar.DAY_OF_YEAR, -1)
    }

    val latestDay = journalDays.first()

    if (latestDay != today.timeInMillis && latestDay != yesterday.timeInMillis) {
        return 0
    }

    var streak = 0
    val expectedDay = Calendar.getInstance().apply {
        timeInMillis = latestDay
    }

    for (day in journalDays) {
        if (day == expectedDay.timeInMillis) {
            streak++
            expectedDay.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            break
        }
    }

    return streak
}