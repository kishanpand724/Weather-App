package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.CitySuggestion
import com.example.data.DailyForecastItem
import com.example.data.HourlyForecastItem
import com.example.data.WeatherReport
import com.example.ui.theme.CardGlassBorder
import com.example.ui.theme.CardGlassWhite
import com.example.ui.theme.CloudFrostWhite
import com.example.ui.theme.SkyCyanAccent
import com.example.ui.theme.SunWarmAmber
import com.example.ui.theme.TextSecondaryIce
import kotlin.math.roundToInt

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val currentVisualType = (state.weatherState as? WeatherUiState.Success)?.report?.visualType
    val backgroundBrush = AtmosphericGradients.backgroundFor(currentVisualType)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isExpandedScreen = maxWidth >= 720.dp
            val contentMaxWidth = if (isExpandedScreen) 960.dp else 600.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = contentMaxWidth)
                    .padding(horizontal = 20.dp)
                    .testTag("weather_main_scroll"),
                contentPadding = PaddingValues(top = 16.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    TopAppBarHeader(
                        onRefresh = {
                            focusManager.clearFocus()
                            viewModel.refreshCurrentCity()
                        }
                    )
                }

                item {
                    SearchSection(
                        query = state.searchQuery,
                        suggestions = state.suggestions,
                        isSearchingSuggestions = state.isSearchingSuggestions,
                        onQueryChange = viewModel::onSearchQueryChange,
                        onClearQuery = viewModel::clearSearchQuery,
                        onSubmitSearch = {
                            focusManager.clearFocus()
                            viewModel.submitSearch()
                        },
                        onSelectSuggestion = { suggestion ->
                            focusManager.clearFocus()
                            viewModel.selectSuggestion(suggestion)
                        }
                    )
                }

                item {
                    QuickCitiesRow(
                        cities = state.quickCities,
                        selectedCity = state.selectedCityName,
                        onSelectCity = { city ->
                            focusManager.clearFocus()
                            viewModel.searchCity(city)
                        }
                    )
                }

                when (val weatherState = state.weatherState) {
                    is WeatherUiState.Loading -> {
                        item {
                            LoadingStateCard(cityName = state.selectedCityName)
                        }
                    }

                    is WeatherUiState.Error -> {
                        item {
                            ErrorStateCard(
                                errorMessage = weatherState.message,
                                attemptedCity = weatherState.attemptedCity,
                                onRetry = {
                                    focusManager.clearFocus()
                                    viewModel.refreshCurrentCity()
                                }
                            )
                        }
                    }

                    is WeatherUiState.Success -> {
                        val report = weatherState.report
                        if (isExpandedScreen) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1.05f),
                                        verticalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        CurrentWeatherHeroCard(report = report)
                                        HourlyForecastCard(hourlyItems = report.hourlyForecast)
                                    }
                                    Column(
                                        modifier = Modifier.weight(0.95f),
                                        verticalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        WeatherMetricsGrid(report = report)
                                        DailyForecastCard(dailyItems = report.dailyForecast)
                                    }
                                }
                            }
                        } else {
                            item {
                                CurrentWeatherHeroCard(report = report)
                            }
                            item {
                                WeatherMetricsGrid(report = report)
                            }
                            if (report.hourlyForecast.isNotEmpty()) {
                                item {
                                    HourlyForecastCard(hourlyItems = report.hourlyForecast)
                                }
                            }
                            if (report.dailyForecast.isNotEmpty()) {
                                item {
                                    DailyForecastCard(dailyItems = report.dailyForecast)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopAppBarHeader(
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = CloudFrostWhite
            )
            Text(
                text = stringResource(id = R.string.updated_just_now),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondaryIce
            )
        }

        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .background(CardGlassWhite)
                .border(1.dp, CardGlassBorder, CircleShape)
                .testTag("refresh_weather_button")
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = stringResource(id = R.string.refresh_weather_desc),
                tint = CloudFrostWhite
            )
        }
    }
}

