package com.example.termproject.service
import kotlinx.coroutines.withTimeout
import android.graphics.Bitmap
import com.google.ai.client.generativeai.type.content
import android.util.Log
import org.json.JSONObject
import com.example.termproject.BuildConfig
import com.example.termproject.model.CbtAnalysis
import com.example.termproject.model.CognitiveDistortion
import com.example.termproject.model.BehavioralActivity
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName

import android.content.Context

class GeminiAnalysisService(private val context: Context) {

    private val gson = Gson()
    private val fallback = CbtAnalysisService.getInstance(context)

    // ──────────────────────────────────────────────
    // 🔑 API Key Rotation Pool
    //    local.properties에 키를 채워 넣으면 자동으로 풀에 포함됨
    // ──────────────────────────────────────────────
    private val apiKeyPool: List<String> = listOf(
        BuildConfig.GEMINI_API_KEY,
        BuildConfig.GEMINI_API_KEY_2,
        BuildConfig.GEMINI_API_KEY_3
    ).filter { key ->
        key.isNotBlank() &&
        key.all { it.code in 32..126 } && // 비ASCII(한글 등 깨진 문자) 제외
        !key.startsWith("YOUR_") &&        // 영문 플레이스홀더 제외
        key.length >= 10                   // 너무 짧은 더미 값 제외
    } // 유효하지 않은 키 자동 제외

    // 현재 사용 중인 키 인덱스 (휘발성 — 앱 재시작 시 0부터 다시 시작)
    @Volatile private var currentKeyIndex = 0

