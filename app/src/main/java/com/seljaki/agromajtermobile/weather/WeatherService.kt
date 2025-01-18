package com.seljaki.agromajtermobile.weather

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = "e24b8196c1b94e9a7c5bc543a467314c",
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

data class WeatherResponse(
    @SerializedName("main") val main: Main
)

data class Main(
    @SerializedName("temp") val temp: Double
)

object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/"
    val instance: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}