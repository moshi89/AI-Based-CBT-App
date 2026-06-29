package com.example.termproject.model

/**
 * Represents a Cognitive Distortion identified by your custom ML model.
 */
data class CognitiveDistortion(
    val tag: String,          // e.g., "독심술 오류"
    val englishTag: String,   // e.g., "Mind Reading"
    val description: String,  // e.g., "타인의 마음을 부정적으로 추측합니다..."
         // 💡 ADD THIS: Actual probability from your model (e.g., 0.68f)
)