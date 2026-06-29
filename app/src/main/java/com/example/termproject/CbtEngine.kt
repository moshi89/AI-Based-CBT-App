package com.example.termproject

import com.example.termproject.model.CbtAnalysis
import com.example.termproject.model.CognitiveDistortion

/**
 * ⚠️ DEPRECATED — 직접 호출하지 마세요.
 *
 * 이 파일은 CbtAnalysisService와 동일한 규칙 기반 엔진의 중복 구현체입니다.
 * 현재 분석 흐름은 GeminiAnalysisService → (실패 시) CbtAnalysisService 폴백 구조로 동작합니다.
 * 이 object는 추후 제거될 예정입니다.
 */
object CbtEngine {

    suspend fun analyze(thought: String, lang: String = "ko"): CbtAnalysis {
        if (BuildConfig.GEMINI_API_KEY.isBlank() || BuildConfig.GEMINI_API_KEY.contains("YOUR_")) {
            throw IllegalStateException("API Key not configured in BuildConfig")
        }
        val lower = thought.lowercase()
        return when {

            // Case 1: 관계 불안 — 독심술 오류 + 파국화
            lower.contains("답장") || lower.contains("연락") ||
            lower.contains("카톡") || lower.contains("문자") -> CbtAnalysis(
                cognitiveDistortions = listOf(
                    CognitiveDistortion(
                        tag = "독심술 오류",
                        englishTag = "mind_reading",
                        description = "상대방의 명확한 증거나 설명 없이 '나를 싫어할 거야'라고 상대방의 마음을 완전히 부정적으로 예단하는 왜곡입니다."
                    ),
                    CognitiveDistortion(
                        tag = "파국화",
                        englishTag = "catastrophizing",
                        description = "작은 단서에서 출발해 '관계가 영영 끝장날 것이다'와 같이 최악의 재앙적인 파멸 시나리오를 머릿속으로 키우는 패턴입니다."
                    )
                ),
                socraticQuestion = "상대가 지금 당장 당신에게 답장을 보내지 못하는 실제 상황과 다른 현실적인 방해 요소는 무엇이 있을까요?",
                options = listOf(
                    "현재 집중해야 하는 중요한 회의, 용무, 혹은 근무 중이다.",
                    "습관적으로 핸드폰을 장시간 멀리 두고 생활하는 성향이다.",
                    "확인은 했지만 사려 깊게 긴 답변을 쓰기 위해 임시 휴식 중이다."
                ),
                alternativeThoughts = listOf(
                    "친구는 지금 단지 바쁜 상황이거나 개인적인 상황일 뿐이야. 나와의 호감도나 내 존재 가치와는 전혀 무관해.",
                    "조급해하기보다 나의 평온한 호흡에 충실하며 때가 되면 올 연착을 가볍게 기다리는 편이 내 마음에 이로워."
                )
            )

            // Case 2: 성취 불안 — 파국화 + 과도한 일반화
            lower.contains("면접") || lower.contains("시험") ||
            lower.contains("실패") || lower.contains("불합격") || lower.contains("망") -> CbtAnalysis(
                cognitiveDistortions = listOf(
                    CognitiveDistortion(
                        tag = "파국화",
                        englishTag = "catastrophizing",
                        description = "일회성 좌절이나 실수 하나를 두고 인생 전체의 종말이나 돌이킬 수 없는 완벽한 폐허로 인식하는 감정 극단성입니다."
                    ),
                    CognitiveDistortion(
                        tag = "과도한 일반화",
                        englishTag = "overgeneralization",
                        description = "하나의 한정된 사건을 근거로 '난 매번 이럴 거야, 늘 실패할 거야'라고 삶 전반을 부정적으로 덮어버리는 오류입니다."
                    )
                ),
                socraticQuestion = "단 한 번의 불충분함이나 불합격이 당신이라는 인격의 근원적 재능과 자격 전부를 평가절하할 만큼 절대적인 객관 근거일까요?",
                options = listOf(
                    "이번 탈락은 단순히 그 기관과 나의 상호 결이 일치하지 않았던 배치의 결과다.",
                    "성장하고 나아가기 위해 보강해야 할 실질적인 피드백을 주는 유익한 중간 평가이다.",
                    "수많은 성공한 거장들도 커리어 초반에는 수십 번에 달하는 거절과 고배를 당연하게 겪었다."
                ),
                alternativeThoughts = listOf(
                    "불합격은 단지 한 번의 매칭 결과일 뿐이야. 내 존재가 부패했거나 능력치의 한계를 영원히 규정하는 것은 결코 아니야.",
                    "오늘 배운 경험치를 더 단단한 화이트보드 삼아 부족함을 메우고 다음 목표를 향해 정교하게 다듬자."
                )
            )

            // Case 3: 발표/대인 불안 — 정서적 여과 + 개인화
            lower.contains("실수") || lower.contains("발표") ||
            lower.contains("다들") || lower.contains("말실수") || lower.contains("부끄") -> CbtAnalysis(
                cognitiveDistortions = listOf(
                    CognitiveDistortion(
                        tag = "정서적 여과",
                        englishTag = "mental_filter",
                        description = "훌륭했던 분위기나 90%의 원만한 흐름은 전부 무시한 채, 아주 작고 사소한 10%의 실수에만 돋보기를 갖다 대 온 정신을 가두는 왜곡입니다."
                    ),
                    CognitiveDistortion(
                        tag = "개인화",
                        englishTag = "personalization",
                        description = "주변 사람들의 미약한 표정 변화, 무관심, 혹은 중립적인 태도를 주관적으로 '내가 뭘 잘못했기 때문일 것'이라 판단하는 오류입니다."
                    )
                ),
                socraticQuestion = "타인이 나의 실수를 머릿속에 수집하며 계속 비웃고 있으리라는 생각에 대항하는 반대 사실은 무엇일까요?",
                options = listOf(
                    "대부분의 타인들은 자기 마음속 고통과 용무에 바빠 내 자잘한 실수를 기억할 만큼의 여유 공간이 없다.",
                    "모든 대화 속 실수는 인간다운 자연스러움의 일부이며, 다른 사람들도 일상 속 실수를 너그럽게 잊어버린다.",
                    "그 사건 외에 전체 미팅과 담론은 전반적으로 큰 차질 없이 원활하게 흘러갔다."
                ),
                alternativeThoughts = listOf(
                    "나조차 남의 어색함을 금방 잊어버리듯, 다른 사람들도 내 실수를 오랫동안 저장하지 않아. 안도하며 훌훌 털어도 괜찮아.",
                    "실수가 주는 단편적 시선에 내 소중한 하루의 전체 우울을 제물로 바칠 필요가 전혀 없다."
                )
            )

            // 기본 폴백: 감정적 추론 + 이분법적 사고
            else -> CbtAnalysis(
                cognitiveDistortions = listOf(
                    CognitiveDistortion(
                        tag = "감정적 추론",
                        englishTag = "emotional_reasoning",
                        description = "객관적인 외부 증거 없이 단순히 '내가 지금 극도로 비참하고 두려우니 상황 또한 백 퍼센트 최악이며 재앙이다'라고 믿는 왜곡입니다."
                    ),
                    CognitiveDistortion(
                        tag = "이분법적 사고",
                        englishTag = "all_or_nothing",
                        description = "세상을 오직 100점자리 성공과 0점짜리 쓰레기 파탄으로 두 극단으로만 나누고, 그 중간의 찬란한 회색 지대를 차단하는 성향입니다."
                    )
                ),
                socraticQuestion = "지금 느끼는 당신의 이 압도적인 불안 감정이, 차분한 제3의 배심원 눈앞에 엄밀한 실체적 증거로서 채택될 수 있을까요?",
                options = listOf(
                    "감정은 날씨나 바람과 같이 기분 따라 가변적으로 소실되는 현상일 뿐, 절대적인 우주적 팩트가 아니다.",
                    "스쳐 가는 나쁜 기분에 일일이 과몰입하여 현실인 양 극단적으로 해석을 보탠 부분이 있었다.",
                    "단지 몸이 피곤하거나 뇌의 도파민 지수가 낮아져 부정 가중치가 일시적으로 붙었을 수 있다."
                ),
                alternativeThoughts = listOf(
                    "불안한 내 피어 오른 안개 같은 감정은 내게 일시적으로 경고를 보낼 뿐이며, 나 자신이나 나의 온전한 삶의 흐름을 통제하지 못한다.",
                    "이 감정의 해일 또한 영원하지 않다. 심호흡과 함께 그대로 머무르다 보면 언젠가 서서히 흩어질 구름이다."
                )
            )
        }
    }
}
