package com.example.termproject.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.termproject.model.SavedJournal
import com.example.termproject.ui.component.*
import com.example.termproject.viewmodel.WeatherInfo
import androidx.compose.ui.res.stringResource
import com.example.termproject.R

// ══════════════════════════════════════════════════════════════
// STEP 0 — 생각 입력 / 저장 일지 리스트 화면
// ══════════════════════════════════════════════════════════════

@Composable
fun EntryScreen(
    currentWeather: WeatherInfo?,
    thoughtText: String,
    currentLanguage: String = "ko",
    onThoughtChange: (String) -> Unit,
    onAnalyzeClick: () -> Unit,
    onTemplateClick: (String) -> Unit
) {val handleAnalyze = remember(onAnalyzeClick) {
    {
        if (thoughtText.trim().isNotEmpty()) {
            onAnalyzeClick()
        }
    }
}
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentWeather != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SoftSageLight.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "☁️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = stringResource(id = R.string.weather_title), fontSize = 12.sp, color = MediumGray)
                                Text(
                                    text = "${currentWeather.temperature} | ${currentWeather.condition}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkCharcoal
                                )
                            }
                        }
                    }
                } else {
                    // 🟢 THIS WILL SHOW UP IF THE DATA ISN'T LOADED YET
                    Text(
                        text = "No weather data yet",
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.entry_hero_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkCharcoal,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
                Text(
                    text = stringResource(id = R.string.entry_hero_subtitle),
                    fontSize = 12.sp,
                    color = MediumGray,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }



        }

        // 텍스트 입력 카드
        // 텍스트 입력 카드 (REPLACE THIS SECOND ITEM BLOCK)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = thoughtText,
                    onValueChange = { newValue ->
                        // Only accept the change if it doesn't exceed 500 characters
                        // This keeps the input connection stable for multi-stage IME characters
                        if (newValue.length <= 500) {
                            onThoughtChange(newValue)
                        }
                    },
                    textStyle = TextStyle(
                        color = DarkCharcoal,
                        fontSize = 15.sp,
                        lineHeight = 23.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.entry_input_placeholder),
                            color = Color.LightGray,
                            fontSize = 15.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default // Switch back to Default with the new lambda rule
                    ),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = SagePrimary,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )

                // Below the TextField, keep your character count and Send button row:
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${thoughtText.length} / 500",
                        fontSize = 12.sp,
                        color = MediumGray.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 14.dp)
                    )

                    // Send Button
                    IconButton(
                        onClick = onAnalyzeClick,
                        enabled = thoughtText.trim().isNotEmpty(),
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = if (thoughtText.trim().isNotEmpty())
                                    SoftSageLight
                                else
                                    Color(0xFFF3F2EE),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "인지 왜곡 분석하기",
                            tint = if (thoughtText.trim().isNotEmpty())
                                SagePrimary
                            else
                                Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }



        // 템플릿 제안
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Text(
                    text = stringResource(id = R.string.entry_template_hint),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SagePrimary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                val templates = listOf(
                    stringResource(id = R.string.entry_tpl_rel_title) to stringResource(id = R.string.entry_tpl_rel_body),
                    stringResource(id = R.string.entry_tpl_ach_title) to stringResource(id = R.string.entry_tpl_ach_body),
                    stringResource(id = R.string.entry_tpl_pub_title) to stringResource(id = R.string.entry_tpl_pub_body)
                )

                templates.forEach { (title, fullTxt) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onTemplateClick(fullTxt) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SagePrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = fullTxt,
                                    fontSize = 12.sp,
                                    color = MediumGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "사용",
                                tint = SoftSageLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── 명상 가이드 다운로드 배너 ─────────────────────────────
        item {
            MeditationGuideDownloadBanner()
        }

    }
}

// ──────────────────────────────────────────────────────────────
// 명상 가이드 PDF 다운로드 배너 컴포저블
// ──────────────────────────────────────────────────────────────
@Composable
fun MeditationGuideDownloadBanner() {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F4EB)
        ),
        border = BorderStroke(1.dp, SoftSageLight.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "🧘",
                    fontSize = 22.sp,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Column {
                    Text(
                        text = "호흡 명상 가이드",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SagePrimary
                    )
                    Text(
                        text = "UCLA 공식 한국어 명상 자료 · PDF",
                        fontSize = 11.sp,
                        color = MediumGray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Button(
                onClick = { downloadMeditationGuide(context) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SagePrimary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "다운로드",
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "받기",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// DownloadManager로 PDF 다운로드 실행
// ──────────────────────────────────────────────────────────────
fun downloadMeditationGuide(context: Context) {
    val url = "https://www.uclahealth.org/sites/default/files/documents/a7/korean-breathing.pdf?f=9b9aa522"
    val fileName = "UCLA_호흡명상가이드.pdf"

    try {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("호흡 명상 가이드")
            setDescription("UCLA 한국어 명상 자료를 다운로드하고 있습니다...")
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )
            setMimeType("application/pdf")
            addRequestHeader("User-Agent", "Mozilla/5.0")
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)

        Toast.makeText(
            context,
            "📥 다운로드가 시작되었습니다. 알림에서 확인하세요.",
            Toast.LENGTH_SHORT
        ).show()

    } catch (e: Exception) {
        Toast.makeText(
            context,
            "다운로드 중 오류가 발생했습니다.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
