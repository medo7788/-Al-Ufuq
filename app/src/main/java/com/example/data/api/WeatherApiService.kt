package com.example.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val current_weather: CurrentWeatherDto? = null
)

data class CurrentWeatherDto(
    val temperature: Double = 25.0,
    val windspeed: Double = 12.0,
    val weathercode: Int = 0,
    val is_day: Int = 1,
    val time: String = ""
)

data class WeatherInfo(
    val temperatureCelcius: Int = 26,
    val conditionArabic: String = "مشمس وصافٍ",
    val iconEmoji: String = "☀️",
    val windSpeedKm: Int = 12,
    val isDay: Boolean = true
) {
    companion object {
        fun fromDto(dto: CurrentWeatherDto?): WeatherInfo {
            if (dto == null) {
                return WeatherInfo()
            }
            val temp = dto.temperature.toInt()
            val wind = dto.windspeed.toInt()
            val isDay = dto.is_day == 1
            val (desc, icon) = when (dto.weathercode) {
                0 -> if (isDay) "صافٍ ومشمش" to "☀️" else "صافٍ ولطيف" to "🌙"
                1, 2 -> "غائم جزئياً" to "🌤️"
                3 -> "غائم" to "☁️"
                45, 48 -> "ضباب خفيف" to "🌫️"
                51, 53, 55, 61, 63, 65 -> "أمطار خير" to "🌧️"
                71, 73, 75 -> "تساقط ثلوج" to "❄️"
                80, 81, 82 -> "زخات مطر" to "🌦️"
                95, 96, 99 -> "عاصفة رعدية" to "⛈️"
                else -> "معتدل وصافٍ" to "🌤️"
            }
            return WeatherInfo(temp, desc, icon, wind, isDay)
        }
    }
}

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): Response<WeatherResponse>
}
