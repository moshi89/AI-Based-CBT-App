package com.example.termproject.model

/**
 * Represents metadata recorded during an Exposure Path activity (Finding my shining moment).
 */
data class ExposureInfo(
    val photoUri: String,
    val compliments: List<String>,
    val reflectionText: String
)
