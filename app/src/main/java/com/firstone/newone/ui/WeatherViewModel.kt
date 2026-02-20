package com.firstone.newone.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.firstone.newone.data.ForecastResponse
import com.firstone.newone.data.WeatherDatabase
import com.firstone.newone.data.WeatherRepository
import com.firstone.newone.location.LocationProvider
import com.firstone.newone.utils.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Idle : WeatherUiState
    data object Loading : WeatherUiState
    data class Success(val data: ForecastResponse) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val database = WeatherDatabase.getDatabase(application)
    private val repository = WeatherRepository(weatherDao = database.weatherDao())
    private val locationProvider = LocationProvider(application)
    private val permissionManager = PermissionManager(application)

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState

    fun load() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                // First try to get location from saved preference
                val savedLocation = permissionManager.getLastKnownLocation()
                var location = savedLocation?.let { 
                    android.location.Location("saved").apply {
                        latitude = it.first
                        longitude = it.second
                    }
                }
                
                // If no saved location or permission not granted, try current location
                if (location == null || !permissionManager.isLocationPermissionGranted()) {
                    location = locationProvider.getLastKnownLocation()
                }
                
                if (location == null) {
                    _uiState.value = WeatherUiState.Error("Location unavailable")
                    return@launch
                }
                
                // Save location for future use
                permissionManager.saveLastKnownLocation(location.latitude, location.longitude)
                
                val forecast = repository.fetch7DayForecast(location.latitude, location.longitude)
                _uiState.value = WeatherUiState.Success(forecast)
            } catch (t: Throwable) {
                // If API fails, try to load from cache
                try {
                    val savedLocation = permissionManager.getLastKnownLocation()
                    if (savedLocation != null) {
                        val cachedForecast = repository.getCachedForecast(savedLocation.first, savedLocation.second)
                        if (cachedForecast != null) {
                            _uiState.value = WeatherUiState.Success(cachedForecast)
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    // Cache also failed
                }
                _uiState.value = WeatherUiState.Error(t.message ?: "No internet connection and no cached data")
            }
        }
    }
    
    fun onPermissionGranted() {
        permissionManager.setLocationPermissionGranted(true)
        load() // Reload with new permission
    }
}


