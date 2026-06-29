package com.example.termproject.ui.screen
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ImageBitmap
import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import coil.compose.AsyncImage
import com.example.termproject.model.SavedJournal
import com.example.termproject.ui.component.*
import androidx.compose.ui.res.stringResource
import com.example.termproject.R

// ══════════════════════════════════════════════════════════════
// STEP 10 — 저널 상세 다시 읽기 화면
// ══════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalDetailScreen(
    journal: SavedJournal,
    currentLanguage: String,
    onHomeClick: () -> Unit
) {
    val context = LocalContext.current
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()

    // 1. 기존 LazyColumn을 Column으로 감싸서 버튼과 저널 내용을 배치
    Column(modifier = Modifier.fillMaxSize()) {

        // 2. 상단 액션 버튼 추가
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = {
                    val shareText = """
            CBT Journal
            
            Date: ${journal.date}
            
            Thought:
            ${journal.originalThought}
        """.trimIndent()

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }

                    context.startActivity(
                        Intent.createChooser(intent, "Share Journal")
                    )
                }
            ) {
                Icon(Icons.Default.Share, "Share", tint = SagePrimary)
            }
            IconButton(
                onClick = {
                    scope.launch {
                        try {
                            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()

                            saveBitmapToGallery(context, bitmap)

                            Toast.makeText(
                                context,
                                "Image saved successfully!",
                                Toast.LENGTH_SHORT
                            ).show()

                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Failed to save image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Download",
                    tint = SagePrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                },
            contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val dateBg = when (journal.pathType) {
                        "behavioral" -> Color(0xFFE0F2F1)
                        "exposure" -> Color(0xFFDDE7C2)
                        else -> SagePrimary.copy(alpha = 0.1f)
                    }
                    val dateColor = when (journal.pathType) {
                        "behavioral" -> Color(0xFF004D40)
                        "exposure" -> Color(0xFF424A2F)
                        else -> SagePrimary
                    }
                    Box(
                        modifier = Modifier
                            .background(dateBg, CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = journal.date,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dateColor
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(id = R.string.jd_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal
                    )
                }
            }

            // Original Unhappy Thought
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.jd_original_thought),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SagePrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${journal.originalThought}\"",
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = DarkCharcoal,
                            lineHeight = 21.sp
                        )
                    }
                }
            }

            // Identified Cognitive Distortion Tags
            item {
                SanctuaryCard {
                    Text(
                        text = stringResource(id = R.string.jd_cognitive_filter),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkCharcoal
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        journal.cognitiveDistortions.forEach { dist ->
                            DistortionBadge(
                                tag = dist.tag,
                                englishTag = dist.englishTag,
                                currentLanguage = currentLanguage
                            )
                        }
                    }
                }
            }

            // Path-specific contents
            when (journal.pathType) {
                "behavioral" -> {
                    if (journal.behavioralInfo != null) {
                        // CbtLocales 기반 활동 필드 변환
                        val activityId = when (journal.behavioralInfo.activityName) {
                            "푸른 숲길 공원 둘레길", "좋은 산림 공원 산속길" -> "forest"
                            "햇살 충전 광합성 산책로", "하양 광합성 산책로" -> "sunlight"
                            "잔잔한 옥빛 개울가 하천길", "에메랄드 계곡길" -> "stream"
                            else -> null
                        }
                        val displayActivityName = if (activityId != null) {
                            val resActId = context.resources.getIdentifier("act_${activityId}_name", "string", context.packageName)
                            if (resActId != 0) context.getString(resActId) else journal.behavioralInfo.activityName
                        } else journal.behavioralInfo.activityName

                        val displayDuration = if (activityId != null) {
                            val resDurId = context.resources.getIdentifier("act_${activityId}_duration", "string", context.packageName)
                            if (resDurId != 0) context.getString(resDurId) else journal.behavioralInfo.duration
                        } else journal.behavioralInfo.duration

                        // 기분 라벨 역변환 (저장된 한국어 → 현재 언어)
                        val moodKeyMap = mapOf(
                            "상쾌해짐" to "rc_mood_refreshed",
                            "차분해짐" to "rc_mood_calm",
                            "가벼워짐" to "rc_mood_relieved",
                            "보통임" to "rc_mood_neutral"
                        )
                        val savedMoodKey = moodKeyMap[journal.behavioralInfo.postMood]
                        val displayPostMood = if (savedMoodKey != null) {
                            val resMoodId = context.resources.getIdentifier(savedMoodKey, "string", context.packageName)
                            if (resMoodId != 0) context.getString(resMoodId) else journal.behavioralInfo.postMood
                        } else journal.behavioralInfo.postMood

                        item {
                            SanctuaryCard(
                                backgroundColor = Color(0xFFF9FBF8),
                                borderColor = Color(0xFF004D40).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.jd_behavioral_complete),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF004D40)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(id = R.string.jd_mission),
                                            fontSize = 10.sp,
                                            color = MediumGray
                                        )
                                        Text(
                                            displayActivityName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkCharcoal
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = stringResource(id = R.string.jd_duration),
                                            fontSize = 10.sp,
                                            color = MediumGray
                                        )
                                        Text(
                                            displayDuration,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkCharcoal
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = stringResource(id = R.string.jd_walk_distance),
                                            fontSize = 10.sp,
                                            color = MediumGray
                                        )
                                        Text(
                                            journal.behavioralInfo.distance,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkCharcoal
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = stringResource(id = R.string.jd_mood_after),
                                            fontSize = 10.sp,
                                            color = MediumGray
                                        )
                                        Text(
                                            displayPostMood,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF004D40)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = stringResource(id = R.string.jd_walk_note),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF004D40)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = journal.behavioralInfo.reflectionText,
                                    fontSize = 13.sp,
                                    color = DarkCharcoal,
                                    lineHeight = 18.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                }

                "exposure" -> {
                    if (journal.exposureInfo != null) {
                        item {
                            SanctuaryCard(
                                backgroundColor = Color(0xFFFAF9F6),
                                borderColor = Color(0xFF596245).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.jd_positive_aspect),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF596245)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // 1) Photo view
                                if (journal.exposureInfo.photoUri.isNotBlank()) {
                                    AsyncImage(
                                        model = journal.exposureInfo.photoUri,
                                        contentDescription = "Uploaded Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                1.dp,
                                                Color(0xFFE4E2E2),
                                                RoundedCornerShape(12.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // 2) AI Compliments title
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(bottom = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF596245),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.jd_ai_perspective),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF444844)
                                    )
                                }

                                // 3) AI Compliment pills
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    journal.exposureInfo.compliments.forEach { compliment ->
                                        // compliment 라벨: 한국어 명칭 → CbtLocales 키로 변환
                                        val complimentKeyMap = mapOf(
                                            "반짝이는 눈망울" to "exposure_compliment_1",
                                            "따뜻한 미소" to "exposure_compliment_2",
                                            "건강한 에너지" to "exposure_compliment_3",
                                            "부드러운 얼굴 선" to "exposure_compliment_4",
                                            "긍정적인 분위기" to "exposure_compliment_5"
                                        )
                                        val displayCompliment = complimentKeyMap[compliment]?.let {
                                            val resCompId = context.resources.getIdentifier(it, "string", context.packageName)
                                            if (resCompId != 0) context.getString(resCompId) else compliment
                                        } ?: compliment

                                        val icon = when (compliment) {
                                            "반짝이는 눈망울" -> Icons.Default.Star
                                            "따뜻한 미소" -> Icons.Default.SentimentSatisfied
                                            "건강한 에너지" -> Icons.Default.LightMode
                                            "부드러운 얼굴 선" -> Icons.Default.Face
                                            "긍정적인 분위기" -> Icons.Default.Mood
                                            else -> Icons.Default.Star
                                        }
                                        val chipColor = when (compliment) {
                                            "반짝이는 눈망울" -> Color(0xFFDDE7C2)
                                            "따뜻한 미소" -> Color(0xFFF0FFD3)
                                            "건강한 에너지" -> Color(0xFFFAF9F6)
                                            "부드러운 얼굴 선" -> Color(0xFFDDE7C2)
                                            "긍정적인 분위기" -> Color(0xFFBECCA3)
                                            else -> Color(0xFFEFEDED)
                                        }
                                        val textColor = when (compliment) {
                                            "반짝이는 눈망울" -> Color(0xFF171E07)
                                            "따뜻한 미소" -> Color(0xFF6A7754)
                                            "건강한 에너지" -> Color(0xFF727270)
                                            "부드러운 얼굴 선" -> Color(0xFF424A2F)
                                            "긍정적인 분위기" -> Color(0xFF3F4B2C)
                                            else -> Color(0xFF1B1C1C)
                                        }
                                        val borderStroke =
                                            if (compliment == "건강한 에너지") BorderStroke(
                                                1.dp,
                                                Color(0xFFC5C7C3)
                                            ) else null

                                        Box(
                                            modifier = Modifier
                                                .background(chipColor, CircleShape)
                                                .run {
                                                    if (borderStroke != null) this.border(
                                                        borderStroke.width,
                                                        borderStroke.brush,
                                                        CircleShape
                                                    ) else this
                                                }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    icon,
                                                    null,
                                                    tint = textColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    displayCompliment,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = stringResource(id = R.string.jd_reflection_content),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF596245)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = journal.exposureInfo.reflectionText,
                                    fontSize = 13.sp,
                                    color = DarkCharcoal,
                                    lineHeight = 18.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                }

                else -> {
                    item {
                        SanctuaryCard {
                            Text(
                                text = stringResource(id = R.string.jd_socratic_question),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SagePrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = journal.socraticQuestion,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkCharcoal,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(id = R.string.jd_rational_wisdom),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SagePrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = journal.selectedOption,
                                fontSize = 13.sp,
                                color = DarkCharcoal,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                SanctuaryButton(
                    text = stringResource(id = R.string.jd_home_btn),
                    onClick = onHomeClick
                )
            }
        }
    }
}

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
        val filename = "CBT_Journal_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CBT_Sanctuary")
        }
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
        }
    }


