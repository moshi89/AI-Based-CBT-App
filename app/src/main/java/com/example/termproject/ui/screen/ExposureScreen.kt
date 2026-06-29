package com.example.termproject.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.example.termproject.R
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExposureScreen(
    currentLanguage: String = "ko",
    dynamicTags: List<String> = emptyList(),          // 💡 Dynamic tags from Gemini
    aiAnalysisText: String = "",                      // 💡 Dynamic descriptive text from Gemini
    isAnalyzingImage: Boolean = false,                // Loading flag state
    onAnalyzeClick: (String) -> Unit = {},            // Triggers manual button run
    onSaveExposure: (reflectionText: String, photoUri: String) -> Unit
) {
    var reflectionText by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val defaultDescription = stringResource(id = R.string.exposure_ai_desc)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF9F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .drawBehindDashedBorder(
                        color = Color(0xFFE4E2E2),
                        strokeWidth = 1.5.dp
                    )
                    .clickable { if (photoUri.isBlank()) launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri.isNotBlank()) {
                    // Image display layer
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Selected Asset",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Loading or Action overlay layer
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAnalyzingImage) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Button(
                                onClick = { onAnalyzeClick(photoUri) },
                                shape = RoundedCornerShape(99.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF566342
                                    )
                                )
                            ) {
                                Text(stringResource(id = R.string.exposure_ai_analyze_btn), color = Color.White)
                            }
                        }
                    }
                } else {
                    // Empty state placeholder
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp)
                                .background(Color(0xFFF2F0EF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                null,
                                tint = Color(0xFF566342),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            stringResource(id = R.string.exposure_upload_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            // 2) AI 의 따뜻한 시선" Box Card Layout (Matches image_f53101.png)
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE4E2E2).copy(alpha = 0.6f)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF566342),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.exposure_ai_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF444844)
                        )
                    }

                    // 💡 Positive Tags Layout Area
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        if (dynamicTags.isNotEmpty()) {
                            // Render parsed keywords directly from Gemini
                            dynamicTags.forEachIndexed { index, tag ->
                                val chipBg =
                                    if (index == 0) Color(0xFFDDE7C2) else Color(
                                        0xFFF0FFD3
                                    )
                                val chipText =
                                    if (index == 0) Color(0xFF171E07) else Color(
                                        0xFF566342
                                    )
                                Box(
                                    modifier = Modifier.background(chipBg, CircleShape)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            if (index == 0) Icons.Default.Star else Icons.Default.Mood,
                                            null,
                                            tint = chipText,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = tag,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = chipText
                                        )
                                    }
                                }
                            }
                        } else {
                            // Default layout placeholders matching sample view
                            Box(
                                modifier = Modifier.background(
                                    Color(0xFFDDE7C2),
                                    CircleShape
                                ).padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        null,
                                        tint = Color(0xFF171E07),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        stringResource(id = R.string.exposure_compliment_1),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF171E07)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier.background(
                                    Color(0xFFF0FFD3),
                                    CircleShape
                                ).padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Mood,
                                        null,
                                        tint = Color(0xFF566342),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        stringResource(id = R.string.exposure_compliment_2),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF566342)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier.background(Color.White, CircleShape)
                                    .border(1.dp, Color(0xFFC5C7C3), CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LightMode,
                                        null,
                                        tint = Color(0xFF727270),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        stringResource(id = R.string.exposure_compliment_3),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF727270)
                                    )
                                }
                            }
                        }
                    }

                    // Description text paragraph container output
                    Text(
                        text = aiAnalysisText.ifBlank { defaultDescription },
                        fontSize = 14.sp,
                        color = Color(0xFF1B1C1C),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3) Personal Reflection Text Field Input
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.exposure_input_label),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1B1C1C),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reflectionText,
                    onValueChange = { reflectionText = it },
                    placeholder = { Text(stringResource(id = R.string.exposure_input_placeholder)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF566342),
                        unfocusedBorderColor = Color(0xFFE4E2E2),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 4) Main Bottom Save Button
            val canSave =
                reflectionText.isNotBlank() && photoUri.isNotBlank() && !isAnalyzingImage
            Button(
                onClick = { onSaveExposure(reflectionText, photoUri) },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF566342),
                    disabledContainerColor = Color(0xFFEAE8E7)
                ),
                shape = RoundedCornerShape(99.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = if (canSave) Color.White else Color(0xFFC5C7C3),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.exposure_save_btn),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canSave) Color.White else Color(0xFF9E9E9E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.exposure_footer),
                fontSize = 12.sp,
                color = Color(0xFF727270),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
        }
    }
}
private fun Modifier.drawBehindDashedBorder(color: Color, strokeWidth: Dp): Modifier = this.drawBehind {
    val density = this.density
    val strokeWidthPx = strokeWidth.toPx()
    drawRoundRect(
        color = color,
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f * density, 8f * density), 0f)
        ),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx())
    )
}