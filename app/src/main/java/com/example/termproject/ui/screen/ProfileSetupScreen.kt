package com.example.termproject.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import com.example.termproject.R
import java.io.File
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    nickname: String,
    jobTitle: String,
    photoUri: String,
    currentLanguage: String = "ko",
    onNicknameChange: (String) -> Unit,
    onJobTitleChange: (String) -> Unit,
    onPhotoChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSaveProfile: () -> Unit
) {
    var isSaving by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onPhotoChange(uri.toString())
        }
    }

    val handleSave = {
        if (nickname.isNotBlank()) {
            isSaving = true
            // 로컬 스피너 및 성공 연출 효과
            onSaveProfile()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F0E8), // 세이지 힐링 스페이스
                        Color(0xFFFCFAF7)  // 린넨 순백 마감
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isSaving) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                    tint = Color(0xFF2E3E34)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
                .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isSaving) {
                // 1) 기본 편집 화면
                Text(
                    text = "Create Your Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E3E34),
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(id = R.string.profile_setup_title),
                    fontSize = 12.sp,
                    color = Color(0xFF7A8B80),
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(26.dp))

                // 가상의 원형 프로필 아바타 및 플로팅 카메라 탑재 업로더
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clickable { launcher.launch("image/*") }, // 갤러리 직접 실행
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFFE2EBE5))
                            .border(2.dp, Color(0xFFB0CDBC), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Check if string is a valid file path and exists
                        val file = if (photoUri.isNotBlank()) File(photoUri) else null

                        if (file != null && file.exists()) {
                            AsyncImage(
                                model = file,
                                contentDescription = "Profile Photo",
                                modifier = Modifier.size(100.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Profile",
                                modifier = Modifier.size(100.dp),
                                tint = Color(0xFF8B9D91)
                            )
                        }
                    }

                    // 카메라 플로팅 버튼
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6B8E7B))
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change photo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.profile_avatar_label),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8B9D91)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 닉네임 입력란
                OutlinedTextField(
                    value = nickname,
                    onValueChange = onNicknameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(id = R.string.profile_nickname_label), color = Color(0xFF6B8E7B)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Nickname",
                            tint = Color(0xFF8B9D91)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6B8E7B),
                        unfocusedBorderColor = Color(0xFFCBDCD0),
                        focusedContainerColor = Color(0xFFFAFAF9),
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 직장 및 역할 입력란
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = onJobTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(id = R.string.profile_job_label), color = Color(0xFF6B8E7B)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = "Role",
                            tint = Color(0xFF8B9D91)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { handleSave() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6B8E7B),
                        unfocusedBorderColor = Color(0xFFCBDCD0),
                        focusedContainerColor = Color(0xFFFAFAF9),
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 프로필 저장 및 탐색 시작 버튼 (빈 칸 검증 진행)
                Button(
                    onClick = { handleSave() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = nickname.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6B8E7B),
                        disabledContainerColor = Color(0xFFD2DDD6)
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_save_btn),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                // 2) 프로필 생성 중 가상 전환 스켈레톤/스피너 연출 및 최종 환영 메시지
                LaunchedEffect(Unit) {
                    delay(1200)
                    saveSuccess = true
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!saveSuccess) {
                        CircularProgressIndicator(
                            color = Color(0xFF6B8E7B),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(id = R.string.profile_building),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4A6854)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Welcome Home Success",
                            tint = Color(0xFF6B8E7B),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Welcome Home",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E3E34)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.profile_build_success),
                            fontSize = 12.sp,
                            color = Color(0xFF7A8B80),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
