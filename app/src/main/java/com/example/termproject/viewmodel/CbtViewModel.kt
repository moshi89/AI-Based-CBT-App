package com.example.termproject.viewmodel

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.termproject.BuildConfig
import com.example.termproject.CbtStep
import com.example.termproject.model.BehavioralActivity
import com.example.termproject.model.BehavioralInfo
import com.example.termproject.model.CbtAnalysis
import com.example.termproject.model.ExposureInfo
import com.example.termproject.model.SavedJournal
import com.example.termproject.service.CbtAnalysisService
import com.example.termproject.service.FirebaseAuthService
import com.example.termproject.service.GeminiAnalysisService
import com.example.termproject.service.JournalRepository
import com.example.termproject.service.MoodInferenceService
import com.example.termproject.utils.ImageUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

/**
 * 전체 CBT 앱의 UI 상태를 담는 단일 불변 데이터 클래스.
 */
// Create a data class for your spots
data class Spot(val id: String, val name: String, val location: LatLng)

// Define your local database in the ViewModel
val busanSpots = listOf(
    Spot("beach", "Gwangalli Beach", LatLng(35.1531, 129.1187)),
    Spot("beach", "Haeundae Beach", LatLng(35.1587, 129.1604)),
    Spot("forest", "Geumjeongsan Mountain", LatLng(35.2575, 129.0558)),
    Spot("forest", "Busan Citizens Park", LatLng(35.1666, 129.0551))
)

// Then simply find the one that matches the activity type:
fun getNearbySpot(type: String): LatLng {
    return busanSpots.firstOrNull { it.id == type }?.location
        ?: LatLng(35.1796, 129.0756) // Fallback to Busan City Hall
}
data class WeatherInfo(
    val temperature: String = "--",
    val condition: String = "Loading...",
    val iconCode: String = "01d"
)
data class DistortionCount(val tag: String, val count: Int, val percentage: Float)
data class CbtUiState(
    val selectedImagePath: String? = null,
    val detectedMood: String? = null,
    val weeklyUsageMinutes: List<Int> = List(7) { 0 },
    val todayMoodRating: Int = 0,
    val currentWeather: WeatherInfo? = null,
    val exposureTags: List<String> = emptyList(),
    val exposureAiAnalysis: String = "",
    val isAnalyzingImage: Boolean = false,
    val step: CbtStep = CbtStep.ModelDownload,  // 앱 시작 시 우선 모델 확인
    val thoughtText: String = "",
    val analysisResult: CbtAnalysis? = null,
    val selectedOption: String? = null,
    val savedJournals: List<SavedJournal> = emptyList(),
    val activeJournal: SavedJournal? = null,
    val showInfoDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val currentLanguage: String = "ko",
    val snackbarMessage: String? = null,

    // ML 모델 다운로드 상태
    val downloadProgress: Float = 0f,          // 0f ~ 1f
    val downloadStatusText: String = "",        // 사용자에게 보여줄 메시지
    val downloadError: String? = null,          // null = 정상, else = 에러 메시지

    // 로그인, 회원가입, 프로필 연동 데이터
    val loginEmail: String = "",
    val loginPassword: String = "",
    val signUpEmail: String = "",
    val signUpPassword: String = "",
    val setupNickname: String = "",
    val setupJobTitle: String = "",
    val setupPhotoUri: String = "",
    val savedEmail: String = "",
    val savedPassword: String = "",
    val isLoggedIn: Boolean = false,

    // [Path B - Behavioral Fields]
    val pathType: String = "cognitive",
    val behavioralActivities: List<BehavioralActivity> = emptyList(),
    val selectedActivity: BehavioralActivity? = null,
    val timerSeconds: Int = 0,
    val timerPaused: Boolean = false,
    val walkDistanceMeters: Int = 0,
    val selectedMood: String? = null,
    val reflectionText: String = "",

    // 구글 맵 및 GPS 연동 데이터
    val trackedPath: List<LatLng> = emptyList(),
    val currentLocation: LatLng? = null,
    val destinationLocation: LatLng? = null,
    val navigationPath: List<LatLng> = emptyList()

)

/**
 * CBT Sanctuary 앱의 비즈니스 로직 및 상태 관리 ViewModel.
 */
class CbtViewModel(application: Application) : AndroidViewModel(application) {
    private val _dailyMoodRatings = MutableStateFlow<Map<String, Int>>(emptyMap())

    // 2. Expose the "today" rating derived from that map
    val todayRating: StateFlow<Int> = _dailyMoodRatings
        .map { map ->
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            map[today] ?: 0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 3. Update function
    private val geminiService = GeminiAnalysisService(application)
    private val fallbackAnalysisService = CbtAnalysisService.getInstance(application)
    // journalRepository는 로그인 시 UID로 재초기화하므로 var로 선언
    private var journalRepository = JournalRepository(application, "anonymous")
    private val dateSdf = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREA)

    // TFLite 모델은 lazy로 딱 한 번만 로드 (매번 새 인스턴스 생성 방지 → OOM 해결)
    private var _moodService: MoodInferenceService? = null
    private fun getMoodService(): MoodInferenceService? {
        if (_moodService == null) {
            try {
                _moodService = MoodInferenceService(getApplication())
            } catch (e: Exception) {
                Log.e("CbtViewModel", "MoodInferenceService 초기화 실패: ${e.message}")
            }
        }
        return _moodService
    }

    private val Context.dataStore by androidx.datastore.preferences.preferencesDataStore(name = "user_profile")
    // 언어 설정은 계정 공용으로 유지
    private val languageKey = stringPreferencesKey("app_language")
    // 계정별 키를 동적으로 생성하는 헬퍼 (UID 접미사로 완전 분리)
    private fun nicknameKey(uid: String) = stringPreferencesKey("nickname_$uid")
    private fun jobTitleKey(uid: String) = stringPreferencesKey("job_title_$uid")
    private fun photoUriKey(uid: String) = stringPreferencesKey("photo_uri_$uid")
    private fun moodRatingKey(uid: String) = stringPreferencesKey("today_mood_rating_$uid")

    private val _uiState = MutableStateFlow(CbtUiState())
    val uiState: StateFlow<CbtUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private val authService = FirebaseAuthService()
    private var currentUid: String? = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

