package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

class WeatherRepository(
    private val geocodingService: GeocodingApiService = NetworkModule.geocodingService,
    private val forecastService: OpenMeteoForecastService = NetworkModule.forecastService
) {

    suspend fun searchCitySuggestions(query: String): Result<List<CitySuggestion>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val trimmed = query.trim()
                if (trimmed.isEmpty()) return@runCatching emptyList()
                val response = geocodingService.searchCity(name = trimmed, count = 6)
                response.results.orEmpty().map { dto ->
                    CitySuggestion(
                        name = dto.name,
                        region = dto.admin1.orEmpty(),
                        country = dto.country.orEmpty(),
                        latitude = dto.latitude,
                        longitude = dto.longitude
                    )
                }
            }
        }

    suspend fun fetchWeatherForCityName(cityQuery: String): Result<WeatherReport> =
        withContext(Dispatchers.IO) {
            runCatching {
                val trimmed = cityQuery.trim()
                require(trimmed.isNotEmpty()) { "Please enter a city name to search." }

                val geoResponse = geocodingService.searchCity(name = trimmed, count = 1)
                val matchedCity = geoResponse.results?.firstOrNull()
                    ?: throw NoSuchElementException("Could not find any city matching \"$trimmed\". Please check the spelling and try again.")

                val regionAndCountry = buildString {
                    if (!matchedCity.admin1.isNullOrBlank() &&
                        !matchedCity.admin1.equals(matchedCity.name, ignoreCase = true)
                    ) {
                        append(matchedCity.admin1)
                    }
                    if (!matchedCity.country.isNullOrBlank()) {
                        if (isNotEmpty()) append(", ")
                        append(matchedCity.country)
                    }
                }.ifBlank { "Global Coordinates" }

                fetchWeatherByCoordinatesInternal(
                    cityName = matchedCity.name,
                    regionAndCountry = regionAndCountry,
                    latitude = matchedCity.latitude,
                    longitude = matchedCity.longitude
                )
            }
        }

    suspend fun fetchWeatherForSuggestion(suggestion: CitySuggestion): Result<WeatherReport> =
        withContext(Dispatchers.IO) {
            runCatching {
                val regionAndCountry = buildString {
                    if (suggestion.region.isNotBlank() &&
                        !suggestion.region.equals(suggestion.name, ignoreCase = true)
                    ) {
                        append(suggestion.region)
                    }
                    if (suggestion.country.isNotBlank()) {
                        if (isNotEmpty()) append(", ")
                        append(suggestion.country)
                    }
                }.ifBlank { "Global Coordinates" }

                fetchWeatherByCoordinatesInternal(
                    cityName = suggestion.name,
                    regionAndCountry = regionAndCountry,
                    latitude = suggestion.latitude,
                    longitude = suggestion.longitude
                )
            }
        }

    private suspend fun fetchWeatherByCoordinatesInternal(
        cityName: String,
        regionAndCountry: String,
        latitude: Double,
        longitude: Double
    ): WeatherReport {
        val forecast = forecastService.getWeatherForecast(
            latitude = latitude,
            longitude = longitude
        )
        val current = forecast.current
        val isDay = current.isDay == 1
        val currentCode = current.weatherCode

        val daily = forecast.daily
        val todayHigh = daily?.temperature2mMax?.firstOrNull() ?: (current.temperature2m + 3.0)
        val todayLow = daily?.temperature2mMin?.firstOrNull() ?: (current.temperature2m - 4.0)
        val todayUv = daily?.uvIndexMax?.firstOrNull() ?: 0.0

        val currentIsoTime = current.time // e.g., "2026-10-19T14:15"
        val currentHourPrefix = currentIsoTime.take(13) // "2026-10-19T14"

        val hourlyList = mutableListOf<HourlyForecastItem>()
        val hourlyDto = forecast.hourly
        if (hourlyDto != null && hourlyDto.time.isNotEmpty()) {
            val startIndex = hourlyDto.time.indexOfFirst { it.startsWith(currentHourPrefix) }
                .let { if (it == -1) 0 else it }
            val endIndex = (startIndex + 24).coerceAtMost(hourlyDto.time.size)

            for (i in startIndex until endIndex) {
                val rawTime = hourlyDto.time.getOrNull(i) ?: continue
                val temp = hourlyDto.temperature2m.getOrNull(i) ?: current.temperature2m
                val code = hourlyDto.weatherCode.getOrNull(i) ?: currentCode
                val precipProb = hourlyDto.precipitationProbability?.getOrNull(i) ?: 0
                val formattedHour = if (i == startIndex) {
                    "Now"
                } else {
                    rawTime.substringAfter("T", "00:00").take(5)
                }
                val hourInt = rawTime.substringAfter("T", "12:00").take(2).toIntOrNull() ?: 12
                val hourIsDay = hourInt in 6..19

                hourlyList.add(
                    HourlyForecastItem(
                        formattedHour = formattedHour,
                        temperatureCelsius = temp.roundToInt(),
                        weatherCode = code,
                        conditionLabel = WeatherCodeMapper.getConditionText(code),
                        visualType = WeatherCodeMapper.getVisualType(code, hourIsDay),
                        precipitationProb = precipProb
                    )
                )
            }
        }

        val dailyList = mutableListOf<DailyForecastItem>()
        if (daily != null && daily.time.isNotEmpty()) {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dayFormat = SimpleDateFormat("EEE", Locale.US)
            val dateFormat = SimpleDateFormat("MMM d", Locale.US)

            val count = daily.time.size.coerceAtMost(7)
            for (i in 0 until count) {
                val rawDate = daily.time[i]
                val parsedDate = runCatching { inputFormat.parse(rawDate) }.getOrNull()
                val dayLabel = if (i == 0) {
                    "Today"
                } else if (parsedDate != null) {
                    dayFormat.format(parsedDate)
                } else {
                    rawDate
                }
                val dateLabel = if (parsedDate != null) {
                    dateFormat.format(parsedDate)
                } else {
                    rawDate
                }

                val code = daily.weatherCode.getOrNull(i) ?: currentCode
                val maxT = daily.temperature2mMax.getOrNull(i) ?: todayHigh
                val minT = daily.temperature2mMin.getOrNull(i) ?: todayLow
                val precipProb = daily.precipitationProbabilityMax?.getOrNull(i) ?: 0

                dailyList.add(
                    DailyForecastItem(
                        dayLabel = dayLabel,
                        dateLabel = dateLabel,
                        maxTempCelsius = maxT.roundToInt(),
                        minTempCelsius = minT.roundToInt(),
                        weatherCode = code,
                        conditionLabel = WeatherCodeMapper.getConditionText(code),
                        visualType = WeatherCodeMapper.getVisualType(code, true),
                        precipitationProb = precipProb
                    )
                )
            }
        }

        val formattedObsTime = current.time.substringAfter("T", current.time)

        return WeatherReport(
            cityName = cityName,
            regionAndCountry = regionAndCountry,
            latitude = latitude,
            longitude = longitude,
            temperatureCelsius = current.temperature2m,
            feelsLikeCelsius = current.apparentTemperature,
            highCelsius = todayHigh,
            lowCelsius = todayLow,
            conditionText = WeatherCodeMapper.getConditionText(currentCode),
            conditionSubtitle = WeatherCodeMapper.getConditionSubtitle(currentCode, isDay),
            weatherCode = currentCode,
            isDay = isDay,
            visualType = WeatherCodeMapper.getVisualType(currentCode, isDay),
            humidityPercent = current.relativeHumidity2m,
            windSpeedKmh = current.windSpeed10m,
            windDirectionLabel = WeatherCodeMapper.getWindDirectionCardinal(current.windDirection10m),
            pressureHpa = current.surfacePressure.roundToInt(),
            uvIndex = todayUv,
            precipitationMm = current.precipitation,
            observationTime = formattedObsTime,
            hourlyForecast = hourlyList,
            dailyForecast = dailyList
        )
    }
}
