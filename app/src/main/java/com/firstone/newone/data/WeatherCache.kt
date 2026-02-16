package com.firstone.newone.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey
    val locationKey: String, // "lat_lon" format
    val forecastData: String, // JSON string of ForecastResponse
    val timestamp: Long, // When data was cached
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class CachedForecastData(
    val forecast: ForecastResponse,
    val cachedAt: Long,
    val locationKey: String
)


