package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @Json(name = "results") val results: List<GeocodingCityDto>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingCityDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "country") val country: String? = null,
    @Json(name = "country_code") val countryCode: String? = null,
    @Json(name = "admin1") val admin1: String? = null,
    @Json(name = "timezone") val timezone: String? = null
)

@JsonClass(generateAdapter = true)
data class ForecastResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "timezone") val timezone: String? = null,
    @Json(name = "current") val current: CurrentWeatherDto,
    @Json(name = "hourly") val hourly: HourlyWeatherDto? = null,
    @Json(name = "daily") val daily: DailyWeatherDto? = null
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @Json(name = "time") val time: String,
    @Json(name = "temperature_2m") val temperature2m: Double,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int,
    @Json(name = "apparent_temperature") val apparentTemperature: Double,
    @Json(name = "is_day") val isDay: Int = 1,
    @Json(name = "precipitation") val precipitation: Double = 0.0,
    @Json(name = "weather_code") val weatherCode: Int,
    @Json(name = "surface_pressure") val surfacePressure: Double = 1013.0,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double,
    @Json(name = "wind_direction_10m") val windDirection10m: Int = 0
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherDto(
    @Json(name = "time") val time: List<String>,
    @Json(name = "temperature_2m") val temperature2m: List<Double>,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
    @Json(name = "time") val time: List<String>,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>? = null,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = null
)

enum class WeatherVisualType {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY,
    OVERCAST,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDERSTORM
}

data class HourlyForecastItem(
    val formattedHour: String,
    val temperatureCelsius: Int,
    val weatherCode: Int,
    val conditionLabel: String,
    val visualType: WeatherVisualType,
    val precipitationProb: Int
)

data class DailyForecastItem(
    val dayLabel: String,
    val dateLabel: String,
    val maxTempCelsius: Int,
    val minTempCelsius: Int,
    val weatherCode: Int,
    val conditionLabel: String,
    val visualType: WeatherVisualType,
    val precipitationProb: Int
)

data class CitySuggestion(
    val name: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
) {
    val displayTitle: String
        get() = buildString {
            append(name)
            if (region.isNotBlank() && !region.equals(name, ignoreCase = true)) {
                append(", ")
                append(region)
            }
            if (country.isNotBlank()) {
                append(", ")
                append(country)
            }
        }
}

data class WeatherReport(
    val cityName: String,
    val regionAndCountry: String,
    val latitude: Double,
    val longitude: Double,
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val highCelsius: Double,
    val lowCelsius: Double,
    val conditionText: String,
    val conditionSubtitle: String,
    val weatherCode: Int,
    val isDay: Boolean,
    val visualType: WeatherVisualType,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirectionLabel: String,
    val pressureHpa: Int,
    val uvIndex: Double,
    val precipitationMm: Double,
    val observationTime: String,
    val hourlyForecast: List<HourlyForecastItem>,
    val dailyForecast: List<DailyForecastItem>
)

object WeatherCodeMapper {
    fun getConditionText(code: Int): String = when (code) {
        0 -> "Clear Sky"
        1 -> "Mainly Clear"
        2 -> "Partly Cloudy"
        3 -> "Overcast"
        45, 48 -> "Misty Fog"
        51, 53, 55 -> "Light Drizzle"
        56, 57 -> "Freezing Drizzle"
        61 -> "Slight Rain"
        63 -> "Moderate Rain"
        65 -> "Heavy Rain"
        66, 67 -> "Freezing Rain"
        71, 73, 75, 77 -> "Snowfall"
        80, 81, 82 -> "Rain Showers"
        85, 86 -> "Snow Showers"
        95 -> "Thunderstorm"
        96, 99 -> "Severe Thunderstorm"
        else -> "Fair Skies"
    }

    fun getConditionSubtitle(code: Int, isDay: Boolean): String = when (code) {
        0 -> if (isDay) "Abundant sunshine and crisp visibility" else "Starlit skies and calm evening air"
        1, 2 -> if (isDay) "Gentle scattered clouds across the horizon" else "Partly veiled night sky"
        3 -> "Full cloud cover creating diffused atmospheric light"
        45, 48 -> "Low atmospheric mist reducing distant visibility"
        51, 53, 55, 56, 57 -> "Fine misting rain droplets in the air"
        61, 63, 65, 66, 67, 80, 81, 82 -> "Active rainfall across the region — carry an umbrella"
        71, 73, 75, 77, 85, 86 -> "Wintry snow flurries and crisp sub-zero air"
        95, 96, 99 -> "Electrical storm activity with gusty winds"
        else -> "Live atmospheric telemetry"
    }

    fun getVisualType(code: Int, isDay: Boolean = true): WeatherVisualType = when (code) {
        0, 1 -> if (isDay) WeatherVisualType.CLEAR_DAY else WeatherVisualType.CLEAR_NIGHT
        2 -> WeatherVisualType.PARTLY_CLOUDY
        3 -> WeatherVisualType.OVERCAST
        45, 48 -> WeatherVisualType.FOG
        51, 53, 55, 56, 57 -> WeatherVisualType.DRIZZLE
        61, 63, 65, 66, 67, 80, 81, 82 -> WeatherVisualType.RAIN
        71, 73, 75, 77, 85, 86 -> WeatherVisualType.SNOW
        95, 96, 99 -> WeatherVisualType.THUNDERSTORM
        else -> if (isDay) WeatherVisualType.CLEAR_DAY else WeatherVisualType.CLEAR_NIGHT
    }

    fun getWindDirectionCardinal(degrees: Int): String {
        val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = ((degrees % 360 + 360) % 360 + 22) / 45 % 8
        return directions[index]
    }
}
