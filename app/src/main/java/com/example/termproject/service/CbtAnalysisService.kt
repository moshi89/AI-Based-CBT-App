package com.example.termproject.service

import android.content.Context
import android.util.Log
import com.example.termproject.model.BehavioralActivity
import com.example.termproject.model.CbtAnalysis
import com.example.termproject.model.CognitiveDistortion

/**
 * CBT 인지 왜곡 분석 서비스.
 * 규칙 기반 로컬 엔진으로 인지왜곡을 탐지합니다.
 * ✅ OOM 방지: TFLite 모델은 MoodInferenceService에서만 로드합니다.
 *    CbtAnalysisService가 같은 556MB 모델을 중복 로드하면 1GB+ 메모리 → OOM 발생.
 */
class CbtAnalysisService private constructor(private val context: Context) {

    init {
        Log.d(TAG, "CbtAnalysisService initialized (rule-based mode, TFLite skipped to prevent OOM)")
    }

    /**
     * 분석 요청 진입점. 규칙 기반 분석을 수행한다.
     */
    fun analyze(thought: String, lang: String = "ko"): CbtAnalysis {
        return try {
            analyzeInternal(thought, lang)
        } catch (e: Exception) {
            Log.e(TAG, "Analysis failed: ${e.message}. Returning fallback.", e)
            fallbackAnalysis(lang)
        }
    }

