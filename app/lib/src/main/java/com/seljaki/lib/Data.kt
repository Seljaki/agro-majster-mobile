package com.seljaki.lib

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Data (
    val temperature: Double,
    val longitude: Double,
    val latitude: Double,
    val prediction: WeatherPrediction,
    val UUID: String?
) {
    fun toJson(): String {
        return Json.encodeToString(this)
    }
}