    val behavioralActivities = listOf(
        BehavioralActivity(
            id = "forest",
            name = "푸른 숲길 공원 둘레길",
            distance = "0.8km",
            purpose = "나무 바람 소리와 숲 향기를 감각하며 복잡한 잡념 비워내기",
            durationText = "약 15분 소요",
            emoji = "🌲",
            instruction = "오직 초록빛 잎사귀들과 발바닥에 번갈아 닿는 흙길의 감촉에 현존의 주의를 집중하세요."
        ),
        BehavioralActivity(
            id = "sunlight",
            name = "햇살 충전 광합성 산책로",
            distance = "1.2km",
            purpose = "밝은 세로토닌 합성을 도우며 정서적 무기력과 피로감을 지워내기",
            durationText = "약 20분 소요",
            emoji = "☀️",
            instruction = "피부 끝으로 안착되는 햇볕의 훈훈한 온도를 느끼고, 어깨를 넓게 펴 일정한 템포로 걸으세요."
        ),
        BehavioralActivity(
            id = "stream",
            name = "잔잔한 옥빛 개울가 하천길",
            distance = "1.5km",
            purpose = "졸졸 흐르는 맑은 백색소음을 빌어 마음속 오래 짓눌렸던 우울을 씻어내기",
            durationText = "약 25분 소요",
            emoji = "🌊",
            instruction = "규칙적으로 흐르는 맑은 시냇물의 자연 소리와 리듬에 내 들숨, 날숨의 싱크를 편히 맞아들이세요."
        )
    )

    init {
        val context = getApplication<Application>()
        _uiState.update {
            it.copy(
                savedJournals = journalRepository.loadJournals(),
                behavioralActivities = behavioralActivities
            )
        }
        // 앱 시작 시: 모델이 이미 다운로드되어 있으면 바로 스플래시로, 없으면 다운로드 화면으로 (임시 우회)
        _uiState.update { it.copy(step = CbtStep.Splash) }

        viewModelScope.launch {
            try {
                val preferences = context.dataStore.data.first()
                val savedLang = preferences[languageKey] ?: "ko"
                _uiState.update { it.copy(currentLanguage = savedLang) }
                
                val localeList = androidx.core.os.LocaleListCompat.forLanguageTags(savedLang)
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(localeList)
            } catch (e: Exception) {
                Log.e("CbtViewModel", "Failed to restore language: ${e.message}")
            }
        }
    }

    // ──────────────────────────────────────────────
    // 📦 ML 모델 다운로드 관련
    // ──────────────────────────────────────────────

    /**
     * Firebase Storage에서 모델 다운로드를 시작한다.
     * ModelDownloadScreen에서 호출됨.
     */
    fun startModelDownload() {
        val context = getApplication<Application>()
        _uiState.update {
            it.copy(
                downloadProgress   = 0f,
                downloadStatusText = "AI 모델 다운로드 중...",
                downloadError      = null
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            com.example.termproject.service.ModelDownloadService.downloadModels(
                context = context,
                onProgress = { progress ->
                    val pct = (progress * 100).toInt()
                    _uiState.update {
                        it.copy(
                            downloadProgress   = progress,
                            downloadStatusText = "다운로드 중... $pct%"
                        )
                    }
                },
                onComplete = {
                    _uiState.update {
                        it.copy(
                            downloadProgress   = 1f,
                            downloadStatusText = "완료! 앱을 시작합니다...",
                            step               = CbtStep.Splash
                        )
                    }
                },
                onError = { errorMsg ->
                    Log.e("ModelDownload", "Error: $errorMsg")
                    _uiState.update {
                        it.copy(
                            downloadError      = errorMsg,
                            downloadStatusText = "다운로드 실패. 다시 시도해주세요."
                        )
                    }
                }
            )
        }
    }

    // ──────────────────────────────────────────────
    // 🔐 로그인, 회원가입, 프로필 설정 관련 이벤트
    // ──────────────────────────────────────────────


    fun updateLoginEmail(value: String) {
        _uiState.update { it.copy(loginEmail = value) }
    }

    fun updateLoginPassword(value: String) {
        _uiState.update { it.copy(loginPassword = value) }
    }

    fun updateSignUpEmail(value: String) {
        _uiState.update { it.copy(signUpEmail = value) }
    }
    fun onImageSelected(path: String?) {
        _uiState.update { it.copy(selectedImagePath = path) }
    }
    fun updateSignUpPassword(value: String) {
        _uiState.update { it.copy(signUpPassword = value) }
    }

    fun updateSetupNickname(value: String) {
        _uiState.update { it.copy(setupNickname = value) }
    }

    fun updateSetupJobTitle(value: String) {
        _uiState.update { it.copy(setupJobTitle = value) }
    }

    // Inside CbtViewModel.kt
    // In your ViewModel
    fun updateSetupPhotoUri(path: String) {
        // Save this path to your state (setupPhotoUri로 올바르게 저장)
        _uiState.update { it.copy(setupPhotoUri = path) }
    }
    fun detectMood() {
        val text = _uiState.value.thoughtText

        if (text.isBlank()) return

        // 1. Reset the UI to "Analyzing..." or empty
        _uiState.update { it.copy(detectedMood = "Analyzing...") }

        viewModelScope.launch(Dispatchers.Default) {
            try {
                // 싱글톤 서비스 재사용 (매번 새 인스턴스 생성 → OOM 방지)
                val service = getMoodService()
                    ?: throw IllegalStateException("모델 파일이 아직 다운로드되지 않았습니다.")
                val result = service.predictMood(text)

                // 2. Add Logging to verify the AI actually returned a new result
                Log.d("MoodCheck", "New text: '$text', Predicted: '$result'")

                // 3. Update UI
                _uiState.update { it.copy(detectedMood = result) }
            } catch (e: Exception) {
                Log.e("MoodError", "Error: ${e.message}")
                _uiState.update { it.copy(detectedMood = "Error: Could not analyze") }
            }
        }
    }
    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun signIn() {
        val state = _uiState.value

        if (state.loginEmail.isBlank() || state.loginPassword.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "email_pass_required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(snackbarMessage = "checking_profile") }

            authService.login(state.loginEmail, state.loginPassword) { success, error ->
                if (success) {
                    viewModelScope.launch {
                        // 로그인 성공 시 UID로 계정별 저장소 초기화
                        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                            ?: state.loginEmail
                        currentUid = uid
                        Log.d("ACCOUNT_DEBUG", "signIn success, resolved uid: $uid")
                        initUserRepositories(uid)
                        loadUserProfileData(uid)
                        _uiState.update {
                            it.copy(
                                isLoggedIn = true,
                                savedEmail = state.loginEmail, // 이메일 저장
                                step = CbtStep.Entry,
                                snackbarMessage = "welcome_back"
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(snackbarMessage = error ?: "login_invalid")
                    }
                }
            }
        }
    }
    // In CbtViewModel.kt

    fun signUp() {
        val state = _uiState.value

        if (state.signUpEmail.isBlank() || state.signUpPassword.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "email_pass_required") }
            return
        }

        if (state.signUpPassword.length < 8) {
            _uiState.update { it.copy(snackbarMessage = "pass_too_short") }
            return
        }

        authService.signup(state.signUpEmail, state.signUpPassword) { success, error ->
            if (success) {
                // 회원가입 성공 시 UID로 계정별 저장소 초기화
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    ?: state.signUpEmail
                currentUid = uid
                initUserRepositories(uid)
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        step = CbtStep.ProfileSetup,
                        snackbarMessage = "signup_complete"
                    )
                }
            } else {
                _uiState.update { it.copy(snackbarMessage = error ?: "Signup failed") }
            }
        }
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.setupNickname.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "nickname_required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(snackbarMessage = "saving_profile") }