    /**
     * 규칙 기반 인지 왜곡 분석
     */
    private fun analyzeInternal(thought: String, lang: String): CbtAnalysis {
        val lower = thought.lowercase()
        val isEn = lang.lowercase() == "en"
        return when {
            lower.contains("답장") || lower.contains("연락") ||
            lower.contains("카톡") || lower.contains("문자") ||
            lower.contains("reply") || lower.contains("text") || lower.contains("message") -> {
                if (isEn) {
                    CbtAnalysis(
                        cognitiveDistortions = listOf(
                            CognitiveDistortion("Mind Reading", "mind_reading", "Assuming you know what others are thinking without any evidence."),
                            CognitiveDistortion("Catastrophizing", "catastrophizing", "Assuming the worst possible outcome will happen.")
                        ),
                        socraticQuestion = "What are some other realistic reasons why they might not be able to reply to you right now?",
                        options = listOf("They are busy", "They keep their phone away", "Taking time to respond"),
                        alternativeThoughts = listOf("My friend is busy", "Instead of anxiety, I will wait calmly"),
                        distortionScores = mapOf(
                            "mind_reading" to 75f,
                            "catastrophizing" to 60f,
                            "all_or_nothing" to 15f,
                            "overgeneralization" to 10f
                        )
                    )
                } else {
                    CbtAnalysis(
                        cognitiveDistortions = listOf(
                            CognitiveDistortion("독심술 오류", "mind_reading", "상대방의 마음을 명확한 증거 없이 예단하는 왜곡입니다."),
                            CognitiveDistortion("파국화", "catastrophizing", "관계가 영영 끝장날 것이라며 최악의 재앙적인 시나리오를 머릿속으로 키우는 패턴입니다.")
                        ),
                        socraticQuestion = "상대가 지금 당장 당신에게 답장을 보내지 못하는 실제 상황과 다른 현실적인 방해 요소는 무엇이 있을까요?",
                        options = listOf("현재 집중해야 하는 용무 중이다", "핸드폰을 멀리 두고 생활한다", "사려 깊은 답변을 준비 중이다"),
                        alternativeThoughts = listOf("친구는 단지 바쁜 상황일 뿐이야", "조급해하지 않고 가볍게 기다리는 편이 나아"),
                        distortionScores = mapOf(
                            "mind_reading" to 75f,
                            "catastrophizing" to 60f,
                            "all_or_nothing" to 15f,
                            "overgeneralization" to 10f
                        )
                    )
                }
            }
            lower.contains("면접") || lower.contains("시험") ||
            lower.contains("실패") || lower.contains("불합격") || lower.contains("망") ||
            lower.contains("fail") || lower.contains("interview") || lower.contains("exam") || lower.contains("reject") -> {
                if (isEn) {
                    CbtAnalysis(
                        cognitiveDistortions = listOf(
                            CognitiveDistortion("Catastrophizing", "catastrophizing", "Viewing a single setback as a total disaster."),
                            CognitiveDistortion("Overgeneralization", "overgeneralization", "Drawing broad conclusion on a single negative event.")
                        ),
                        socraticQuestion = "Does this single rejection really define your core worth?",
                        options = listOf("Not a fit", "Growth opportunity", "Successful people also get rejected"),
                        alternativeThoughts = listOf("One result, doesn't limit me", "Learn and prepare next"),
                        distortionScores = mapOf(
                            "catastrophizing" to 80f,
                            "overgeneralization" to 70f,
                            "mental_filter" to 15f,
                            "all_or_nothing" to 10f
                        )
                    )
                } else {
                    CbtAnalysis(
                        cognitiveDistortions = listOf(
                            CognitiveDistortion("파국화", "catastrophizing", "일회성 좌절이나 실수 하나를 두고 인생 전체의 종말로 인식하는 왜곡입니다."),
                            CognitiveDistortion("과도한 일반화", "overgeneralization", "한정된 사건을 근거로 '난 매번 실패할 거야'라고 덮어버리는 오류입니다.")
                        ),
                        socraticQuestion = "단 한 번의 불합격이 당신이라는 인격의 능력 전부를 평가절하할 만큼 절대적인 객관 근거일까요?",
                        options = listOf("기관과 내가 맞지 않았던 결과다", "성장에 보탬이 되는 중간 평가이다", "성공한 거장들도 수십 번 겪은 일이다"),
                        alternativeThoughts = listOf("불합격은 단지 한 번의 결과일 뿐이야", "부족함을 채우고 다음을 준비하자"),
                        distortionScores = mapOf(
                            "catastrophizing" to 80f,
                            "overgeneralization" to 70f,
                            "mental_filter" to 15f,
                            "all_or_nothing" to 10f
                        )
                    )
                }
            }
            lower.contains("못해") || lower.contains("항상") || lower.contains("절대") ||
            lower.contains("always") || lower.contains("never") || lower.contains("can't") -> {
                if (isEn) {
                    CbtAnalysis(
                        cognitiveDistortions = listOf(
                            CognitiveDistortion("All-or-Nothing Thinking", "all_or_nothing", "Seeing things in black-and-white categories, ignoring any middle ground."),
                            CognitiveDistortion("Overgeneralization", "overgeneralization", "Drawing a broad conclusion about your life based on a single negative event.")
                        ),
                        socraticQuestion = "Can your current feeling of anxiety be used as objective, physical evidence in court?",
                        options = listOf(
                            "Feelings are temporary states that change, not objective absolute facts.",
                            "I might be over-interpreting the situation based on my passing mood.",
                            "I could just be tired or physically drained, causing temporary negativity."
                        ),
                        alternativeThoughts = listOf(
                            "My anxious feelings are just warning signals; they do not define reality or control my life.",
                            "This wave of emotion will pass. It is a cloud that will drift away with deep breaths."
                        ),
                        distortionScores = mapOf(
                            "all_or_nothing" to 80f,
                            "overgeneralization" to 65f,
                            "mental_filter" to 20f,
                            "should_statements" to 15f
                        )
                    )
                } else {
                    CbtAnalysis(
                        cognitiveDistortions = listOf(
                            CognitiveDistortion("이분법적 사고", "all_or_nothing", "세상을 오직 100점자리 성공과 0점짜리 파탄으로 두 극단으로만 나누는 성향입니다."),
                            CognitiveDistortion("과도한 일반화", "overgeneralization", "하나의 한정된 사건을 근거로 삶 전반을 부정적으로 덮어버리는 오류입니다.")
                        ),
                        socraticQuestion = "지금 느끼는 당신의 이 압도적인 불안 감정이, 차분한 제3의 배심원 눈앞에 엄밀한 실체적 증거로서 채택될 수 있을까요?",
                        options = listOf(
                            "감정은 날씨나 바람과 같이 기분 따라 가변적으로 소실되는 현상일 뿐, 절대적인 팩트가 아니다.",
                            "스쳐 가는 나쁜 기분에 일일이 과몰입하여 현실인 양 극단적으로 해석을 보탠 부분이 있었다.",
                            "단지 몸이 피곤하거나 뇌의 도파민 지수가 낮아져 부정 가중치가 일시적으로 붙었을 수 있다."
                        ),
                        alternativeThoughts = listOf(
                            "불안한 내 피어 오른 안개 같은 감정은 내게 일시적으로 경고를 보낼 뿐이며, 나의 온전한 삶의 흐름을 통제하지 못한다.",
                            "이 감정의 해일 또한 영원하지 않다. 심호흡과 함께 그대로 머무르다 보면 언젠가 서서히 흩어질 구름이다."
                        ),
                        distortionScores = mapOf(
                            "all_or_nothing" to 80f,
                            "overgeneralization" to 65f,
                            "mental_filter" to 20f,
                            "should_statements" to 15f
                        )
                    )
                }
            }
            else -> fallbackAnalysis(lang)
        }
    }

