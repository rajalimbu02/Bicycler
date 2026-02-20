package com.firstone.newone

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import kotlin.math.cos
import kotlin.math.sin
import androidx.lifecycle.viewmodel.compose.viewModel
import com.firstone.newone.ui.WeatherUiState
import com.firstone.newone.ui.WeatherViewModel
import com.firstone.newone.ui.theme.NewOneTheme
import com.firstone.newone.utils.CyclingScoreCalculator
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.values.all { it }
            if (granted) {
                // Permission granted, notify ViewModel
                // We'll handle this through the ViewModel
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewOneTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen(modifier = Modifier.padding(innerPadding)) {
                        requestPermission.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherScreen(modifier: Modifier = Modifier, requestLocationPermissions: () -> Unit) {
    val vm: WeatherViewModel = viewModel()
    val state by vm.uiState.collectAsState()
    val showAboutDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.load()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD), // Light blue background
                        Color(0xFFBBDEFB)  // Slightly darker light blue
                    )
                )
            )
    ) {
        Column(modifier = modifier) {
            // Header - Centered
            Text(
                text = "Bicycler",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                color = Color(0xFF1A237E),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
        
        when (state) {
            is WeatherUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is WeatherUiState.Error -> {
                val msg = (state as WeatherUiState.Error).message
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: $msg", 
                        color = Color(0xFFD32F2F),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            requestLocationPermissions()
                            vm.onPermissionGranted()
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        )
                    ) { 
                        Text(
                            "Grant Location Permission",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        ) 
                    }
                }
            }
            is WeatherUiState.Success -> {
                val data = (state as WeatherUiState.Success).data
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(data.daily.time) { index, date ->
                        val tMax = data.daily.temperatureMax.getOrNull(index) ?: 0.0
                        val tMin = data.daily.temperatureMin.getOrNull(index) ?: 0.0
                        val pop = data.daily.precipitationProbabilityMax.getOrNull(index) ?: 0
                        val windSpeed = data.daily.windSpeedMax.getOrNull(index) ?: 0.0
                        
                        val cyclingScore = CyclingScoreCalculator.calculateScore(
                            tMax, tMin, pop, windSpeed
                        )
                        
                        val dayName = formatDateToDayName(date)
                        
                        val isToday = dayName == "Today"
                        val bikingReason = if (isToday) {
                            generateBikingReason(tMax, tMin, pop, windSpeed, cyclingScore)
                        } else null
                        
                        // Get additional data for today only (with fallbacks)
                        val apparentTempMax = if (isToday) data.daily.apparentTemperatureMax?.getOrNull(index) else null
                        val apparentTempMin = if (isToday) data.daily.apparentTemperatureMin?.getOrNull(index) else null
                        val airQualityIndex = if (isToday) data.daily.airQualityIndexMax?.getOrNull(index) else null
                        val hourlyData = if (isToday) data.hourly else null
                        
                        WeatherDayCard(
                            dayName = dayName,
                            temperatureMax = tMax,
                            temperatureMin = tMin,
                            rainChance = pop,
                            windSpeed = windSpeed,
                            cyclingScore = cyclingScore,
                            bikingReason = bikingReason,
                            apparentTempMax = apparentTempMax,
                            apparentTempMin = apparentTempMin,
                            airQualityIndex = airQualityIndex,
                            hourlyData = hourlyData,
                            isToday = isToday
                        )
                        
                        // Show hourly breakdown for today only (full width outside card)
                        if (isToday) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "📅 Hourly Forecast",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A237E),
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (hourlyData != null && hourlyData.time.isNotEmpty()) {
                                // Show next 8 hours
                                val currentHour = java.time.LocalTime.now().hour
                                val nextHours = (0..7).map { (currentHour + it) % 24 }
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .padding(horizontal = 20.dp)
                                ) {
                                    items(nextHours.size) { hourIndex ->
                                        val hour = nextHours[hourIndex]
                                        val timeIndex = hourlyData.time.indexOfFirst { 
                                            it.contains(String.format("%02d:00", hour)) 
                                        }
                                        
                                        if (timeIndex >= 0 && timeIndex < hourlyData.time.size) {
                                            val temp = hourlyData.temperature.getOrNull(timeIndex) ?: 0.0
                                            val apparentTemp = hourlyData.apparentTemperature?.getOrNull(timeIndex) ?: temp
                                            val rainChance = hourlyData.precipitationProbability.getOrNull(timeIndex) ?: 0
                                            val wind = hourlyData.windSpeed.getOrNull(timeIndex) ?: 0.0
                                            
                                            HourlyWeatherCard(
                                                time = String.format("%02d:00", hour),
                                                temperature = temp,
                                                apparentTemperature = apparentTemp,
                                                rainChance = rainChance,
                                                windSpeed = wind
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Fallback: Show basic hourly info
                                val currentHour = java.time.LocalTime.now().hour
                                val nextHours = (0..7).map { (currentHour + it) % 24 }
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .padding(horizontal = 20.dp)
                                ) {
                                    items(nextHours.size) { hourIndex ->
                                        val hour = nextHours[hourIndex]
                                        val temp = tMax - (hourIndex * 2.0) // Simple fallback
                                        val feelsLike = temp + 1.0
                                        
                                        HourlyWeatherCard(
                                            time = String.format("%02d:00", hour),
                                            temperature = temp,
                                            apparentTemperature = feelsLike,
                                            rainChance = pop,
                                            windSpeed = windSpeed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // About Button at bottom
                Button(
                    onClick = { showAboutDialog.value = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    )
                ) {
                    Text(
                        "About",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
            WeatherUiState.Idle -> Text("Idle")
        }
    }
    
    // About Dialog
    if (showAboutDialog.value) {
        AlertDialog(
            onDismissRequest = { showAboutDialog.value = false },
            title = {
                Text(
                    text = "About Bicycler",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Bicycler helps you find the best days for cycling based on weather conditions.",
                        fontSize = 14.sp,
                        color = Color(0xFF424242)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🌤️ Weather Data Source:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E)
                    )
                    Text(
                        text = "Open-Meteo API (open-meteo.com)",
                        fontSize = 12.sp,
                        color = Color(0xFF424242)
                    )
                    Text(
                        text = "Global weather stations network",
                        fontSize = 11.sp,
                        color = Color(0xFF757575),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "📊 Features:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E)
                    )
                    Text(
                        text = "• 7-day weather forecast",
                        fontSize = 12.sp,
                        color = Color(0xFF424242)
                    )
                    Text(
                        text = "• Hourly breakdown for today",
                        fontSize = 12.sp,
                        color = Color(0xFF424242)
                    )
                    Text(
                        text = "• Air quality index",
                        fontSize = 12.sp,
                        color = Color(0xFF424242)
                    )
                    Text(
                        text = "• Feels like temperature",
                        fontSize = 12.sp,
                        color = Color(0xFF424242)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Developed by Raja Limbu",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "For your convenience",
                        fontSize = 11.sp,
                        color = Color(0xFF757575),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showAboutDialog.value = false }
                ) {
                    Text(
                        "OK",
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
    }
}

@Composable
fun WeatherDayCard(
    dayName: String,
    temperatureMax: Double,
    temperatureMin: Double,
    rainChance: Int,
    windSpeed: Double,
    cyclingScore: Int,
    bikingReason: String? = null,
    apparentTempMax: Double? = null,
    apparentTempMin: Double? = null,
    airQualityIndex: Int? = null,
    hourlyData: com.firstone.newone.data.Hourly? = null,
    isToday: Boolean = false
) {
    // Light blue background for all cards as requested
    val cardColor = Color(0xFFE3F2FD) // Light blue
    val accentColor = when {
        cyclingScore >= 90 -> Color(0xFF1B5E20) // Very Dark Green - Excellent
        cyclingScore >= 80 -> Color(0xFF2E7D32) // Dark Green - Very Good
        cyclingScore >= 70 -> Color(0xFF388E3C) // Green - Good
        cyclingScore >= 50 -> Color(0xFFE65100) // Dark Orange - Fair
        cyclingScore >= 30 -> Color(0xFFD84315) // Orange - Poor
        else -> Color(0xFFC62828) // Dark Red - Very Poor
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            Column(modifier = Modifier.weight(1f)) {
                if (dayName.isNotEmpty()) {
                    Text(
                        text = dayName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Text(
                    text = "🌡️ Temperature: ${String.format("%.1f", temperatureMax)}° / ${String.format("%.1f", temperatureMin)}°",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF424242)
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "☁️ Rain Chance: $rainChance%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF424242)
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "💨 Wind Speed: ${String.format("%.1f", windSpeed)} km/h",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF424242)
                )
                
                // Show feels like temperature for today only
                if (isToday) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val feelsLikeMax = apparentTempMax ?: (temperatureMax + 2.0) // Fallback calculation
                    val feelsLikeMin = apparentTempMin ?: (temperatureMin - 1.0) // Fallback calculation
                    Text(
                        text = "🌡️ Feels Like: ${String.format("%.1f", feelsLikeMax)}° / ${String.format("%.1f", feelsLikeMin)}°",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242)
                    )
                }
                
                // Show air quality for today only
                if (isToday) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val airQuality = airQualityIndex ?: 2 // Default to "Fair" if not available
                    val airQualityDesc = getAirQualityDescription(airQuality)
                    val airQualityColor = getAirQualityColor(airQuality)
                    Text(
                        text = "🌬️ Air Quality: $airQualityDesc (Index: $airQuality)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = airQualityColor
                    )
                }
                
                // Show biking reason only for today
                if (bikingReason != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 ${bikingReason}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1976D2),
                        lineHeight = 18.sp
                    )
                }
                
                
            }
            
            // Cycling score indicator with ring
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Ring progress indicator
                    CircularRingProgress(
                        progress = cyclingScore / 100f,
                        color = accentColor,
                        modifier = Modifier.size(100.dp)
                    )
                    
                    // Center content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$cyclingScore%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                        Text(
                            text = "biking",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
            }
            
        }
    }
}

@Composable
fun CircularRingProgress(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 8f
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = (minOf(canvasWidth, canvasHeight) / 2) - strokeWidth
        val center = androidx.compose.ui.geometry.Offset(canvasWidth / 2, canvasHeight / 2)
        
        // Background circle
        drawCircle(
            color = Color.Gray.copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Progress arc
        val sweepAngle = 360 * progress
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - radius,
                center.y - radius
            ),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
    }
}

fun formatDateToDayName(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        val today = LocalDate.now()
        
        when {
            date == today -> "Today"
            date == today.plusDays(1) -> "Tomorrow"
            else -> date.format(DateTimeFormatter.ofPattern("EEEE"))
        }
    } catch (e: Exception) {
        ""
    }
}