            // 현재 로그인된 UID 가져오기
            val uid = currentUid ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                ?: return@launch
            Log.d("ACCOUNT_DEBUG", "saveProfile called for uid: $uid, nickname: ${state.setupNickname}")

            // content:// URI를 앱 내부 저장소에 복사하여 재빌드 후에도 유지되도록 함
            val persistedPhotoPath = withContext(Dispatchers.IO) {
                val rawUri = state.setupPhotoUri
                if (rawUri.isNotBlank() && rawUri.startsWith("content://")) {
                    try {
                        val uri = android.net.Uri.parse(rawUri)
                        val savedFile = ImageUtils.saveImageToInternalStorage(
                            getApplication<Application>(),
                            uri
                        )
                        savedFile?.absolutePath ?: rawUri
                    } catch (e: Exception) {
                        Log.e("CbtViewModel", "프로필 사진 내부 저장 실패: ${e.message}")
                        rawUri
                    }
                } else {
                    rawUri // 이미 절대 경로이거나 비어있는 경우 그대로 사용
                }
            }

            // UID 기반 계정별 키로 저장
            getApplication<Application>().dataStore.edit { preferences ->
                preferences[nicknameKey(uid)] = state.setupNickname
                preferences[jobTitleKey(uid)] = state.setupJobTitle
                preferences[photoUriKey(uid)] = persistedPhotoPath
            }

