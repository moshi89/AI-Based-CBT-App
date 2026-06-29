package com.example.termproject.model

/**
 * Represents a complete CBT Socratic Analysis structure.
 */
data class CbtAnalysis(
    val cognitiveDistortions: List<CognitiveDistortion>,
    val socraticQuestion: String = "",
    val options: List<String> = emptyList(),
    val alternativeThoughts: List<String> = emptyList(),
    val distortionScores: Map<String, Float> = emptyMap()
)
