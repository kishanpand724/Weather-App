package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CitySuggestion
import com.example.data.WeatherReport
import com.example.data.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val report: WeatherReport) : WeatherUiState
    data class Error(val message: String, val attemptedCity: String) : WeatherUiState
}

data class WeatherScreenState(
    val searchQuery: String = "",
    val suggestions: List<CitySuggestion> = emptyList(),
    val isSearchingSuggestions: Boolean = false,
    val quickCities: List<String> = listOf(
        "Tokyo",
        "London",
        "New York",
        "Paris",
        "Sydney",
        "Mumbai",
        "Toronto",
        "Singapore"
    ),
    val selectedCityName: String = "Tokyo",
    val weatherState: WeatherUiState = WeatherUiState.Loading
)

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _screenState = MutableStateFlow(WeatherScreenState())
    val screenState: StateFlow<WeatherScreenState> = _screenState.asStateFlow()

    private var suggestionJob: Job? = null
    private var weatherFetchJob: Job? = null

    init {
        searchCity("Tokyo")
    }

    fun onSearchQueryChange(newQuery: String) {
        _screenState.update {
            it.copy(searchQuery = newQuery)
        }
        suggestionJob?.cancel()
        val trimmed = newQuery.trim()
        if (trimmed.length < 2) {
            _screenState.update {
                it.copy(suggestions = emptyList(), isSearchingSuggestions = false)
            }
            return
        }

        suggestionJob = viewModelScope.launch {
            delay(280)
            _screenState.update { it.copy(isSearchingSuggestions = true) }
            val result = repository.searchCitySuggestions(trimmed)
            result.onSuccess { list ->
                _screenState.update {
                    it.copy(suggestions = list, isSearchingSuggestions = false)
                }
            }.onFailure {
                _screenState.update {
                    it.copy(suggestions = emptyList(), isSearchingSuggestions = false)
                }
            }
        }
    }

    fun clearSearchQuery() {
        suggestionJob?.cancel()
        _screenState.update {
            it.copy(
                searchQuery = "",
                suggestions = emptyList(),
                isSearchingSuggestions = false
            )
        }
    }

    fun dismissSuggestions() {
        suggestionJob?.cancel()
        _screenState.update {
            it.copy(suggestions = emptyList(), isSearchingSuggestions = false)
        }
    }

    fun submitSearch() {
        val query = _screenState.value.searchQuery.trim()
        if (query.isNotEmpty()) {
            searchCity(query)
        }
    }

    fun searchCity(cityName: String) {
        val trimmed = cityName.trim()
        if (trimmed.isEmpty()) return

        suggestionJob?.cancel()
        weatherFetchJob?.cancel()

        _screenState.update { state ->
            val updatedQuickCities = if (state.quickCities.any { it.equals(trimmed, ignoreCase = true) }) {
                state.quickCities
            } else {
                (listOf(trimmed) + state.quickCities).take(8)
            }
            state.copy(
                selectedCityName = trimmed,
                suggestions = emptyList(),
                isSearchingSuggestions = false,
                quickCities = updatedQuickCities,
                weatherState = WeatherUiState.Loading
            )
        }

        weatherFetchJob = viewModelScope.launch {
            val result = repository.fetchWeatherForCityName(trimmed)
            result.onSuccess { report ->
                _screenState.update { state ->
                    val canonicalCities = if (state.quickCities.any { it.equals(report.cityName, ignoreCase = true) }) {
                        state.quickCities
                    } else {
                        (listOf(report.cityName) + state.quickCities.filterNot { it.equals(trimmed, ignoreCase = true) }).take(8)
                    }
                    state.copy(
                        selectedCityName = report.cityName,
                        quickCities = canonicalCities,
                        weatherState = WeatherUiState.Success(report)
                    )
                }
            }.onFailure { error ->
                val friendlyMessage = when (error) {
                    is NoSuchElementException -> error.message ?: "City not found. Try searching another city."
                    else -> "Could not connect to the live weather service. Please check your internet connection and try again."
                }
                _screenState.update {
                    it.copy(
                        weatherState = WeatherUiState.Error(
                            message = friendlyMessage,
                            attemptedCity = trimmed
                        )
                    )
                }
            }
        }
    }

    fun selectSuggestion(suggestion: CitySuggestion) {
        suggestionJob?.cancel()
        weatherFetchJob?.cancel()

        _screenState.update { state ->
            val updatedQuickCities = if (state.quickCities.any { it.equals(suggestion.name, ignoreCase = true) }) {
                state.quickCities
            } else {
                (listOf(suggestion.name) + state.quickCities).take(8)
            }
            state.copy(
                searchQuery = suggestion.name,
                selectedCityName = suggestion.name,
                suggestions = emptyList(),
                isSearchingSuggestions = false,
                quickCities = updatedQuickCities,
                weatherState = WeatherUiState.Loading
            )
        }

        weatherFetchJob = viewModelScope.launch {
            val result = repository.fetchWeatherForSuggestion(suggestion)
            result.onSuccess { report ->
                _screenState.update {
                    it.copy(
                        selectedCityName = report.cityName,
                        weatherState = WeatherUiState.Success(report)
                    )
                }
            }.onFailure {
                _screenState.update {
                    it.copy(
                        weatherState = WeatherUiState.Error(
                            message = "Unable to load live weather for ${suggestion.name}. Please try again.",
                            attemptedCity = suggestion.name
                        )
                    )
                }
            }
        }
    }

    fun refreshCurrentCity() {
        val currentTarget = when (val ws = _screenState.value.weatherState) {
            is WeatherUiState.Success -> ws.report.cityName
            is WeatherUiState.Error -> ws.attemptedCity
            WeatherUiState.Loading -> _screenState.value.selectedCityName
        }
        searchCity(currentTarget.ifBlank { "Tokyo" })
    }
}