            delay(1000)
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    setupNickname = state.setupNickname,
                    setupJobTitle = state.setupJobTitle,
                    setupPhotoUri = persistedPhotoPath,
                    step = CbtStep.Entry,
                    snackbarMessage = "profile_saved"
                )
            }
        }
    }

    fun signOut() {
        authService.logout()
        currentUid = null
        // 로그아웃 시 계정 데이터 완전 초기화 (다음 계정 로그인 전 오염 방지)
        journalRepository = JournalRepository(getApplication(), "anonymous")
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                loginEmail = "",
                loginPassword = "",
                signUpEmail = "",
                signUpPassword = "",
                // 프로필 초기화
                setupNickname = "",
                setupJobTitle = "",
                setupPhotoUri = "",
                savedEmail = "",
                // 저널 초기화
                savedJournals = emptyList(),
                step = CbtStep.Login,
                snackbarMessage = "logged_out",
                // 잔류 상태 데이터 완전 초기화
                thoughtText = "",
                analysisResult = null,
                selectedOption = null,
                detectedMood = null,
                selectedImagePath = null,
                exposureTags = emptyList(),
                exposureAiAnalysis = "",
                selectedActivity = null,
                timerSeconds = 0,
                walkDistanceMeters = 0,
                selectedMood = null,
                reflectionText = "",
                trackedPath = emptyList(),
                currentLocation = null,
                destinationLocation = null,
                navigationPath = emptyList()
            )
        }
        _dailyMoodRatings.value = emptyMap()
    }

    // ──────────────────────────────────────────────
    // 생각 입력 및 CBT 분석 엔진 구동 (100% Dynamic Pipeline Validation)
    // ──────────────────────────────────────────────

    fun updateThought(text: String) {
        _uiState.update { it.copy(thoughtText = text.take(500)) }
    }

    fun selectOption(option: String) {
        _uiState.update { it.copy(selectedOption = option) }
    }

    fun useTemplate(text: String) {
        _uiState.update {
            it.copy(
                thoughtText = text,
                snackbarMessage = "example_thought_inserted"
            )
        }
    }


    fun analyzeThought() {
        viewModelScope.launch {
            _uiState.update { it.copy(step = CbtStep.Analysing) }

            val userThought = _uiState.value.thoughtText.trim()
            val userLang = _uiState.value.currentLanguage

            try {
                // Step 1: 로컬 CbtAnalysisService로 왜곡 분석만 수행 (API 호출 없음)
                // Gemini API는 CategorySelect에서 cognitive 선택 시에만 호출됨
                val localResult = fallbackAnalysisService.analyze(userThought, userLang)

                _uiState.update {
                    it.copy(
                        analysisResult = localResult,
                        step = CbtStep.DistortionReview
                    )
                }
            } catch (e: Exception) {
                Log.e("CbtViewModel", "로컬 분석 오류: ${e.message}")
                _uiState.update {
                    it.copy(
                        step = CbtStep.Entry,
                        snackbarMessage = "분석 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    fun updateTodayMoodRating(rating: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val uid = currentUid ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            ?: return // 로그인 상태가 아니면 저장하지 않음

        viewModelScope.launch {
            // UID 기반 계정별 키로 DataStore에 저장
            getApplication<Application>().dataStore.edit { preferences ->
                preferences[moodRatingKey(uid)] = "$today:$rating"
            }

            // Update the UI state
            _dailyMoodRatings.update { currentMap ->
                currentMap.toMutableMap().apply { put(today, rating) }
            }
        }
    }

    fun choosePathType(type: String) {
        _uiState.update { it.copy(pathType = type) }
        when (type) {
            "cognitive" -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(step = CbtStep.SocraticAnalysing) }
                    val userThought = _uiState.value.thoughtText.trim()
                    val userLang = _uiState.value.currentLanguage
                    try {
                        val apiResult = geminiService.analyze(userThought, userLang)
                        _uiState.update {
                            it.copy(
                                analysisResult = apiResult,
                                step = CbtStep.SocraticCheck
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("CbtViewModel", "Gemini API 분석 오류, 로컬 데이터 유지: ${e.message}")
                        val localResult = fallbackAnalysisService.analyze(userThought, userLang)
                        _uiState.update {
                            it.copy(
                                analysisResult = localResult,
                                step = CbtStep.SocraticCheck,
                                snackbarMessage = "api_error"
                            )
                        }
                    }
                }
            }
            "exposure" -> {
                _uiState.update { it.copy(
                    exposureTags = emptyList(),
                    exposureAiAnalysis = "",
                    isAnalyzingImage = false,
                    step = CbtStep.ExposureActive
                ) }
            }
            "behavioral" -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(step = CbtStep.BehavioralAnalysing) }
                    val userThought = _uiState.value.thoughtText.trim()
                    val userLang = _uiState.value.currentLanguage
                    val currentLoc = _uiState.value.currentLocation ?: LatLng(35.1796, 129.0756)
                    try {
                        val recommended = geminiService.recommendBehavioralActivities(
                            thought = userThought,
                            latitude = currentLoc.latitude,
                            longitude = currentLoc.longitude,
                            lang = userLang
                        )
                        if (recommended.isNotEmpty()) {
                            _uiState.update {
                                it.copy(
                                    behavioralActivities = recommended,
                                    step = CbtStep.BehaviorRecommend
                                )
                            }
                        } else {
                            val localRec = fallbackAnalysisService.recommendBehavioralActivities(
                                thought = userThought,
                                latitude = currentLoc.latitude,
                                longitude = currentLoc.longitude,
                                lang = userLang
                            )
                            _uiState.update {
                                it.copy(
                                    behavioralActivities = localRec,
                                    step = CbtStep.BehaviorRecommend,
                                    snackbarMessage = "api_error"
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CbtViewModel", "Gemini Behavioral 추천 오류: ${e.message}")
                        val localRec = fallbackAnalysisService.recommendBehavioralActivities(
                            thought = userThought,
                            latitude = currentLoc.latitude,
                            longitude = currentLoc.longitude,
                            lang = userLang
                        )
                        _uiState.update {
                            it.copy(
                                behavioralActivities = localRec,
                                step = CbtStep.BehaviorRecommend,
                                snackbarMessage = "api_error"
                            )
                        }
                    }
                }
            }
            else -> navigateTo(CbtStep.CategorySelect)
        }
    }

    private fun generateSocraticReflection() {
        navigateTo(CbtStep.SocraticCheck)
    }

    // ──────────────────────────────────────────────
    // 구글 맵 및 GPS 서비스 위치 추적 로직
    // ──────────────────────────────────────────────

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation ?: return
            val newLatLng = LatLng(location.latitude, location.longitude)

            _uiState.update { state ->
                val updatedPath = state.trackedPath + newLatLng
                var addedDistance = 0
                if (state.trackedPath.isNotEmpty() && !state.timerPaused) {
                    val lastLatLng = state.trackedPath.last()
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        lastLatLng.latitude, lastLatLng.longitude,
                        newLatLng.latitude, newLatLng.longitude,
                        results
                    )
                    addedDistance = results[0].toInt()
                }

                val dest = state.destinationLocation ?: state.selectedActivity?.let { act ->
                    LatLng(act.latitude, act.longitude)
                }

                if (dest != null && state.navigationPath.isEmpty()) {
                    triggerDirectionsFetch(newLatLng, dest)
                }

                state.copy(
                    currentLocation = newLatLng,
                    trackedPath = updatedPath,
                    destinationLocation = dest,
                    walkDistanceMeters = state.walkDistanceMeters + if (!state.timerPaused) addedDistance else 0
                )
            }
        }
    }

    private fun triggerDirectionsFetch(origin: LatLng, dest: LatLng) {
        viewModelScope.launch {
            val apiKey = getMapsApiKey()
            if (apiKey.isNotBlank()) {
                val path = fetchDirections(origin, dest, apiKey)
                _uiState.update { it.copy(navigationPath = path) }
            } else {
                _uiState.update { it.copy(navigationPath = listOf(origin, dest)) }
            }
        }
    }

    private fun getMapsApiKey(): String {
        return try {
            val appInfo = getApplication<Application>().packageManager.getApplicationInfo(
                getApplication<Application>().packageName,
                android.content.pm.PackageManager.GET_META_DATA
            )
            appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result ushr 1).inv() else result ushr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result ushr 1).inv() else result ushr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }
    // Add this helper function to your CbtViewModel


    private suspend fun fetchDirections(
        origin: LatLng,
        dest: LatLng,
        apiKey: String
    ): List<LatLng> = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${dest.latitude},${dest.longitude}" +
                    "&mode=walking" +
                    "&key=$apiKey"
            val response = java.net.URL(urlString).readText()
            val jsonObject = com.google.gson.JsonParser.parseString(response).asJsonObject
            val status = jsonObject.get("status").asString
            if (status == "OK") {
                val routes = jsonObject.getAsJsonArray("routes")
                if (routes.size() > 0) {
                    val route = routes.get(0).asJsonObject
                    val overviewPolyline = route.getAsJsonObject("overview_polyline")
                    val points = overviewPolyline.get("points").asString
                    return@withContext decodePolyline(points)
                }
            }
        } catch (e: Exception) {
            Log.e("CbtViewModel", "Failed to fetch directions", e)
        }
        listOf(origin, dest)
    }

    private fun getDestinationLatLng(start: LatLng, distanceMeters: Int): LatLng {
        val latDiff = (distanceMeters * 0.707) / 111000.0
        val lngDiff =
            (distanceMeters * 0.707) / (111000.0 * Math.cos(Math.toRadians(start.latitude)))
        return LatLng(start.latitude + latDiff, start.longitude + lngDiff)
    }

    @SuppressWarnings("MissingPermission")
    fun startLocationUpdates() {
        try {
            _uiState.update {
                it.copy(
                    trackedPath = emptyList(),
                    currentLocation = null,
                    destinationLocation = null,
                    navigationPath = emptyList(),
                    walkDistanceMeters = 0
                )
            }

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L).apply {
                setMinUpdateIntervalMillis(1500L)
                setMinUpdateDistanceMeters(1f)
            }.build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                android.os.Looper.getMainLooper()
            )

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)

                    // 1. Trigger the background fetch for weather (Non-blocking)
                    updateWeather(latLng.latitude, latLng.longitude)

                    // 2. Perform UI state update
                    _uiState.update { state ->
                        val dest = state.selectedActivity?.let { act ->
                            LatLng(act.latitude, act.longitude)
                        }

                        // Fetch directions if we have a destination
                        if (dest != null) {
                            triggerDirectionsFetch(latLng, dest)
                        }

                        state.copy(
                            currentLocation = latLng,
                            trackedPath = listOf(latLng),
                            destinationLocation = dest
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("CbtViewModel", "Missing location permission for updates", e)
        }
    }
    // Inside CbtViewModel.kt

    fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e("CbtViewModel", "Error removing location updates", e)
        }
    }

    // ──────────────────────────────────────────────
    // 행동 활성화 치료 미션 및 시간 관리
    // ──────────────────────────────────────────────
