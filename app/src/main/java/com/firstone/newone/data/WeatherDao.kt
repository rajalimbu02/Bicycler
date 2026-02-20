package com.firstone.newone.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE locationKey = :locationKey")
    suspend fun getCachedWeather(locationKey: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherCache(weatherCache: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE timestamp < :expiryTime")
    suspend fun deleteExpiredCache(expiryTime: Long)

    @Query("DELETE FROM weather_cache")
    suspend fun clearAllCache()
}