    private fun buildModel(apiKey: String) = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            maxOutputTokens = 4096
            responseMimeType = "application/json"
        }
    )

    /**
     * 현재 키로 API 호출 시도 → 429/quota 에러면 다음 키로 자동 로테이션.
     * 모든 키가 소진되면 마지막 예외를 그대로 던짐.
     */
    private suspend fun <T> withKeyRotation(block: suspend (GenerativeModel) -> T): T {
        if (apiKeyPool.isEmpty()) throw Exception("API 키가 설정되지 않았습니다.")

        var lastException: Exception? = null
        val startIndex = currentKeyIndex

        repeat(apiKeyPool.size) { attempt ->
            val idx = (startIndex + attempt) % apiKeyPool.size
            val key = apiKeyPool[idx]
            val model = buildModel(key)

            try {
                val result = block(model)
                // 성공 시 현재 인덱스 유지
                currentKeyIndex = idx
                Log.d("KeyRotation", "✅ 키 #${idx + 1} 성공")
                return result
            } catch (e: Exception) {
                Log.w("KeyRotation", "⚠️ 키 #${idx + 1} 실패 (${e.message}) → 다음 키로 전환")
                currentKeyIndex = (idx + 1) % apiKeyPool.size
                lastException = e
            }
        }
        throw lastException ?: Exception("모든 API 키 호출에 실패했습니다.")
    }

    fun listAvailableModels() {
        Log.d("KeyRotation", "현재 키 풀 크기: ${apiKeyPool.size}개")
        Log.d("API_DEBUG", "Ensure your API key is in the correct Google Cloud Project.")
    }


    private fun fallbackExposureTags(lang: String): Pair<List<String>, String> {
        val isEn = lang.lowercase() == "en"
        val tags = if (isEn) {
            listOf("Warm Smile", "Healthy Energy", "Positive Vibe")
        } else {
            listOf("따뜻한 미소", "건강한 에너지", "긍정적인 분위기")
        }
        val description = if (isEn) {
            "Your appearance exudes warmth and energy. Be confident in who you are today."
        } else {
            "당신의 모습은 따뜻함과 에너지를 풍깁니다. 오늘 하루도 스스로에게 자신감을 가지세요."
        }
        return Pair(tags, description)
    }

    suspend fun analyzeExposureImageWithTags(imageBitmap: Bitmap, thought: String = "", lang: String = "ko"): Pair<List<String>, String>? {
        if (apiKeyPool.isEmpty()) return fallbackExposureTags(lang)

        return try {
            val targetLang = if (lang.lowercase() == "en") "English" else "Korean"
            val prompt = """
    You are a CBT therapist.
    The user is struggling with a negative self-image thought: "$thought"
    Analyze the ACTUAL uploaded image of the user.
    Look carefully at:
    - facial expression
    - smile
    - eyes
    - posture
    - clothing style
    - colors
    - lighting
    - background environment
    - mood conveyed by the image

    Generate results that are SPECIFIC to THIS image and directly challenge the user's negative thought "$thought".

    Requirements:
    1. Create exactly 3 positive tags based on what is visually present in the image.
    2. Write 1 warm CBT-style encouraging message that helps them reframe their negative self-image.
    3. Do NOT use generic tags unless they truly match the image.
    4. Different images should produce different tags and feedback.
    5. Focus on strengths, confidence, warmth, energy, calmness, authenticity, effort, or positive atmosphere.

    Return ONLY valid JSON:
    {
      "tags": ["tag1", "tag2", "tag3"],
      "description": "warm personalized feedback"
    }
    All text must be written in $targetLang.
""".trimIndent()
            //1. Generate the content (withKeyRotation으로 자동 키 전환)
            val response = withKeyRotation { model ->
                model.generateContent(content {
                    image(imageBitmap)
                    text(prompt)
                })
            }

            // 1. Validation Logic
            val candidate = response.candidates.firstOrNull()
            if (candidate?.finishReason?.name != "STOP") {
                Log.e("GEMINI_ERROR", "Generation failed. Reason: ${candidate?.finishReason?.name}")
                return fallbackExposureTags(lang)
            }

            val rawText = response.text
            if (rawText.isNullOrBlank()) {
                Log.e("GEMINI_ERROR", "Model returned empty text.")
                return fallbackExposureTags(lang)
            }

            // 2. Parsing Logic
            val cleanJson = rawText.replace("```json", "").replace("```", "").trim()

            // --- YOUR FIX STARTS HERE ---
            val jsonObject = JSONObject(cleanJson)
            val jsonArray = jsonObject.getJSONArray("tags")
            val tagsList = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                tagsList.add(jsonArray.getString(i))
            }
            val description = jsonObject.getString("description")

            // This line returns the result to the caller (CbtViewModel)
            return Pair(tagsList, description)
            // --- YOUR FIX ENDS HERE ---

        } catch (e: Exception) {
            Log.e("GeminiService", "Error analyzing exposure image: ${e.message}", e)
            fallbackExposureTags(lang)
        }
    }
    suspend fun analyze(thought: String, lang: String = "ko"): CbtAnalysis {
        val prompt = buildDistortionPrompt(thought, lang)

        // withKeyRotation으로 자동 키 전환하며 content 생성
        val response = withKeyRotation { model -> model.generateContent(prompt) }

        // Get the raw text
        val rawText = response.text ?: throw Exception("Gemini returned a null/empty response.")

        // Log the exact text received to see what's causing the "unexpected" nature
        Log.d("GEMINI_DEBUG", "Raw Model Output: $rawText")

        return try {
            // Remove all markdown formatting
            val sanitizedJson = rawText.replace(Regex("```json|```"), "").trim()

            // Parse with GSON directly
            val dto = gson.fromJson(sanitizedJson, DistortionResponseDto::class.java)
            dto.toCbtAnalysis() ?: throw Exception("Data mapping resulted in null.")
        } catch (e: Exception) {
            Log.e("GeminiService", "Parsing failed for: $rawText", e)
            throw Exception("Failed to parse AI response: ${e.message}")
        }
    }
    // ──────────────────────────────────────────────
    // Clean Core Prompt Structures
    // ──────────────────────────────────────────────

    private fun buildDistortionPrompt(thought: String, lang: String): String {
        val targetLang = if (lang.lowercase() == "en") "English" else "Korean"
        return """
        You are a CBT therapist. Analyze: "$thought"
        Return ONLY valid JSON with this exact structure:
        {
          "cognitiveDistortions": [
            { "tag": "...", "englishTag": "...", "description": "..." }
          ],
          "socraticQuestion": "...",
          "options": ["...", "...", "..."],
          "alternativeThoughts": ["...", "..."]
        }
        All text must be in $targetLang.
    """.trimIndent()
    }

    private fun buildSocraticPrompt(thought: String, distortions: List<CognitiveDistortion>, lang: String): String {
        val targetLang = if (lang.lowercase() == "en") "English" else "Korean (한국어)"
        return """
            You are a professional Cognitive Behavioral Therapist (CBT).
            Analyze this thought and its distortions to generate 1 deep Socratic questioning prompt to break down this negative pattern, exactly 3 realistic alternative perspective options to fact-check it, and 2 balanced positive final thoughts.
            All values inside the array values MUST be written in $targetLang. Output your response as a RAW JSON object with NO markdown text blocks.

            Original User Thought: "$thought"
            Identified Distortions:
            ${distortions.joinToString("\n") { "- ${it.tag}" }}

            Required JSON Structure:
            {
              "socraticQuestion": "Deep unique question in $targetLang challenging the thought validation facts",
              "options": [
                "Dynamic option alternative 1 based entirely on analyzing '$thought' in $targetLang",
                "Dynamic option alternative 2 based entirely on analyzing '$thought' in $targetLang",
                "Dynamic option alternative 3 based entirely on analyzing '$thought' in $targetLang"
              ],
              "alternativeThoughts": [
                "Balanced replacement reframed thought 1 in $targetLang",
                "Balanced replacement reframed thought 2 in $targetLang"
              ]
            }
        """.trimIndent()
    }

    // ──────────────────────────────────────────────
    // Bulletproof JSON Processors (No Tokenizer Mixups)
    // ──────────────────────────────────────────────

    private fun parseDistortionResponse(raw: String): CbtAnalysis? {
        return try {
            val dto = gson.fromJson(raw.trim(), DistortionResponseDto::class.java)
            dto.toCbtAnalysis()
        } catch (e: Exception) {
            null
        }
    }

    private fun parseSocraticResponse(raw: String): SocraticResult? {
        return try {
            val dto = gson.fromJson(raw.trim(), SocraticResult::class.java)
            dto
        } catch (e: Exception) {
            null
        }
    }

    data class SocraticResult(
        val socraticQuestion: String,
        val options: List<String>,
        val alternativeThoughts: List<String>
    )

    private data class DistortionResponseDto(
        @SerializedName("cognitiveDistortions") val cognitiveDistortions: List<DistortionDto>,
        @SerializedName("socraticQuestion") val socraticQuestion: String,
        @SerializedName("options") val options: List<String>,
        @SerializedName("alternativeThoughts") val alternativeThoughts: List<String>
    ) {
        fun toCbtAnalysis() = CbtAnalysis(
            cognitiveDistortions = cognitiveDistortions.map { it.toCognitiveDistortion() },
            socraticQuestion = socraticQuestion,
            options = options,
            alternativeThoughts = alternativeThoughts
        )
    }
    private data class DistortionDto(
        @SerializedName("tag") val tag: String? = null,
        @SerializedName("englishTag") val englishTag: String? = null,
        @SerializedName("description") val description: String? = null
    ) {
        fun toCognitiveDistortion() = CognitiveDistortion(
            tag = tag ?: "인지 왜곡",
            englishTag = englishTag ?: "unknown",
            description = description ?: ""
        )
    }

    suspend fun recommendBehavioralActivities(
        thought: String,
        latitude: Double,
        longitude: Double,
        lang: String = "ko"
    ): List<BehavioralActivity> {
        if (apiKeyPool.isEmpty()) {
            return fallback.recommendBehavioralActivities(thought, latitude, longitude, lang)
        }

        val prompt = buildBehavioralPrompt(thought, latitude, longitude, lang)

        return try {
            val response = withKeyRotation { model -> model.generateContent(prompt) }
            val rawText = response.text
            if (rawText.isNullOrBlank()) {
                Log.e("GeminiService", "Model returned empty text for behavioral recommendation.")
                return fallback.recommendBehavioralActivities(thought, latitude, longitude, lang)
            }

            val sanitizedJson = rawText.replace(Regex("```json|```"), "").trim()
            val dto = gson.fromJson(sanitizedJson, BehavioralRecommendResponseDto::class.java)
            dto.activities.mapIndexed { index, item ->
                BehavioralActivity(
                    id = item.id.ifBlank { "gemini_$index" },
                    name = item.name,
                    distance = item.distance,
                    purpose = item.purpose,
                    durationText = item.durationText,
                    emoji = item.emoji,
                    instruction = item.instruction,
                    latitude = item.latitude,
                    longitude = item.longitude
                )
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Error recommending behavioral activities: ${e.message}", e)
            fallback.recommendBehavioralActivities(thought, latitude, longitude, lang)
        }
    }

    private fun buildBehavioralPrompt(
        thought: String,
        latitude: Double,
        longitude: Double,
        lang: String
    ): String {
        val targetLang = if (lang.lowercase() == "en") "English" else "Korean"
        return """
            You are a CBT therapist. 
            The user is experiencing a negative thought: "$thought"
            Based on the user's current GPS location (latitude: $latitude, longitude: $longitude), recommend exactly 3 REAL and EXISTING natural healing spots, walking paths, mountains, rivers, parks, or scenic spots within a 10km radius (such as Geumjeongsan Mountain/금정산성 or Oncheoncheon Stream/온천천 산책로 in Busan, depending on their actual GPS coordinates).
            
            Provide the recommendation in JSON format matching the structure below.
            All text contents (name, distance, purpose, durationText, instruction) must be in $targetLang.
            
            JSON Structure:
            {
              "activities": [
                {
                  "id": "A unique lowercase string ID (e.g. geumjeongsan, oncheoncheon)",
                  "name": "Name of the real place (e.g. 금정산성 둘레길)",
                  "distance": "Approximate distance from current coordinates in km (e.g. 2.5km)",
                  "purpose": "A brief explanation of how this specific walk helps soothe their thought: '$thought' (e.g. 넓게 트인 전경을 보며 자책에서 벗어나기)",
                  "durationText": "Estimated walking time (e.g. 약 30분 소요)",
                  "emoji": "One relevant emoji (e.g. 🌲, 🌊, ⛰️)",
                  "instruction": "CBT-based mindfulness walk instruction specific to this place (e.g. 성곽 길을 걸으며 시원한 바람소리에 오감을 열어보세요)",
                  "latitude": 35.1234,
                  "longitude": 129.1234
                }
              ]
            }
            Ensure the latitude and longitude are accurate for the recommended place. 
            Return ONLY raw JSON, do not include any markdown format tags like ```json.
        """.trimIndent()
    }

    private data class BehavioralRecommendResponseDto(
        @SerializedName("activities") val activities: List<BehavioralRecommendDto>
    )

    private data class BehavioralRecommendDto(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("distance") val distance: String,
        @SerializedName("purpose") val purpose: String,
        @SerializedName("durationText") val durationText: String,
        @SerializedName("emoji") val emoji: String,
        @SerializedName("instruction") val instruction: String,
        @SerializedName("latitude") val latitude: Double,
        @SerializedName("longitude") val longitude: Double
    )
}