// In CbtViewModel.kt
    fun fetchAppUsageStats(context: Context) {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (7 * 24 * 60 * 60 * 1000)

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        // Filter and map to a list of 7 integers (minutes for the last 7 days)
        val usageList = (6 downTo 0).map { daysAgo ->
            val targetDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            }

            stats.filter {
                it.packageName == context.packageName &&
                        isSameDay(it.firstTimeStamp, targetDate.timeInMillis)
            }.sumOf { it.totalTimeInForeground / 1000 / 60 }.toInt()
        }

        _uiState.update { it.copy(weeklyUsageMinutes = usageList) }
    }

    // Helper to match usage stats dates to your calendar days
    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // In CbtViewModel.kt
    fun selectBehavioralActivity(activity: BehavioralActivity) {
        val destination = LatLng(activity.latitude, activity.longitude)

        _uiState.update {
            it.copy(
                selectedActivity = activity,
                destinationLocation = destination
            )
        }
    }

    fun pauseTimer() {
        _uiState.update { it.copy(timerPaused = true) }
    }

    fun toggleTimer() {
        val currentlyPaused =
            _uiState.value.timerPaused; _uiState.update { it.copy(timerPaused = !currentlyPaused) }
    }

    fun selectPostMood(mood: String) {
        _uiState.update { it.copy(selectedMood = mood) }
    }

    fun updateReflectionText(text: String) {
        _uiState.update { it.copy(reflectionText = text) }
    }

    fun startTimer() {
        _uiState.update { it.copy(timerPaused = false) }
        startLocationUpdates()
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.timerPaused) {
                    _uiState.update { state -> state.copy(timerSeconds = state.timerSeconds + 1) }
                }
            }
        }
    }

    fun stopTimerAndGoToReflection() {
        timerJob?.cancel()
        stopLocationUpdates()
        _uiState.update {
            it.copy(
                step = CbtStep.ReflectionComplete,
                // The timerSeconds and walkDistanceMeters are already in the state
            )
        }
    }

    // In CbtViewModel.kt
    fun refreshNearbyActivities() {
        viewModelScope.launch {
            _uiState.update { it.copy(snackbarMessage = "nearby_refreshing") }
            val userThought = _uiState.value.thoughtText.trim()
            val userLang = _uiState.value.currentLanguage
            val currentLoc = _uiState.value.currentLocation ?: LatLng(35.1796, 129.0756)
            try {
                val recommended = geminiService.recommendBehavioralActivities(
                    thought = userThought,
                    latitude = currentLoc.latitude,
                    longitude = currentLoc.longitude,
                    lang = userLang
                )
                if (recommended.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            behavioralActivities = recommended,
                            snackbarMessage = "nearby_updated"
                        )
                    }
                } else {
                    val localRec = fallbackAnalysisService.recommendBehavioralActivities(
                        thought = userThought,
                        latitude = currentLoc.latitude,
                        longitude = currentLoc.longitude,
                        lang = userLang
                    )
                    _uiState.update {
                        it.copy(
                            behavioralActivities = localRec,
                            snackbarMessage = "api_error"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("CbtViewModel", "Gemini Behavioral 새로고침 오류: ${e.message}")
                val localRec = fallbackAnalysisService.recommendBehavioralActivities(
                    thought = userThought,
                    latitude = currentLoc.latitude,
                    longitude = currentLoc.longitude,
                    lang = userLang
                )
                _uiState.update {
                    it.copy(
                        behavioralActivities = localRec,
                        snackbarMessage = "api_error"
                    )
                }
            }
        }
    }

    // ──────────────────────────────────────────────
    // 영속 저널 일지 데이터 구조 빌더 정리
    // ──────────────────────────────────────────────
// You need to call this when the user selects an activity
    fun fetchNearbyDestination(activityId: String, currentLatLng: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            val keyword = when (activityId) {
                "beach" -> "beach"
                "forest" -> "park"
                else -> "park"
            }

            // Construct the Google Places Nearby Search URL
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=${currentLatLng.latitude},${currentLatLng.longitude}" +
                    "&radius=5000" + // 5km radius
                    "&type=$keyword" +
                    "&key=${getMapsApiKey()}"

            try {
                val response = java.net.URL(url).readText()
                val json = com.google.gson.JsonParser.parseString(response).asJsonObject
                val results = json.getAsJsonArray("results")

                if (results.size() > 0) {
                    // Pick the first result (the closest one)
                    val location = results.get(0).asJsonObject
                        .getAsJsonObject("geometry")
                        .getAsJsonObject("location")

                    val dest = LatLng(location.get("lat").asDouble, location.get("lng").asDouble)

                    _uiState.update { it.copy(destinationLocation = dest) }
                    // Now fetch directions to this NEW dynamic destination
                    triggerDirectionsFetch(currentLatLng, dest)
                }
            } catch (e: Exception) {
                Log.e("CbtViewModel", "Failed to find nearby place", e)
            }
        }
    }

    fun saveJournal() {
        val state = _uiState.value
        val result = state.analysisResult ?: return

        val entry = SavedJournal(
            date = dateSdf.format(Date()),
            originalThought = state.thoughtText,
            cognitiveDistortions = result.cognitiveDistortions,
            socraticQuestion = result.socraticQuestion,
            selectedOption = state.selectedOption ?: "",
            alternativeThoughts = result.alternativeThoughts,
            pathType = "cognitive"
        )

        val updated = journalRepository.addJournal(entry, state.savedJournals)
        _uiState.update {
            it.copy(
                savedJournals = updated,
                step = CbtStep.Entry,
                thoughtText = "",
                analysisResult = null,
                selectedOption = null,
                snackbarMessage = "cognitive_journal_saved"
            )
        }
    }

    fun saveBehavioralJournal() {
        val state = _uiState.value
        val result = state.analysisResult ?: return
        val activity = state.selectedActivity ?: return

        val durationStr =
            String.format("%02d:%02d", state.timerSeconds / 60, state.timerSeconds % 60)
        val distanceStr = String.format("%.2fkm", state.walkDistanceMeters / 1000.0)

        val entry = SavedJournal(
            date = dateSdf.format(Date()),
            originalThought = state.thoughtText,
            cognitiveDistortions = result.cognitiveDistortions,
            socraticQuestion = result.socraticQuestion,
            selectedOption = "마음챙김 야외활동: ${activity.name}",
            alternativeThoughts = listOf(
                "야외 행동 활성화 치료를 직접 실천하며 신체적 현존 상태를 고양하였습니다.",
                "실천 미션: ${activity.name} ($distanceStr) 소요시간 ($durationStr)"
            ),
            pathType = "behavioral",
            behavioralInfo = BehavioralInfo(
                activityName = activity.name, duration = durationStr, distance = distanceStr,
                postMood = state.selectedMood ?: "평온함", reflectionText = state.reflectionText
            )
        )

        val updated = journalRepository.addJournal(entry, state.savedJournals)
        _uiState.update {
            it.copy(
                savedJournals = updated,
                step = CbtStep.Entry,
                thoughtText = "",
                analysisResult = null,
                selectedOption = null,
                selectedActivity = null,
                timerSeconds = 0,
                timerPaused = false,
                walkDistanceMeters = 0,
                selectedMood = null,
                reflectionText = "",
                snackbarMessage = "behavioral_journal_saved"
            )
        }
    }

    private suspend fun fetchWeather(lat: Double, lon: Double): WeatherInfo? = withContext(Dispatchers.IO) {
        Log.d("WeatherDebug", "Fetching weather for: $lat, $lon")
        try {
            val apiKey = "cab783c68df686e8111abef075d1ef38"
            val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&appid=$apiKey"

            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 5000

            val responseCode = connection.responseCode
            Log.d("WeatherDebug", "Response Code: $responseCode")

            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = com.google.gson.JsonParser.parseString(response).asJsonObject

                val temp = jsonObject.getAsJsonObject("main").get("temp").asInt
                val weatherArray = jsonObject.getAsJsonArray("weather")
                val weatherObj = weatherArray.get(0).asJsonObject
                val condition = weatherObj.get("main").asString
                val icon = weatherObj.get("icon").asString

                return@withContext WeatherInfo(
                    temperature = "$temp°C",
                    condition = condition,
                    iconCode = icon
                )
            } else {
                Log.e("WeatherDebug", "API Error Code: $responseCode")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("WeatherDebug", "Exception: ${e.localizedMessage}")
            return@withContext null
        }
    }
    // In CbtViewModel.kt
    fun getJournalStatistics(): Map<String, Any> {
        val journals = _uiState.value.savedJournals
        val total = journals.size
        // Calculate main distortion (simple count)
        val distortions = journals.flatMap { it.cognitiveDistortions }.groupingBy { it.tag }.eachCount()
        val mainDistortion = distortions.maxByOrNull { it.value }?.key ?: "None"

        return mapOf(
            "totalCount" to total,
            "mainDistortion" to mainDistortion,
            "distortionCount" to (distortions[mainDistortion] ?: 0)
        )
    }
    fun updateWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            // 1. Fetch the data first (this returns WeatherInfo?)
            val weather = fetchWeather(lat, lon)

            // 2. Only update if data was successfully retrieved
            if (weather != null) {
                _uiState.update { it.copy(currentWeather = weather) }
            }
        }
    }

    fun runExposureAiAnalysis(context: Context, imageUriStr: String) {
        if (imageUriStr.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingImage = true) }

            try {
                val uri = Uri.parse(imageUriStr)
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                val scaledBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    800,
                    (bitmap.height * (800.0 / bitmap.width)).toInt(),
                    true
                )

                // 1. Perform the API call first
                val result = geminiService.analyzeExposureImageWithTags(
                    scaledBitmap,
                    _uiState.value.thoughtText.trim(),
                    _uiState.value.currentLanguage
                )

                // 2. Check the result and THEN update state
                if (result != null) {
                    Log.d("UI_DEBUG", "Saving tags to state: ${result.first}")
                    _uiState.update {
                        it.copy(
                            exposureTags = result.first,
                            exposureAiAnalysis = result.second,
                            isAnalyzingImage = false
                        )
                    }
                } else {
                    throw Exception("Null analysis response from Gemini")
                }

            } catch (e: Exception) {
                Log.e("GEMINI_ERROR", "Error: ${e.message}")

                // Fallback UI state on error
                _uiState.update {
                    it.copy(
                        exposureTags = listOf("따뜻한 눈빛", "빛나는 존재", "소중한 가치"),
                        exposureAiAnalysis = "사진에서 온화하고 긍정적인 에너지가 은은하게 느껴집니다.",
                        isAnalyzingImage = false
                    )
                }
            }
        }
    }
    fun deleteJournal(id: String) {
        val updated = journalRepository.deleteJournal(id, _uiState.value.savedJournals)
        _uiState.update { it.copy(savedJournals = updated) }
    }

    fun openJournalDetail(journal: SavedJournal) {
        _uiState.update {
            it.copy(
                activeJournal = journal, thoughtText = journal.originalThought,
                analysisResult = CbtAnalysis(
                    cognitiveDistortions = journal.cognitiveDistortions, socraticQuestion = journal.socraticQuestion,
                    options = listOf(journal.selectedOption, "보완할 다른 대안"), alternativeThoughts = journal.alternativeThoughts
                ),
                selectedOption = journal.selectedOption, step = CbtStep.ViewJournalDetail
            )
        }
    }
