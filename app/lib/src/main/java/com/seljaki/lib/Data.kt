package com.seljaki.lib

import kotlinx.serialization.Serializable

@Serializable
data class Data (
    val temperature: Double,
    val longitude: Double,
    val latitude: Double,
    val prediction: WeatherPrediction,
    val UUID: String?
)