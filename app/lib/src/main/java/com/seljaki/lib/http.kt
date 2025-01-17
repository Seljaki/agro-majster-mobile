package com.seljaki.lib

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

val SELJAKI_NET_SERVER = "https://seljakinet-server.schnapsen66.eu"
val SELJAKI_CHAIN_SERVER = "http://smd.schnapsen66.eu:3336"

val client = HttpClient(CIO)

suspend fun recognizeWeather(image: ByteArray, contentType: String): WeatherPrediction? {
    return try {
        val imageFormat = contentType.substring(6, contentType.length)
        val response: HttpResponse = client
            .submitFormWithBinaryData(
                url = "$SELJAKI_NET_SERVER/predict",
                formData = formData {
                    append("file", image, Headers.build {
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"upload.$imageFormat\"")
                        append(HttpHeaders.ContentType, contentType)
                    })
                }
            )

        if (response.status != HttpStatusCode.OK)
            return null

        val body = response.bodyAsText()
        return Json.decodeFromString(body)
    } catch (e: Exception) {
        println(e)
        null
    }
}

suspend fun getBlockchain(): Blockchain? {
    return try {
        val response: HttpResponse = client
            .get("$SELJAKI_CHAIN_SERVER/blockchain/blocks")

        if (response.status != HttpStatusCode.OK)
            return null

        var body = response.bodyAsText()
        body = body.replace("uuid", "UUID");
        return Blockchain.fromJson(body)
    } catch (e: Exception) {
        println(e)
        null
    }
}