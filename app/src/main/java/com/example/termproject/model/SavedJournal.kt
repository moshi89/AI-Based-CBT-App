package com.example.termproject.model

import java.util.UUID

/**
 * Represents a saved CBT cognitive restructuring event in the user's secure vault.
 */
data class SavedJournal(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val originalThought: String,
    val cognitiveDistortions: List<CognitiveDistortion>,
    val socraticQuestion: String,
    val selectedOption: String,
    val alternativeThoughts: List<String>,
    val pathType: String = "cognitive", // "cognitive" | "behavioral" | "exposure"
    val behavioralInfo: BehavioralInfo? = null,
    val exposureInfo: ExposureInfo? = null
)
