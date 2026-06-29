package com.example.termproject

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest // 💡 Added missing import
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import com.example.termproject.ui.theme.LinenBackground
import com.example.termproject.ui.theme.SagePrimary
import com.example.termproject.ui.theme.MediumGray
import com.example.termproject.ui.theme.DarkCharcoal
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.termproject.ui.component.*
import com.example.termproject.ui.screen.*
import com.example.termproject.ui.theme.CbtSanctuaryTheme
import com.example.termproject.viewmodel.CbtViewModel
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.termproject.utils.ImageUtils
import com.example.termproject.viewmodel.DistortionCount
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource

/**
 * 앱 진입점. Compose 기반 MainActivity.
 */
class MainActivity : AppCompatActivity() {
    private val viewModel: CbtViewModel by viewModels()
    // 💡 Kept registration simple to avoid initialization/instance variable errors
    // 1. Register the contract (Place this as a member variable in your Activity)
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Successfully selected: $uri")

            // 1. Save the image to your app's internal storage IMMEDIATELY
            val savedFile = ImageUtils.saveImageToInternalStorage(this, uri)

            if (savedFile != null) {
                // 2. Pass the PERMANENT local file path to your ViewModel
                viewModel.updateSetupPhotoUri(savedFile.absolutePath)
            } else {
                Log.e("PhotoPicker", "Failed to save image locally")
            }
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }


// 2. Launch the picker (e.g., inside a button click)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            CbtSanctuaryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LinenBackground
                ) {
                    // 💡 Pass the execution trigger event down into the Compose layout tree
                    CbtSanctuaryApp(
                        viewModel = viewModel,
                        onTriggerPhotoPicker = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CbtSanctuaryApp(

    viewModel: CbtViewModel = viewModel(),
    onTriggerPhotoPicker: () -> Unit // 💡 Received the event callback here
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            val resId = context.resources.getIdentifier(msg, "string", context.packageName)
            val localizedMsg = if (resId != 0) context.getString(resId) else msg
            snackbarHostState.showSnackbar(localizedMsg)
            viewModel.clearSnackbar()
        }
    }


    val isMainWorkflow = uiState.step != CbtStep.Splash &&
            uiState.step != CbtStep.ModelDownload &&
            uiState.step != CbtStep.Login &&
            uiState.step != CbtStep.SignUp &&
            uiState.step != CbtStep.ProfileSetup

    val isMainTabStep = uiState.step == CbtStep.Entry ||
            uiState.step == CbtStep.Insight ||
            uiState.step == CbtStep.Profile

    BackHandler(enabled = isMainWorkflow && !isMainTabStep) {
        viewModel.navigateBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(LinenBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ══════════════════════════════════════════
            // 상단 헤더
            // ══════════════════════════════════════════
            if (isMainWorkflow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!isMainTabStep) {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.back),
                                tint = MediumGray
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable { viewModel.resetToHome() }
                    ) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CBT Sanctuary",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SagePrimary,
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    IconButton(onClick = { viewModel.toggleSettingsDialog(true) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = R.string.settings),
                            tint = MediumGray
                        )
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
            }

            // ══════════════════════════════════════════
            // 메인 콘텐츠 영역 (단계별 애니메이션 전환)
            // ══════════════════════════════════════════
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = if (isMainWorkflow) 18.dp else 0.dp)
            ) {
                AnimatedContent(
                    targetState = uiState.step,
                    transitionSpec = {
                        if (targetState == CbtStep.Analysing) {
                            fadeIn(animationSpec = tween(400)) togetherWith
                                    fadeOut(animationSpec = tween(400))
                        } else {
                            (slideInHorizontally(animationSpec = tween(320)) { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally(animationSpec = tween(320)) { -it } + fadeOut())
                        }
                    },
                    label = "MainContentAnimation"
                ) { currentStep ->
                    when (currentStep) {

                        CbtStep.ModelDownload -> ModelDownloadScreen(
                            progress   = uiState.downloadProgress,
                            statusText = uiState.downloadStatusText,
                            error      = uiState.downloadError,
                            onStart    = { viewModel.startModelDownload() },
                            onRetry    = { viewModel.startModelDownload() }
                        )

                        CbtStep.Splash -> SplashScreen(
                            currentLanguage = uiState.currentLanguage,
                            onTimeout = { viewModel.checkSessionAndNavigate() }
                        )

                        CbtStep.Login -> {
                            val context = LocalContext.current
                            val coroutineScope = rememberCoroutineScope()
                            LoginScreen(
                                email = uiState.loginEmail,
                                password = uiState.loginPassword,
                                currentLanguage = uiState.currentLanguage,
                                onEmailChange = { viewModel.updateLoginEmail(it) },
                                onPasswordChange = { viewModel.updateLoginPassword(it) },
                                onLoginClick = { viewModel.signIn() },
                                onGoogleLoginClick = {
                                    coroutineScope.launch {
                                        try {
                                            val credentialManager = androidx.credentials.CredentialManager.create(context)
                                            val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                                                .setFilterByAuthorizedAccounts(false)
                                                .setServerClientId("272368930845-his4i4uh2blve6rvokk1ibns6q3ja4bt.apps.googleusercontent.com")
                                                .build()

                                            val request = androidx.credentials.GetCredentialRequest.Builder()
                                                .addCredentialOption(googleIdOption)
                                                .build()

                                            val result = credentialManager.getCredential(context = context, request = request)
                                            val credential = result.credential
                                            if (credential is androidx.credentials.CustomCredential && 
                                                credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                                                val idToken = googleIdTokenCredential.idToken
                                                viewModel.signInWithGoogle(idToken)
                                            } else {
                                                Log.e("GoogleLogin", "Unexpected credential type")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("GoogleLogin", "Google Sign-In failed: ${e.message}", e)
                                        }
                                    }
                                },
                                onNavigateToSignUp = { viewModel.navigateTo(CbtStep.SignUp) }
                            )
                        }

                        CbtStep.SignUp -> SignUpScreen(
                            email = uiState.signUpEmail,
                            password = uiState.signUpPassword,
                            currentLanguage = uiState.currentLanguage,
                            onEmailChange = { viewModel.updateSignUpEmail(it) },
                            onPasswordChange = { viewModel.updateSignUpPassword(it) },
                            onSignUpClick = { viewModel.signUp() },
                            onBackToLogin = { viewModel.navigateTo(CbtStep.Login) }
                        )

                        CbtStep.ProfileSetup -> ProfileSetupScreen(
                            nickname = uiState.setupNickname,
                            jobTitle = uiState.setupJobTitle,
                            photoUri = uiState.setupPhotoUri,
                            currentLanguage = uiState.currentLanguage,
                            onNicknameChange = { viewModel.updateSetupNickname(it) },
                            onJobTitleChange = { viewModel.updateSetupJobTitle(it) },
                            // 💡 Trigger the system Photo Picker using the lambda reference here
                            onPhotoChange = { onTriggerPhotoPicker() },
                            onBackClick = { viewModel.navigateBack() },
                            onSaveProfile = { viewModel.saveProfile() }
                        )

                        CbtStep.Profile -> ProfileScreen(
                            photoUri = uiState.setupPhotoUri,
                            nickname = uiState.setupNickname,
                            jobTitle = uiState.setupJobTitle,
                            email = uiState.savedEmail.ifBlank { uiState.loginEmail },
                            savedJournals = uiState.savedJournals,
                            currentLanguage = uiState.currentLanguage,
                            onSignOut = { viewModel.signOut() },
                            onNavigateToEditProfile = { viewModel.navigateTo(CbtStep.ProfileSetup) },
                            onJournalClick = { viewModel.openJournalDetail(it) }
                        )

                        CbtStep.Entry -> {
                            LaunchedEffect(Unit) {
                                viewModel.updateWeather(35.1796, 129.0756) // Busan coordinates
                            }
                            EntryScreen(
                                currentWeather = uiState.currentWeather,
                                thoughtText = uiState.thoughtText,
                                currentLanguage = uiState.currentLanguage,
                                onThoughtChange = { viewModel.updateThought(it) },
                                onAnalyzeClick = { viewModel.analyzeThought() },
                                onTemplateClick = { viewModel.useTemplate(it) }
                            )
                        }
                        CbtStep.Analysing -> AnalysingScreen(
                            title = if (uiState.currentLanguage == "en") "Scanning Mind Pollution Filters..." else "마음 오염 왜곡 필터 스캔 중..",
                            description = if (uiState.currentLanguage == "en") {
                                "Identifying negative automatic thoughts (ANT) and performing diagnostic analysis..."
                            } else {
                                "자동으로 피어난 부정적 비관 왜곡(ANT)을 식별하고 정성적으로 조율하기 위해 치료 규칙 데이터베이스 엔진을 대칭 분석하는 중입니다."
                            }
                        )

                        CbtStep.SocraticAnalysing -> AnalysingScreen(
                            title = if (uiState.currentLanguage == "en") "Designing Socratic Questioning..." else "소크라테스식 반론 질문 설계 중..",
                            description = if (uiState.currentLanguage == "en") {
                                "Generating tailored reflective questions and realistic alternative viewpoints to counter detected cognitive distortions..."
                            } else {
                                "감지된 인지 왜곡에 대항하고 균형 잡힌 대안적 생각을 도출하기 위해, 인지 왜곡 맞춤형 성찰 가이드 질문과 현실적 대안 시각들을 논리적으로 생성하고 있습니다."
                            }
                        )

                        CbtStep.DistortionReview -> uiState.analysisResult?.let { result ->
                            DistortionReviewScreen(
                                thoughtText = uiState.thoughtText,
                                result = result,
                                currentLanguage = uiState.currentLanguage,
                                // ✅ 왜곡 리포트 확인 후 실천할 작업 선택 화면으로 이동
                                onNextClick = { viewModel.navigateTo(CbtStep.CategorySelect) }
                            )
                        }

                        CbtStep.SocraticCheck -> FactCheckScreen(
                            analysisResult = uiState.analysisResult,
                            onOptionSelected = { chosenOption -> viewModel.selectOption(chosenOption) },
                            // ✅ 소크라테스 질문 완료 후 대안 사고 화면으로 이동
                            onNextClick = { viewModel.navigateTo(CbtStep.AlternativeReframe) },
                            thoughtText = uiState.thoughtText,
                            currentLanguage = uiState.currentLanguage,
                            onLoadDynamicSocraticData = { _, _ -> }
                        )

                        CbtStep.AlternativeReframe -> {
                            uiState.analysisResult?.let { safeResult ->
                                AlternativeReframeScreen(
                                    result = safeResult,
                                    selectedOption = uiState.selectedOption,
                                    currentLanguage = uiState.currentLanguage,
                                    // ✅ 인지 작업 완료 후 저장만 가능 (분기 선택은 이미 CategorySelect에서 완료)
                                    onSaveClick = {
                                        viewModel.saveJournal()
                                    }
                                )
                            }
                        }

                        CbtStep.CategorySelect -> CategorySelectScreen(
                            currentLanguage = uiState.currentLanguage,
                            // ✅ 선택된 경로로 분기: cognitive → SocraticCheck, behavioral → BehaviorRecommend, exposure → ExposureActive
                            onSelectAction = { path -> viewModel.choosePathType(path) }
                        )

                        CbtStep.BehavioralAnalysing -> AnalysingScreen(
                            title = if (uiState.currentLanguage == "en") "Finding Healing Spots Near You..." else "내 주변 마음챙김 명소 탐색 중..",
                            description = if (uiState.currentLanguage == "en") {
                                "Recommending optimal natural paths near your coordinates tailored to help soothe your mind..."
                            } else {
                                "현재 위치와 부정적 생각을 기반으로 마음에 쉼을 줄 수 있는 최적의 자연 산책 명소(금정산성, 온천천 등)를 탐색하고 있습니다."
                            }
                        )

                        // In CbtSanctuaryApp() -> when (currentStep) { ... }
                        CbtStep.BehaviorRecommend -> BehaviorRecommendScreen(
                            activities = uiState.behavioralActivities,
                            selectedId = uiState.selectedActivity?.id,
                            currentLanguage = uiState.currentLanguage,
                            onActivitySelect = { viewModel.selectBehavioralActivity(it) },
                            onNextClick = { viewModel.navigateTo(CbtStep.MapNavigation) },
                            onRefresh = { viewModel.refreshNearbyActivities() } // ✅ Pass the ViewModel function here
                        )
                        // In MainActivity.kt / CbtSanctuaryApp function
                        CbtStep.MapNavigation -> uiState.selectedActivity?.let { activity ->
                            MapNavigationScreen(
                                activity = activity,
                                currentLocation = uiState.currentLocation,
                                destinationLocation = uiState.destinationLocation,
                                navigationPath = uiState.navigationPath,
                                trackedPath = uiState.trackedPath, // ✅ Pass the state from your UI State here
                                currentLanguage = uiState.currentLanguage,
                                onPermissionGranted = { viewModel.startLocationUpdates() },
                                onStartClick = {
                                    viewModel.startTimer()
                                    viewModel.navigateTo(CbtStep.TimerActive)
                                },
                                onArrived = { viewModel.stopTimerAndGoToReflection() }
                            )
                        }
                        CbtStep.TimerActive -> uiState.selectedActivity?.let { activity ->
                            TimerActiveScreen(
                                activity = activity,
                                durationSecs = uiState.timerSeconds,
                                walkDistanceMeters = uiState.walkDistanceMeters,
                                isPaused = uiState.timerPaused,
                                trackedPath = uiState.trackedPath,
                                navigationPath = uiState.navigationPath,
                                currentLocation = uiState.currentLocation,
                                destinationLocation = uiState.destinationLocation,
                                currentLanguage = uiState.currentLanguage,
                                onPauseToggle = { viewModel.toggleTimer() },
                                onCompleteClick = { viewModel.stopTimerAndGoToReflection() }
                            )
                        }

                        CbtStep.ReflectionComplete -> uiState.selectedActivity?.let { activity ->
                            ReflectionCompleteScreen(
                                activity = activity,
                                durationSecs = uiState.timerSeconds,
                                walkDistanceMeters = uiState.walkDistanceMeters,
                                selectedMood = uiState.selectedMood,
                                reflectionText = uiState.reflectionText,
                                currentLanguage = uiState.currentLanguage,
                                onMoodSelect = { viewModel.selectPostMood(it) },
                                onReflectionChange = { viewModel.updateReflectionText(it) },
                                onSaveJournalClick = { viewModel.saveBehavioralJournal() }
                            )
                        }

                        CbtStep.ExposureActive -> {
                            val context = LocalContext.current
                            ExposureScreen(
                                currentLanguage = uiState.currentLanguage,
                                dynamicTags = uiState.exposureTags,
                                aiAnalysisText = uiState.exposureAiAnalysis,
                                isAnalyzingImage = uiState.isAnalyzingImage,
                                onAnalyzeClick = { uri ->
                                    viewModel.runExposureAiAnalysis(context, uri)
                                },
                                onSaveExposure = { text, uri ->
                                    viewModel.saveExposureJournal(text, uri)
                                }
                            )
                        }

                        CbtStep.Insight -> {
                            val currentMoodRating by viewModel.todayRating.collectAsStateWithLifecycle()
                            val topDistortions: List<DistortionCount> = remember(uiState.savedJournals) {
                                viewModel.getTopDistortions()
                            }
                            // 💡 2. Pass the observed value and the update lambda to the screen
                            InsightScreen(
                                totalJournalsCount = uiState.savedJournals.size,
                                savedJournals = uiState.savedJournals,
                                todayMoodRating = currentMoodRating,
                                topDistortions = topDistortions, // 👈 Pass it here
                                currentLanguage = uiState.currentLanguage,
                                onTodayMoodRatingChange = { newRating ->
                                    viewModel.updateTodayMoodRating(
                                        newRating
                                    )
                                },
                                onNavigateToCbt = { viewModel.navigateTo(CbtStep.Entry)},
                                viewModel =  viewModel
                            )
                        }
                        CbtStep.ViewJournalDetail -> uiState.activeJournal?.let { journal ->
                            JournalDetailScreen(
                                journal = journal,
                                currentLanguage = uiState.currentLanguage,
                                onHomeClick = { viewModel.resetToHome() }
                            )
                        }
                    }
                }
            }

            if (isMainTabStep) {
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CbtBottomTabItem(
                        icon = Icons.Default.Home,
                        label = stringResource(id = R.string.bottom_tab_home),
                        isSelected = uiState.step == CbtStep.Entry,
                        onClick = { viewModel.navigateTo(CbtStep.Entry) }
                    )
                    CbtBottomTabItem(
                        icon = Icons.Default.Insights,
                        label = stringResource(id = R.string.bottom_tab_insight),
                        isSelected = uiState.step == CbtStep.Insight,
                        onClick = { viewModel.navigateTo(CbtStep.Insight) }
                    )
                    CbtBottomTabItem(
                        icon = Icons.Default.Person,
                        label = stringResource(id = R.string.bottom_tab_profile),
                        isSelected = uiState.step == CbtStep.Profile,
                        onClick = { viewModel.navigateTo(CbtStep.Profile) }
                    )
                }
            }
        }

        // ══════════════════════════════════════════
        // 정보 다이얼로그 (CBT 가이드)
        // ══════════════════════════════════════════
        if (uiState.showInfoDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleInfoDialog(false) },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "",
                            tint = SagePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.guide_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkCharcoal
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(id = R.string.guide_intro),
                            fontSize = 12.sp,
                            color = MediumGray,
                            lineHeight = 17.sp
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                        Text(
                            text = stringResource(id = R.string.guide_body),
                            fontSize = 11.sp,
                            color = DarkCharcoal,
                            lineHeight = 17.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.toggleInfoDialog(false) }) {
                        Text(
                            text = stringResource(id = R.string.guide_close),
                            fontWeight = FontWeight.Bold,
                            color = SagePrimary
                        )
                    }
                },
                shape = RoundedCornerShape(18.dp),
                containerColor = Color.White
            )
        }

        // ══════════════════════════════════════════
        // 설정 다이얼로그
        // ══════════════════════════════════════════
        if (uiState.showSettingsDialog) {
            SettingsDialog(
                currentLanguage = uiState.currentLanguage,
                onLanguageChange = { viewModel.changeLanguage(it) },
                onEditProfile = { viewModel.navigateTo(CbtStep.ProfileSetup) },
                onDismiss = { viewModel.toggleSettingsDialog(false) }
            )
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}

@Composable
fun CbtBottomTabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = SagePrimary
    val inactiveColor = MediumGray.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .width(76.dp)
            .height(56.dp)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val backgroundShape = RoundedCornerShape(14.dp)
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(28.dp)
                .background(
                    color = if (isSelected) activeColor.copy(alpha = 0.12f) else Color.Transparent,
                    shape = backgroundShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}

@Composable
fun SettingsDialog(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onEditProfile: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = SagePrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.settings),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoal
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.settings_lang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MediumGray
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isKo = currentLanguage == "ko"
                    Button(
                        onClick = { onLanguageChange("ko") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isKo) SagePrimary else Color(0xFFEFEDED)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.lang_ko),
                            color = if (isKo) Color.White else DarkCharcoal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    val isEn = currentLanguage == "en"
                    Button(
                        onClick = { onLanguageChange("en") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEn) SagePrimary else Color(0xFFEFEDED)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.lang_en),
                            color = if (isEn) Color.White else DarkCharcoal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                OutlinedButton(
                    onClick = {
                        onEditProfile()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBDCD0)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SagePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.edit_profile),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.close),
                    fontWeight = FontWeight.Bold,
                    color = SagePrimary
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}