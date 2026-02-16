package com.firstone.newone.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyUnits(
    @SerialName("time") val time: String,
    @SerialName("temperature_2m_max") val temperatureMax: String,
    @SerialName("temperature_2m_min") val temperatureMin: String,
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: String,
    @SerialName("windspeed_10m_max") val windSpeedMax: String,
    @SerialName("apparent_temperature_max") val apparentTemperatureMax: String? = null,
    @SerialName("apparent_temperature_min") val apparentTemperatureMin: String? = null,
    @SerialName("air_quality_pm10_max") val airQualityPm10Max: String? = null,
    @SerialName("air_quality_pm2_5_max") val airQualityPm25Max: String? = null,
    @SerialName("air_quality_index_max") val airQualityIndexMax: String? = null,
)

@Serializable
data class Daily(
    @SerialName("time") val time: List<String>,
    @SerialName("temperature_2m_max") val temperatureMax: List<Double>,
    @SerialName("temperature_2m_min") val temperatureMin: List<Double>,
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Int> = emptyList(),
    @SerialName("windspeed_10m_max") val windSpeedMax: List<Double> = emptyList(),
    @SerialName("apparent_temperature_max") val apparentTemperatureMax: List<Double> = emptyList(),
    @SerialName("apparent_temperature_min") val apparentTemperatureMin: List<Double> = emptyList(),
    @SerialName("air_quality_pm10_max") val airQualityPm10Max: List<Double> = emptyList(),
    @SerialName("air_quality_pm2_5_max") val airQualityPm25Max: List<Double> = emptyList(),
    @SerialName("air_quality_index_max") val airQualityIndexMax: List<Int> = emptyList(),
)

@Serializable
data class HourlyUnits(
    @SerialName("time") val time: String,
    @SerialName("temperature_2m") val temperature: String,
    @SerialName("apparent_temperature") val apparentTemperature: String? = null,
    @SerialName("precipitation_probability") val precipitationProbability: String,
    @SerialName("windspeed_10m") val windSpeed: String,
    @SerialName("air_quality_index") val airQualityIndex: String? = null,
)

@Serializable
data class Hourly(
    @SerialName("time") val time: List<String>,
    @SerialName("temperature_2m") val temperature: List<Double> = emptyList(),
    @SerialName("apparent_temperature") val apparentTemperature: List<Double> = emptyList(),
    @SerialName("precipitation_probability") val precipitationProbability: List<Int> = emptyList(),
    @SerialName("windspeed_10m") val windSpeed: List<Double> = emptyList(),
    @SerialName("air_quality_index") val airQualityIndex: List<Int> = emptyList(),
)

@Serializable
data class ForecastResponse(
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("timezone") val timezone: String,
    @SerialName("daily_units") val dailyUnits: DailyUnits,
    @SerialName("daily") val daily: Daily,
    @SerialName("hourly_units") val hourlyUnits: HourlyUnits? = null,
    @SerialName("hourly") val hourly: Hourly? = null,
)


