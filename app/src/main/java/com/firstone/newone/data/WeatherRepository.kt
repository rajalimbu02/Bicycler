package com.firstone.newone.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class WeatherRepository(
    private val service: WeatherService = WeatherService.create(),
    private val weatherDao: WeatherDao
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val cacheExpiryTime = TimeUnit.DAYS.toMillis(1) // 1 day cache
    
    suspend fun fetch7DayForecast(latitude: Double, longitude: Double): ForecastResponse = withContext(Dispatchers.IO) {
        val locationKey = "${latitude}_${longitude}"
        
        // Check cache first
        val cachedData = weatherDao.getCachedWeather(locationKey)
        if (cachedData != null && !isCacheExpired(cachedData.timestamp)) {
            return@withContext json.decodeFromString<ForecastResponse>(cachedData.forecastData)
        }
        
        // Fetch from API
        val forecast = service.get7DayForecast(latitude, longitude)
        
        // Cache the result
        val cacheEntity = WeatherCacheEntity(
            locationKey = locationKey,
            forecastData = json.encodeToString(forecast),
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude
        )
        weatherDao.insertWeatherCache(cacheEntity)
        
        // Clean up expired cache
        weatherDao.deleteExpiredCache(System.currentTimeMillis() - cacheExpiryTime)
        
        forecast
    }
    
    suspend fun getCachedForecast(latitude: Double, longitude: Double): ForecastResponse? = withContext(Dispatchers.IO) {
        val locationKey = "${latitude}_${longitude}"
        val cachedData = weatherDao.getCachedWeather(locationKey)
        
        if (cachedData != null && !isCacheExpired(cachedData.timestamp)) {
            json.decodeFromString<ForecastResponse>(cachedData.forecastData)
        } else null
    }
    
    private fun isCacheExpired(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp > cacheExpiryTime
    }
}


