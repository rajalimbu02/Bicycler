package com.firstone.newone.utils

import android.content.Context
import android.content.SharedPreferences

class PermissionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)
    
    private companion object {
        const val LOCATION_PERMISSION_GRANTED = "location_permission_granted"
        const val LAST_LOCATION_LAT = "last_location_lat"
        const val LAST_LOCATION_LON = "last_location_lon"
    }
    
    fun setLocationPermissionGranted(granted: Boolean) {
        prefs.edit().putBoolean(LOCATION_PERMISSION_GRANTED, granted).apply()
    }
    
    fun isLocationPermissionGranted(): Boolean {
        return prefs.getBoolean(LOCATION_PERMISSION_GRANTED, false)
    }
    
    fun saveLastKnownLocation(latitude: Double, longitude: Double) {
        prefs.edit()
            .putFloat(LAST_LOCATION_LAT, latitude.toFloat())
            .putFloat(LAST_LOCATION_LON, longitude.toFloat())
            .apply()
    }
    
    fun getLastKnownLocation(): Pair<Double, Double>? {
        return if (prefs.contains(LAST_LOCATION_LAT) && prefs.contains(LAST_LOCATION_LON)) {
            val lat = prefs.getFloat(LAST_LOCATION_LAT, 0f).toDouble()
            val lon = prefs.getFloat(LAST_LOCATION_LON, 0f).toDouble()
            Pair(lat, lon)
        } else null
    }
}