    /**
     * 예외 또는 미분류 입력에 대한 안전 폴백 분석 결과.
     */
    private fun fallbackAnalysis(lang: String): CbtAnalysis {
        return if (lang.lowercase() == "en") {
            CbtAnalysis(
                cognitiveDistortions = listOf(
                    CognitiveDistortion("Emotional Reasoning", "emotional_reasoning", "Believing that because you feel a certain way, it must be the objective truth."),
                    CognitiveDistortion("All-or-Nothing Thinking", "all_or_nothing", "Seeing things in black-and-white categories, ignoring any middle ground.")
                ),
                socraticQuestion = "Can your current feeling of anxiety be used as objective, physical evidence in court?",
                options = listOf("Feelings are temporary", "Over-interpreting", "Tired or drained"),
                alternativeThoughts = listOf("My feelings are just warning signals", "This wave of emotion will pass"),
                distortionScores = mapOf(
                    "emotional_reasoning" to 75f,
                    "all_or_nothing" to 60f,
                    "mental_filter" to 15f,
                    "overgeneralization" to 10f
                )
            )
        } else {
            CbtAnalysis(
                cognitiveDistortions = listOf(
                    CognitiveDistortion("감정적 추론", "emotional_reasoning", "객관적 증거 없이 단순히 '내가 느끼기에 최악이니 상황도 최악이다'라고 믿는 왜곡입니다."),
                    CognitiveDistortion("이분법적 사고", "all_or_nothing", "세상을 오직 100점과 0점의 양극단으로만 판단하는 오류입니다.")
                ),
                socraticQuestion = "지금 느끼는 당신의 이 불안 감정이, 차분한 제3의 배심원 눈앞에 엄밀한 실체적 증거로서 채택될 수 있을까요?",
                options = listOf("감정은 우주적 팩트가 아니다", "기분에 따라 과몰입하여 해석을 보탰다", "몸이 피로해서 부정적 기분이 들 수 있다"),
                alternativeThoughts = listOf("불안은 일시적 경고일 뿐 날 지배하지 못한다", "심호흡과 함께 그대로 머무르면 흩어질 안개다"),
                distortionScores = mapOf(
                    "emotional_reasoning" to 75f,
                    "all_or_nothing" to 60f,
                    "mental_filter" to 15f,
                    "overgeneralization" to 10f
                )
            )
        }
    }

    /**
     * 위치기반 행동 활성화 장소 추천 로직
     */
    fun recommendBehavioralActivities(
        thought: String,
        latitude: Double,
        longitude: Double,
        lang: String = "ko"
    ): List<BehavioralActivity> {
        val isEn = lang.lowercase() == "en"

        val gLat = 35.2638; val gLng = 129.0402
        val gDist = calculateDistance(latitude, longitude, gLat, gLng)
        val gActivity = BehavioralActivity(
            id = "fallback_geumjeongsan",
            name = if (isEn) "Geumjeongsanseong Fortress Trail" else "금정산성 둘레길",
            distance = "${String.format(java.util.Locale.US, "%.1f", gDist)}km",
            purpose = if (isEn) "Walk along the fortress wall to get a wider perspective and escape negative thoughts."
                      else "성곽 길을 걸으며 넓은 시야를 확보하고 부정적 생각에서 벗어나기",
            durationText = if (isEn) "Approx. 45 mins" else "약 45분 소요",
            emoji = "⛰️",
            instruction = if (isEn) "Walk along the fortress trail, feel the breeze, and focus your senses on the surrounding scenery."
                          else "성곽 길을 따라 걸으며 불어오는 바람을 느끼고 주변 풍경에 오감을 집중해 보세요.",
            latitude = gLat, longitude = gLng
        )

        val oLat = 35.1979; val oLng = 129.0911
        val oDist = calculateDistance(latitude, longitude, oLat, oLng)
        val oActivity = BehavioralActivity(
            id = "fallback_oncheoncheon",
            name = if (isEn) "Oncheoncheon Stream Walkway" else "온천천 산책로",
            distance = "${String.format(java.util.Locale.US, "%.1f", oDist)}km",
            purpose = if (isEn) "Listen to the sound of flowing water to focus on the present moment and calm your mind."
                      else "물소리를 들으며 현재 순간에 집중하고 조급한 마음 가라앉히기",
            durationText = if (isEn) "Approx. 30 mins" else "약 30분 소요",
            emoji = "🌊",
            instruction = if (isEn) "Walk slowly in time with the sound of the flowing stream and your footsteps, focusing on your breath."
                          else "개울물이 흐르는 소리와 발소리에 맞춰 천천히 걸으며 호흡에 집중해 보세요.",
            latitude = oLat, longitude = oLng
        )

        val kwLat = 35.1532; val kwLng = 129.1189
        val kwDist = calculateDistance(latitude, longitude, kwLat, kwLng)
        val kwActivity = BehavioralActivity(
            id = "fallback_gwangalli",
            name = if (isEn) "Gwangalli Beach Walk" else "광안리 해안 산책로",
            distance = "${String.format(java.util.Locale.US, "%.1f", kwDist)}km",
            purpose = if (isEn) "Look at the sea and listen to the waves to release suppressed emotions."
                      else "파도 소리와 바다를 바라보며 억눌린 감정 털어내기",
            durationText = if (isEn) "Approx. 25 mins" else "약 25분 소요",
            emoji = "🏖️",
            instruction = if (isEn) "Step on the sand and let go of negative thoughts to the rhythm of the waves coming and going."
                          else "모래사장을 밟으며 파도가 밀려왔다 쓸려가는 리듬에 맞춰 부정적인 생각을 흘려보내 보세요.",
            latitude = kwLat, longitude = kwLng
        )

        return listOf(gActivity, oActivity, kwActivity)
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0)
        val c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a))
        return r * c
    }

    companion object {
        private const val TAG = "CbtAnalysisService"

        @Volatile
        private var instance: CbtAnalysisService? = null

        fun getInstance(context: Context): CbtAnalysisService {
            return instance ?: synchronized(this) {
                instance ?: CbtAnalysisService(context.applicationContext).also { instance = it }
            }
        }
    }
}
