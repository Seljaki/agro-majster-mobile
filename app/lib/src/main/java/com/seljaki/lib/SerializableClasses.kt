package com.seljaki.lib

import kotlinx.serialization.Serializable

@Serializable
data class WeatherPrediction (
    val clear: Double,
    val cloudy: Double,
    val rainy: Double,
)