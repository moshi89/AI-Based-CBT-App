package com.example.termproject.model

/**
 * Represents a recommended behavioral activity/mission for Path B.
 */
data class BehavioralActivity(
    val id: String,
    val name: String,
    val distance: String,
    val purpose: String,
    val durationText: String,
    val emoji: String,
    val instruction: String,
    val postMoodScore: Int = 3,
    val latitude: Double = 35.1796, // 기본값: 부산시청
    val longitude: Double = 129.0756
)