// ──────────────────────────────────────────────
// 노출 치료(Exposure) 저널 저장 로직
// ──────────────────────────────────────────────

    fun saveExposureJournal(reflectionText: String, photoUri: String) {
        val state = _uiState.value

        // AI 분석 결과(tags 등)가 있다면 활용, 없으면 기본값 사용
        val compliments = if (state.exposureTags.isNotEmpty()) {
            state.exposureTags
        } else {
            listOf("반짝이는 눈망울", "따뜻한 미소", "건강한 에너지")
        }

        val entry = SavedJournal(
            date = dateSdf.format(Date()),
            originalThought = state.thoughtText,
            cognitiveDistortions = emptyList(), // 필요 시 AI 분석 결과 연동
            socraticQuestion = "나의 얼굴을 정성껏 바라보며 긍정적인 면을 찾아봅니다.",
            selectedOption = "나의 빛나는 모습 찾기: 사진 성찰",
            alternativeThoughts = listOf(
                "오늘의 내 사진에서 긍정적인 빛을 발견하고 내면을 정화하였습니다.",
                "성찰 내용: $reflectionText"
            ),
            pathType = "exposure",
            exposureInfo = ExposureInfo(
                photoUri = photoUri,
                compliments = compliments,
                reflectionText = reflectionText
            )
        )

        val updated = journalRepository.addJournal(entry, state.savedJournals)

        // 저장 후 상태 초기화 및 홈으로 이동
        _uiState.update {
            it.copy(
                savedJournals = updated,
                step = CbtStep.Entry,
                thoughtText = "",
                exposureTags = emptyList(),
                exposureAiAnalysis = "",
                snackbarMessage = "exposure_journal_saved"
            )
        }
    }
    // ──────────────────────────────────────────────
    // 🧭 네비게이션 및 다이얼로그 바인딩
    // ──────────────────────────────────────────────

    fun navigateTo(step: CbtStep) { _uiState.update { it.copy(step = step) } }

    fun checkSessionAndNavigate() {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            currentUid = uid
            val email = currentUser.email ?: ""
            Log.d("ACCOUNT_DEBUG", "checkSessionAndNavigate: Auto-login session restored for uid: $uid")
            
            // 로컬 저장소 및 프로필 정보 복구
            initUserRepositories(uid)
            loadUserProfileData(uid)
            
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    loginEmail = email,
                    savedEmail = email,
                    step = CbtStep.Entry
                )
            }
        } else {
            Log.d("ACCOUNT_DEBUG", "checkSessionAndNavigate: No active session found. Navigating to Login.")
            _uiState.update { it.copy(step = CbtStep.Login) }
        }
    }
    fun toggleInfoDialog(show: Boolean) { _uiState.update { it.copy(showInfoDialog = show) } }
    fun toggleSettingsDialog(show: Boolean) { _uiState.update { it.copy(showSettingsDialog = show) } }
    fun changeLanguage(lang: String) {
        _uiState.update { it.copy(currentLanguage = lang) }
        viewModelScope.launch(Dispatchers.Main) {
            val localeList = androidx.core.os.LocaleListCompat.forLanguageTags(lang)
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(localeList)
            try {
                val context = getApplication<Application>()
                context.dataStore.edit { preferences ->
                    preferences[languageKey] = lang
                }
            } catch (e: Exception) {
                Log.e("CbtViewModel", "Failed to save language setting: ${e.message}")
            }
        }
    }

    fun navigateBack() {
        timerJob?.cancel()
        timerJob = null
        stopLocationUpdates()
        _uiState.update {
            val nextStep = when (it.step) {
                // ✅ 수정된 워크플로우 순서
                // Entry → Analysing → DistortionReview → CategorySelect → SocraticCheck → AlternativeReframe
                CbtStep.DistortionReview   -> CbtStep.Entry              // 왜곡 리포트 → 입력 화면
                CbtStep.CategorySelect     -> CbtStep.DistortionReview   // 작업 선택 → 왜곡 리포트
                CbtStep.SocraticCheck      -> CbtStep.CategorySelect     // 소크라테스 질문 → 작업 선택
                CbtStep.AlternativeReframe -> CbtStep.SocraticCheck      // 대안 사고 → 소크라테스 질문

                CbtStep.SocraticAnalysing  -> CbtStep.CategorySelect
                CbtStep.BehaviorRecommend  -> CbtStep.CategorySelect
                CbtStep.MapNavigation      -> CbtStep.BehaviorRecommend
                CbtStep.TimerActive        -> CbtStep.MapNavigation
                CbtStep.ReflectionComplete -> CbtStep.TimerActive
                CbtStep.ExposureActive     -> CbtStep.CategorySelect
                CbtStep.ProfileSetup       -> if (it.isLoggedIn) CbtStep.Profile else CbtStep.Login
                CbtStep.ViewJournalDetail  -> CbtStep.Profile
                CbtStep.Entry              -> CbtStep.Entry
                CbtStep.Insight            -> CbtStep.Insight
                CbtStep.Profile            -> CbtStep.Profile
                else -> it.step
            }
            it.copy(step = nextStep, isLoggedIn = if (nextStep == CbtStep.Login) false else it.isLoggedIn)
        }
    }
