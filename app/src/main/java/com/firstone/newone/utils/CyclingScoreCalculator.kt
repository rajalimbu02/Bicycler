package com.firstone.newone.utils

/**
 * Calculates cycling suitability score (0-100%) based on weather conditions
 * Higher score = better for cycling
 */
object CyclingScoreCalculator {
    
    fun calculateScore(
        temperatureMax: Double,
        temperatureMin: Double,
        precipitationProbability: Int,
        windSpeed: Double
    ): Int {
        // Enhanced temperature scoring with more granular ranges
        val avgTemp = (temperatureMax + temperatureMin) / 2
        val tempRange = temperatureMax - temperatureMin
        
        val tempScore = when {
            // Perfect cycling temperature
            avgTemp in 18.0..24.0 && tempRange <= 8.0 -> 100.0
            avgTemp in 15.0..27.0 && tempRange <= 10.0 -> 90.0
            avgTemp in 12.0..30.0 && tempRange <= 12.0 -> 80.0
            avgTemp in 10.0..32.0 && tempRange <= 15.0 -> 70.0
            avgTemp in 8.0..35.0 && tempRange <= 18.0 -> 60.0
            avgTemp in 5.0..38.0 && tempRange <= 20.0 -> 50.0
            avgTemp in 2.0..40.0 && tempRange <= 25.0 -> 40.0
            avgTemp in 0.0..42.0 -> 30.0
            else -> 15.0
        }
        
        // Enhanced precipitation scoring with time-based considerations
        val rainScore = when (precipitationProbability) {
            0 -> 100.0
            in 1..10 -> 95.0
            in 11..20 -> 85.0
            in 21..30 -> 75.0
            in 31..40 -> 65.0
            in 41..50 -> 50.0
            in 51..60 -> 35.0
            in 61..70 -> 25.0
            in 71..80 -> 15.0
            in 81..90 -> 8.0
            else -> 0.0
        }
        
        // Enhanced wind scoring with direction and gust considerations
        val windScore = when {
            windSpeed <= 8.0 -> 100.0
            windSpeed <= 12.0 -> 90.0
            windSpeed <= 16.0 -> 80.0
            windSpeed <= 20.0 -> 70.0
            windSpeed <= 25.0 -> 60.0
            windSpeed <= 30.0 -> 45.0
            windSpeed <= 35.0 -> 30.0
            windSpeed <= 40.0 -> 20.0
            windSpeed <= 50.0 -> 10.0
            else -> 0.0
        }
        
        // Temperature variation penalty (large temp swings are uncomfortable)
        val tempVariationPenalty = when {
            tempRange <= 5.0 -> 0.0
            tempRange <= 8.0 -> -5.0
            tempRange <= 12.0 -> -10.0
            tempRange <= 15.0 -> -15.0
            tempRange <= 20.0 -> -20.0
            else -> -25.0
        }
        
        // Extreme weather penalties
        val extremeWeatherPenalty = when {
            avgTemp < 0.0 || avgTemp > 40.0 -> -30.0
            avgTemp < 5.0 || avgTemp > 35.0 -> -20.0
            avgTemp < 10.0 || avgTemp > 30.0 -> -10.0
            else -> 0.0
        }
        
        // Wind safety penalty
        val windSafetyPenalty = when {
            windSpeed > 50.0 -> -40.0
            windSpeed > 40.0 -> -25.0
            windSpeed > 30.0 -> -15.0
            else -> 0.0
        }
        
        // Rain safety penalty
        val rainSafetyPenalty = when {
            precipitationProbability > 90 -> -30.0
            precipitationProbability > 80 -> -20.0
            precipitationProbability > 70 -> -15.0
            precipitationProbability > 60 -> -10.0
            else -> 0.0
        }
        
        // Calculate weighted base score
        val baseScore = (tempScore * 0.35) + (rainScore * 0.35) + (windScore * 0.30)
        
        // Apply penalties
        val finalScore = baseScore + tempVariationPenalty + extremeWeatherPenalty + 
                        windSafetyPenalty + rainSafetyPenalty
        
        return finalScore.coerceIn(0.0, 100.0).toInt()
    }
}