@Composable
private fun SearchSection(
    query: String,
    suggestions: List<CitySuggestion>,
    isSearchingSuggestions: Boolean,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSubmitSearch: () -> Unit,
    onSelectSuggestion: (CitySuggestion) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("city_search_input"),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryIce.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = SkyCyanAccent
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = onClearQuery,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(id = R.string.clear_search_desc),
                                tint = TextSecondaryIce
                            )
                        }
                    } else if (isSearchingSuggestions) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = SkyCyanAccent
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmitSearch() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CloudFrostWhite,
                    unfocusedTextColor = CloudFrostWhite,
                    focusedContainerColor = Color(0x28FFFFFF),
                    unfocusedContainerColor = Color(0x1CFFFFFF),
                    focusedBorderColor = SkyCyanAccent,
                    unfocusedBorderColor = CardGlassBorder,
                    cursorColor = SkyCyanAccent
                )
            )

            Button(
                onClick = onSubmitSearch,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("search_submit_button"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkyCyanAccent,
                    contentColor = Color(0xFF082F49)
                ),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .border(1.dp, CardGlassBorder, RoundedCornerShape(20.dp))
                    .testTag("city_suggestions_dropdown"),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xEB0F172A),
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    suggestions.forEachIndexed { index, suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSuggestion(suggestion) }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("suggestion_item_${index}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = SkyCyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = suggestion.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CloudFrostWhite
                                )
                                val sub = listOf(suggestion.region, suggestion.country)
                                    .filter { it.isNotBlank() }
                                    .joinToString(", ")
                                if (sub.isNotBlank()) {
                                    Text(
                                        text = sub,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondaryIce
                                    )
                                }
                            }
                        }
                        if (index < suggestions.lastIndex) {
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickCitiesRow(
    cities: List<String>,
    selectedCity: String,
    onSelectCity: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_cities_row"),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(cities, key = { it }) { city ->
            val isSelected = city.equals(selectedCity, ignoreCase = true)
            val bgColor = if (isSelected) SkyCyanAccent else CardGlassWhite
            val textColor = if (isSelected) Color(0xFF082F49) else CloudFrostWhite
            val borderColor = if (isSelected) SkyCyanAccent else CardGlassBorder

            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(50))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(50))
                    .clickable { onSelectCity(city) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
                    .testTag("quick_city_chip_${city.lowercase().replace(" ", "_")}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = city,
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CurrentWeatherHeroCard(
    report: WeatherReport
) {
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            Color(0x33FFFFFF),
            Color(0x14FFFFFF)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(brush = cardGradient)
            .border(1.dp, CardGlassBorder, RoundedCornerShape(32.dp))
            .padding(24.dp)
            .testTag("current_weather_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Location badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x26000000))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = SkyCyanAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = report.cityName,
                    style = MaterialTheme.typography.titleMedium,
                    color = CloudFrostWhite,
                    modifier = Modifier.testTag("city_name_text")
                )
                if (report.regionAndCountry.isNotBlank()) {
                    Text(
                        text = " • ${report.regionAndCountry}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryIce,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weather Icon + Giant Celsius Temperature side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WeatherArtIllustration(
                    visualType = report.visualType,
                    contentDesc = report.conditionText,
                    modifier = Modifier
                        .size(118.dp)
                        .testTag("weather_condition_icon")
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "${report.temperatureCelsius.roundToInt()}°C",
                        style = MaterialTheme.typography.displayLarge,
                        color = CloudFrostWhite,
                        modifier = Modifier.testTag("current_temperature_text")
                    )
                    Text(
                        text = report.conditionText,
                        style = MaterialTheme.typography.titleLarge,
                        color = SunWarmAmber,
                        modifier = Modifier.testTag("weather_condition_text")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = report.conditionSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryIce,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // High / Low / Feels Like pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x22FFFFFF))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        id = R.string.high_low_format,
                        report.highCelsius.roundToInt().toString(),
                        report.lowCelsius.roundToInt().toString()
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = CloudFrostWhite
                )
                Text(
                    text = "|",
                    color = CardGlassBorder
                )
                Text(
                    text = "Feels like ${report.feelsLikeCelsius.roundToInt()}°C",
                    style = MaterialTheme.typography.labelLarge,
                    color = SkyCyanAccent
                )
            }
        }
    }
}