// Inside CbtViewModel.kt

    // This calculates how many times a specific distortion appears vs total distortions
    fun getTopDistortions(): List<DistortionCount> {
        val journals = _uiState.value.savedJournals
        if (journals.isEmpty()) return emptyList()

        val allDistortions = journals.flatMap { it.cognitiveDistortions }
        val totalCount = allDistortions.size.coerceAtLeast(1)

        // Group by tag, count them, and sort by count descending
        return allDistortions
            .groupingBy { it.tag.ifBlank { it.englishTag } }
            .eachCount()
            .map { (tag, count) ->
                DistortionCount(tag, count, (count.toFloat() / totalCount.toFloat()))
            }
            .sortedByDescending { it.count }
            .take(3) // Get the top 2
    }
    fun resetToHome() {
        timerJob?.cancel()
        timerJob = null
        stopLocationUpdates()
        _uiState.update {
            it.copy(
                step = CbtStep.Entry, thoughtText = "", analysisResult = null, selectedOption = null,
                activeJournal = null, selectedActivity = null, timerSeconds = 0, timerPaused = false,
                walkDistanceMeters = 0, selectedMood = null, reflectionText = "", trackedPath = emptyList(),
                currentLocation = null, destinationLocation = null
            )
        }
    }
    // Add this to CbtViewModel.kt
    // In CbtViewModel.kt
