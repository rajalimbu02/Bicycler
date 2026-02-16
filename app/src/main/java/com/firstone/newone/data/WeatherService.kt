package com.firstone.newone.data

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType.Companion.toMediaType

interface WeatherService {
    @GET("/v1/forecast")
    suspend fun get7DayForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,precipitation_probability_max,windspeed_10m_max,apparent_temperature_max,apparent_temperature_min",
        @Query("hourly") hourly: String = "temperature_2m,apparent_temperature,precipitation_probability,windspeed_10m",
        @Query("timezone") timezone: String = "auto",
    ): ForecastResponse

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/"

        @OptIn(ExperimentalSerializationApi::class)
        fun create(debug: Boolean = false): WeatherService {
            val logging = HttpLoggingInterceptor().apply {
                level = if (debug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            }

            val contentType = "application/json".toMediaType()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()

            return retrofit.create(WeatherService::class.java)
        }
    }
}


