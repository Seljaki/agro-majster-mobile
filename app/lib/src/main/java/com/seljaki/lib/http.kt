package com.seljaki.lib

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.forms.formData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

val SELJAKI_NET_SERVER = "https://seljakinet-server.schnapsen66.eu"

val client = HttpClient(CIO)

suspend fun recognizeWeather(image: ByteArray, contentHeader: String): WeatherPrediction? {
    val response: HttpResponse = client
        .submitFormWithBinaryData(
            url = "$SELJAKI_NET_SERVER/predict",
            formData = formData {
                append("file", image, Headers.build {
                    append(HttpHeaders.ContentType, contentHeader)
                })
            }
        )

    if (response.status != HttpStatusCode.OK)
        return null

    val body = response.bodyAsText()
    return Json.decodeFromString(body)
}