// In CbtViewModel.kt
    fun getBestDestination(currentPos: LatLng, activityId: String): LatLng {
        // 1. If you use Places API, you would fetch real coordinates here.
        // 2. Otherwise, use your current dynamic logic based on proximity:
        return getDestinationLatLng(currentPos, when(activityId) {
            "beach" -> 2000 // meters
            else -> 1000
        })
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.update { it.copy(snackbarMessage = "checking_profile") }
        authService.loginWithGoogle(idToken) { success, error ->
            if (success) {
                viewModelScope.launch {
                    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    val uid = firebaseUser?.uid
                    val googleEmail = firebaseUser?.email ?: ""

                    if (uid != null) {
                        currentUid = uid
                        // Google 로그인 성공 시 UID로 계정별 저장소 초기화
                        initUserRepositories(uid)
                    }

                    // 구글 실제 이메일을 uiState에 저장
                    _uiState.update { it.copy(savedEmail = googleEmail) }

                    val isExistingUser = if (uid != null) checkUserExistsInDatabase(uid) else false

                    if (isExistingUser && uid != null) {
                        loadUserProfileData(uid)
                        _uiState.update {
                            it.copy(
                                isLoggedIn = true,
                                step = CbtStep.Entry,
                                snackbarMessage = "welcome_back"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoggedIn = true,
                                step = CbtStep.ProfileSetup,
                                snackbarMessage = "signup_complete"
                            )
                        }
                    }
                }
            } else {
                _uiState.update {
                    it.copy(snackbarMessage = error ?: "Google Sign-In Failed")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        stopLocationUpdates()
        // TFLite Interpreter 해제하여 메모리 반환
        try { _moodService = null } catch (_: Exception) {}
    }

    // ──────────────────────────────────────────────
    // 💾 Private Local Preferences Database Handlers
    // ──────────────────────────────────────────────

    private suspend fun checkUserExistsInDatabase(uid: String): Boolean {
        val preferences = getApplication<Application>().dataStore.data.first()
        val savedNickname = preferences[nicknameKey(uid)]
        return !savedNickname.isNullOrBlank()
    }
    private fun saveDebugBitmap(context: Context, bitmap: Bitmap) {
        try {
            val file = java.io.File(context.cacheDir, "debug_photo.jpg")
            val stream = java.io.FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            stream.flush()
            stream.close()

            Log.d("PHOTO", "Saved: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("PHOTO", "Save failed", e)
        }
    }
    /**
     * 로그인 시 UID를 기반으로 계정별 저장소를 재초기화한다.
     * journalRepository를 해당 UID의 SharedPreferences로 교체하고 저널을 로드한다.
     */
    private fun initUserRepositories(uid: String) {
        Log.d("ACCOUNT_DEBUG", "initUserRepositories called for uid: $uid")
        journalRepository = JournalRepository(getApplication(), uid)
        val journals = journalRepository.loadJournals()
        Log.d("ACCOUNT_DEBUG", "initUserRepositories: Loaded ${journals.size} journals for uid: $uid")
        _uiState.update { it.copy(savedJournals = journals) }
    }

    /**
     * UID 기반 DataStore 키로 해당 계정의 프로필 데이터를 불러온다.
     */
    private fun loadUserProfileData(uid: String) {
        Log.d("ACCOUNT_DEBUG", "loadUserProfileData called for uid: $uid")
        viewModelScope.launch {
            val preferences = getApplication<Application>().dataStore.data.first()
            val savedNickname = preferences[nicknameKey(uid)] ?: ""
            val savedJobTitle = preferences[jobTitleKey(uid)] ?: ""
            val savedPhotoUri = preferences[photoUriKey(uid)] ?: ""
            Log.d("ACCOUNT_DEBUG", "loadUserProfileData: Loaded nickname: $savedNickname, jobTitle: $savedJobTitle, photoUri: $savedPhotoUri for uid: $uid")
            val savedMood = preferences[moodRatingKey(uid)] ?: "" // Format "yyyy-MM-dd:rating"
            val parts = savedMood.split(":")
            val moodMap = if (parts.size == 2) {
                try { mapOf(parts[0] to parts[1].toIntOrNull()!!) } catch (e: Exception) { emptyMap() }
            } else {
                emptyMap()
            }
            _uiState.update {
                it.copy(
                    setupNickname = savedNickname,
                    setupJobTitle = savedJobTitle,
                    setupPhotoUri = savedPhotoUri
                )
            }
            _dailyMoodRatings.value = moodMap
        }
    }

    fun loginAsTestUser(email: String) {
        viewModelScope.launch {
            val uid = email.replace(".", "_").replace("@", "_")
            currentUid = uid
            Log.d("ACCOUNT_DEBUG", "loginAsTestUser: resolved uid: $uid")
            
            val dummyRepo = JournalRepository(getApplication(), uid)
            val currentJournals = dummyRepo.loadJournals()
            if (currentJournals.isEmpty()) {
                val dummyJournal = SavedJournal(
                    date = dateSdf.format(Date()),
                    originalThought = "이것은 $email 계정의 테스트 저널입니다.",
                    cognitiveDistortions = emptyList(),
                    socraticQuestion = "테스트 질문",
                    selectedOption = "테스트 선택",
                    alternativeThoughts = listOf("대안 생각"),
                    pathType = "cognitive"
                )
                dummyRepo.saveJournals(listOf(dummyJournal))
                Log.d("ACCOUNT_DEBUG", "loginAsTestUser: Seeded dummy journal for $email")
            }

            // DataStore 프로필 Seed (기존 프로필 데이터가 없을 때만 Seed 주입)
            val context = getApplication<Application>()
            val preferences = context.dataStore.data.first()
            if (preferences[nicknameKey(uid)].isNullOrBlank()) {
                context.dataStore.edit { prefs ->
                    prefs[nicknameKey(uid)] = "닉네임_$email"
                    prefs[jobTitleKey(uid)] = "직책_$email"
                    prefs[photoUriKey(uid)] = ""
                }
                Log.d("ACCOUNT_DEBUG", "loginAsTestUser: Seeded dummy profile for $email")
            } else {
                Log.d("ACCOUNT_DEBUG", "loginAsTestUser: Profile already exists, skipping seed for $email")
            }

            initUserRepositories(uid)
            loadUserProfileData(uid)
            
            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    loginEmail = email,
                    savedEmail = email,
                    step = CbtStep.Entry,
                    snackbarMessage = "welcome_back",
                    // 테스트 로그인 시 이전 잔류 상태 데이터 완전 초기화
                    thoughtText = "",
                    analysisResult = null,
                    selectedOption = null,
                    detectedMood = null,
                    selectedImagePath = null,
                    exposureTags = emptyList(),
                    exposureAiAnalysis = "",
                    selectedActivity = null,
                    timerSeconds = 0,
                    walkDistanceMeters = 0,
                    selectedMood = null,
                    reflectionText = "",
                    trackedPath = emptyList(),
                    currentLocation = null,
                    destinationLocation = null,
                    navigationPath = emptyList()
                )
            }
        }
    }
}