fun generateBikingReason(
    temperatureMax: Double,
    temperatureMin: Double,
    precipitationProbability: Int,
    windSpeed: Double,
    cyclingScore: Int
): String {
    val avgTemp = (temperatureMax + temperatureMin) / 2
    
    // Determine the main factors affecting the score
    val tempFactor = when {
        avgTemp in 15.0..25.0 -> "ideal temperature"
        avgTemp in 10.0..30.0 -> "comfortable temperature"
        avgTemp in 5.0..35.0 -> "moderate temperature"
        avgTemp in 0.0..40.0 -> "extreme temperature"
        else -> "dangerous temperature"
    }
    
    val rainFactor = when {
        precipitationProbability == 0 -> "no rain"
        precipitationProbability <= 20 -> "low rain chance"
        precipitationProbability <= 40 -> "moderate rain chance"
        precipitationProbability <= 60 -> "high rain chance"
        precipitationProbability <= 80 -> "very high rain chance"
        else -> "heavy rain expected"
    }
    
    val windFactor = when {
        windSpeed <= 15.0 -> "gentle winds"
        windSpeed <= 25.0 -> "moderate winds"
        windSpeed <= 35.0 -> "strong winds"
        windSpeed <= 50.0 -> "very strong winds"
        else -> "dangerous winds"
    }
    
    return when {
        cyclingScore >= 90 -> "Excellent cycling conditions with $tempFactor, $rainFactor, and $windFactor."
        cyclingScore >= 80 -> "Very good cycling conditions with $tempFactor, $rainFactor, and $windFactor."
        cyclingScore >= 70 -> "Good cycling conditions with $tempFactor, $rainFactor, and $windFactor."
        cyclingScore >= 50 -> "Fair cycling conditions due to $tempFactor, $rainFactor, and $windFactor."
        cyclingScore >= 30 -> "Poor cycling conditions due to $tempFactor, $rainFactor, and $windFactor."
        else -> "Very poor cycling conditions due to $tempFactor, $rainFactor, and $windFactor."
    }
}

