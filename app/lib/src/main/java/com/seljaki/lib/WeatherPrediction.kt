package com.seljaki.lib

import kotlinx.serialization.Serializable

@Serializable
data class WeatherPrediction (
    val clear: Double,
    val cloudy: Double,
    val rainy: Double,
) {
    fun getPredicted(): String {
        return if(clear > cloudy && clear > rainy)
            "clear";
        else if(cloudy > clear && cloudy > rainy)
            "cloudy"
        else
            "rainy"
    }
}