@Composable
private fun WeatherMetricsGrid(
    report: WeatherReport
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weather_metrics_grid"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MetricGlassCard(
                icon = Icons.Filled.WaterDrop,
                iconTint = SkyCyanAccent,
                label = stringResource(id = R.string.metric_humidity),
                value = "${report.humidityPercent}%",
                subtitle = when {
                    report.humidityPercent >= 75 -> "Humid air"
                    report.humidityPercent <= 35 -> "Dry comfort"
                    else -> "Optimal comfort"
                },
                testTag = "metric_humidity_card",
                modifier = Modifier.weight(1f)
            )

            MetricGlassCard(
                icon = Icons.Filled.Air,
                iconTint = Color(0xFF67E8F9),
                label = stringResource(id = R.string.metric_wind_speed),
                value = "${report.windSpeedKmh.roundToInt()} km/h",
                subtitle = "Direction: ${report.windDirectionLabel}",
                testTag = "metric_wind_card",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MetricGlassCard(
                icon = Icons.Filled.Thermostat,
                iconTint = SunWarmAmber,
                label = stringResource(id = R.string.metric_feels_like),
                value = "${report.feelsLikeCelsius.roundToInt()}°C",
                subtitle = "Apparent temp",
                testTag = "metric_feels_like_card",
                modifier = Modifier.weight(1f)
            )

            MetricGlassCard(
                icon = Icons.Filled.WbSunny,
                iconTint = Color(0xFFFDE047),
                label = stringResource(id = R.string.metric_uv_index),
                value = String.format(java.util.Locale.US, "%.1f", report.uvIndex),
                subtitle = when {
                    report.uvIndex >= 8 -> "Very High UV"
                    report.uvIndex >= 6 -> "High UV"
                    report.uvIndex >= 3 -> "Moderate UV"
                    else -> "Low UV"
                },
                testTag = "metric_uv_card",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MetricGlassCard(
                icon = Icons.Filled.Compress,
                iconTint = Color(0xFFA78BFA),
                label = stringResource(id = R.string.metric_pressure),
                value = "${report.pressureHpa} hPa",
                subtitle = "Surface pressure",
                testTag = "metric_pressure_card",
                modifier = Modifier.weight(1f)
            )

            MetricGlassCard(
                icon = Icons.Filled.WaterDrop,
                iconTint = Color(0xFF38BDF8),
                label = stringResource(id = R.string.metric_precipitation),
                value = "${report.precipitationMm} mm",
                subtitle = "Current rainfall",
                testTag = "metric_precipitation_card",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricGlassCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    subtitle: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(CardGlassWhite)
            .border(1.dp, CardGlassBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondaryIce
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = CloudFrostWhite
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondaryIce.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun HourlyForecastCard(
    hourlyItems: List<HourlyForecastItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(CardGlassWhite)
            .border(1.dp, CardGlassBorder, RoundedCornerShape(28.dp))
            .padding(vertical = 20.dp)
            .testTag("hourly_forecast_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(id = R.string.section_hourly_forecast),
            style = MaterialTheme.typography.titleMedium,
            color = CloudFrostWhite,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(hourlyItems) { item ->
                val isNow = item.formattedHour == "Now"
                Column(
                    modifier = Modifier
                        .width(78.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isNow) SkyCyanAccent.copy(alpha = 0.24f)
                            else Color(0x18FFFFFF)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isNow) SkyCyanAccent else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.formattedHour,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isNow) SkyCyanAccent else TextSecondaryIce
                    )

                    WeatherArtIllustration(
                        visualType = item.visualType,
                        contentDesc = item.conditionLabel,
                        animate = false,
                        modifier = Modifier.size(38.dp)
                    )

                    Text(
                        text = "${item.temperatureCelsius}°C",
                        style = MaterialTheme.typography.titleMedium,
                        color = CloudFrostWhite
                    )

                    if (item.precipitationProb > 0) {
                        Text(
                            text = "${item.precipitationProb}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = SkyCyanAccent
                        )
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyForecastCard(
    dailyItems: List<DailyForecastItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(CardGlassWhite)
            .border(1.dp, CardGlassBorder, RoundedCornerShape(28.dp))
            .padding(20.dp)
            .testTag("daily_forecast_card"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(id = R.string.section_daily_forecast),
            style = MaterialTheme.typography.titleMedium,
            color = CloudFrostWhite
        )

        dailyItems.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.1f)) {
                    Text(
                        text = item.dayLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = CloudFrostWhite
                    )
                    Text(
                        text = item.dateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondaryIce
                    )
                }

                Row(
                    modifier = Modifier.weight(1.3f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WeatherArtIllustration(
                        visualType = item.visualType,
                        contentDesc = item.conditionLabel,
                        animate = false,
                        modifier = Modifier.size(34.dp)
                    )
                    Column {
                        Text(
                            text = item.conditionLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CloudFrostWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.precipitationProb > 0) {
                            Text(
                                text = "${item.precipitationProb}% rain",
                                style = MaterialTheme.typography.labelMedium,
                                color = SkyCyanAccent
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.weight(0.9f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.maxTempCelsius}°C",
                        style = MaterialTheme.typography.titleMedium,
                        color = CloudFrostWhite
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${item.minTempCelsius}°C",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryIce
                    )
                }
            }

            if (index < dailyItems.lastIndex) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            }
        }
    }
}

@Composable
private fun LoadingStateCard(
    cityName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(CardGlassWhite)
            .border(1.dp, CardGlassBorder, RoundedCornerShape(28.dp))
            .padding(vertical = 56.dp, horizontal = 24.dp)
            .testTag("weather_loading_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        CircularProgressIndicator(
            color = SkyCyanAccent,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = stringResource(id = R.string.loading_weather),
            style = MaterialTheme.typography.titleMedium,
            color = CloudFrostWhite,
            textAlign = TextAlign.Center
        )
        if (cityName.isNotBlank()) {
            Text(
                text = "Locating $cityName…",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryIce
            )
        }
    }
}

@Composable
private fun ErrorStateCard(
    errorMessage: String,
    attemptedCity: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(CardGlassWhite)
            .border(1.dp, CardGlassBorder, RoundedCornerShape(28.dp))
            .padding(vertical = 40.dp, horizontal = 24.dp)
            .testTag("weather_error_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0x33FB7185)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CloudOff,
                contentDescription = stringResource(id = R.string.error_title),
                tint = Color(0xFFFB7185),
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = stringResource(id = R.string.error_title),
            style = MaterialTheme.typography.titleLarge,
            color = CloudFrostWhite,
            textAlign = TextAlign.Center
        )

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryIce,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SkyCyanAccent,
                contentColor = Color(0xFF082F49)
            ),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("error_retry_button")
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (attemptedCity.isNotBlank()) "Retry \"$attemptedCity\"" else stringResource(id = R.string.retry_button),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