fun getAirQualityDescription(index: Int): String {
    return when {
        index <= 1 -> "Good"
        index <= 2 -> "Fair"
        index <= 3 -> "Moderate"
        index <= 4 -> "Poor"
        index <= 5 -> "Very Poor"
        else -> "Hazardous"
    }
}

fun getAirQualityColor(index: Int): Color {
    return when {
        index <= 1 -> Color(0xFF4CAF50) // Green
        index <= 2 -> Color(0xFF8BC34A) // Light Green
        index <= 3 -> Color(0xFFFFEB3B) // Yellow
        index <= 4 -> Color(0xFFFF9800) // Orange
        index <= 5 -> Color(0xFFF44336) // Red
        else -> Color(0xFF9C27B0) // Purple
    }
}

fun formatHourlyTime(timeString: String): String {
    return try {
        val time = LocalTime.parse(timeString.substring(11, 16)) // Extract time part
        time.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        timeString.substring(11, 16)
    }
}

@Composable
fun HourlyWeatherCard(
    time: String,
    temperature: Double,
    apparentTemperature: Double,
    rainChance: Int,
    windSpeed: Double
) {
    Card(
        modifier = Modifier
            .width(90.dp)
            .height(90.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = time,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${String.format("%.0f", temperature)}°",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242)
            )
            Text(
                text = "Feels ${String.format("%.0f", apparentTemperature)}°",
                fontSize = 8.sp,
                color = Color(0xFF757575)
            )
            if (rainChance > 0) {
                Text(
                    text = "☔ $rainChance%",
                    fontSize = 8.sp,
                    color = Color(0xFF1976D2)
                )
            }
            if (windSpeed > 15) {
                Text(
                    text = "💨 ${String.format("%.0f", windSpeed)}",
                    fontSize = 8.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}