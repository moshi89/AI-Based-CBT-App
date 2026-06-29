package com.example.termproject.model

/**
 * Represents metadata recorded during a Behavioral Path walk activity.
 */
data class BehavioralInfo(
    val activityName: String,
    val duration: String,
    val distance: String,
    val postMood: String,
    val reflectionText: String
)
