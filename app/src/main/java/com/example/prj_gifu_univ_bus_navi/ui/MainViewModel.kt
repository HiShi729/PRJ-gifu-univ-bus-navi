package com.example.prj_gifu_univ_bus_navi.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prj_gifu_univ_bus_navi.data.LocalBusStopData
import com.example.prj_gifu_univ_bus_navi.data.LocalCampusGraphData
import com.example.prj_gifu_univ_bus_navi.data.LocalSchoolHolidayData
import com.example.prj_gifu_univ_bus_navi.data.UserSettingsRepository
import com.example.prj_gifu_univ_bus_navi.data.WeatherRepository
import com.example.prj_gifu_univ_bus_navi.logic.BusRecommendationEngine
import com.example.prj_gifu_univ_bus_navi.logic.CampusGraphBuilder
import com.example.prj_gifu_univ_bus_navi.model.BusTrip
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode
import com.example.prj_gifu_univ_bus_navi.model.RecommendationResult
import com.example.prj_gifu_univ_bus_navi.model.UserEdgeOverride
import com.example.prj_gifu_univ_bus_navi.model.UserGraphNodeInput
import com.example.prj_gifu_univ_bus_navi.model.UserTravelTimeProfile
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    RESULT,
    SETTINGS,
    ADD_NODE,
    EDIT_TRAVEL_TIME,
    TRAVEL_TIME_PROFILE,
}

data class SafetyMarginOption(
    val label: String,
    val minutes: Int,
)

class MainViewModel : ViewModel() {
    private val safetyOptions = listOf(
        SafetyMarginOption("急ぐ: 0分", 0),
        SafetyMarginOption("標準: 1分", 1),
        SafetyMarginOption("安全: 3分", 3),
    )

    var currentScreen by mutableStateOf(AppScreen.HOME)
        private set
    var selectedCurrentNodeId by mutableStateOf(LocalCampusGraphData.nodes.first { it.isSelectableAsStart }.id)
        private set
    var selectedDestinationStopName by mutableStateOf("JR岐阜")
        private set
    var selectedSafetyMargin by mutableStateOf(safetyOptions[1])
        private set
    var selectedMapNodeId by mutableStateOf(selectedCurrentNodeId)
        private set
    var recommendationResult by mutableStateOf<RecommendationResult?>(null)
        private set
    var currentDate by mutableStateOf(LocalDate.now())
        private set
    var currentTime by mutableStateOf(LocalTime.now().withSecond(0).withNano(0))
        private set
    var userNodeInputs by mutableStateOf<List<UserGraphNodeInput>>(emptyList())
        private set
    var userEdgeOverrides by mutableStateOf<List<UserEdgeOverride>>(emptyList())
        private set
    var userTravelTimeProfile by mutableStateOf<UserTravelTimeProfile?>(null)
        private set
    var rainModeEnabled by mutableStateOf(false)
        private set
    var favoriteStartNodeId by mutableStateOf<String?>(null)
        private set
    var rainForecastStatus by mutableStateOf("天気予報を確認中")
        private set
    private var busTrips by mutableStateOf<List<BusTrip>>(emptyList())
    var destinationStopNames by mutableStateOf<List<String>>(listOf("JR岐阜", "名鉄岐阜"))
        private set
    private var settingsRepository: UserSettingsRepository? = null
    private var weatherRepository: WeatherRepository? = null
    private var hasRequestedWeather = false

    val safetyMarginOptions: List<SafetyMarginOption> = safetyOptions

    val graphNodes: List<CampusGraphNode>
        get() = CampusGraphBuilder.buildGraph(
            LocalCampusGraphData.nodes,
            LocalCampusGraphData.edges,
            userNodeInputs,
            userEdgeOverrides,
            userTravelTimeProfile,
            rainModeEnabled,
        ).first

    val graphEdges: List<CampusGraphEdge>
        get() = CampusGraphBuilder.buildGraph(
            LocalCampusGraphData.nodes,
            LocalCampusGraphData.edges,
            userNodeInputs,
            userEdgeOverrides,
            userTravelTimeProfile,
            rainModeEnabled,
        ).second

    val selectableStartNodes: List<CampusGraphNode>
        get() = graphNodes.filter { it.isSelectableAsStart }

    val mapSelectableNodes: List<CampusGraphNode>
        get() = selectableMapNodes(graphNodes)

    val editableEdges: List<CampusGraphEdge>
        get() = LocalCampusGraphData.edges.filter { it.isSelectableForUserEdit }

    val favoriteStartNodeName: String?
        get() = favoriteStartNodeId?.let { id -> graphNodes.firstOrNull { it.id == id }?.name }

    fun loadInitialData(
        busTrips: List<BusTrip>,
        destinationStopNames: List<String>,
        repository: UserSettingsRepository,
        weatherRepository: WeatherRepository,
    ) {
        this.busTrips = busTrips
        this.destinationStopNames = destinationStopNames.ifEmpty { listOf("JR岐阜", "名鉄岐阜") }
        if (settingsRepository != null) return
        settingsRepository = repository
        this.weatherRepository = weatherRepository
        viewModelScope.launch {
            repository.settingsFlow.collect { settings ->
                selectedDestinationStopName = settings.selectedDestinationStopName
                    .takeIf { it in this@MainViewModel.destinationStopNames }
                    ?: "JR岐阜"
                selectedSafetyMargin = safetyOptions.firstOrNull { it.minutes == settings.safetyMarginMinutes } ?: selectedSafetyMargin
                rainModeEnabled = settings.rainModeEnabled
                userNodeInputs = settings.userNodeInputs
                userEdgeOverrides = settings.userEdgeOverrides
                userTravelTimeProfile = settings.userTravelTimeProfile
                favoriteStartNodeId = settings.favoriteStartNodeId
                val preferred = settings.favoriteStartNodeId?.takeIf { id -> selectableStartNodes.any { it.id == id } }
                if (preferred != null) {
                    selectedCurrentNodeId = preferred
                    selectedMapNodeId = preferred
                }
                requestRainForecastOnce()
            }
        }
    }

    fun selectCurrentNode(nodeId: String) {
        selectedCurrentNodeId = nodeId
        selectedMapNodeId = nodeId
    }

    fun selectDestinationStop(stopName: String) {
        selectedDestinationStopName = stopName
        viewModelScope.launch { settingsRepository?.saveSelectedDestinationStopName(stopName) }
    }

    fun selectSafetyMargin(option: SafetyMarginOption) {
        selectedSafetyMargin = option
        viewModelScope.launch { settingsRepository?.saveSafetyMarginMinutes(option.minutes) }
    }

    fun findBestBus() {
        refreshClock()
        recommendationResult = BusRecommendationEngine.recommend(
            currentNodeId = selectedCurrentNodeId,
            nowDate = currentDate,
            nowTime = currentTime,
            safetyMarginMinutes = selectedSafetyMargin.minutes,
            selectedDestinationStopName = selectedDestinationStopName,
            busTrips = busTrips,
            busStops = LocalBusStopData.busStops,
            graphNodes = graphNodes,
            graphEdges = graphEdges,
            schoolHolidays = LocalSchoolHolidayData.schoolHolidays,
        )
        currentScreen = AppScreen.RESULT
    }

    fun addUserNode(input: UserGraphNodeInput) {
        userNodeInputs = userNodeInputs + input
        viewModelScope.launch { settingsRepository?.saveUserNodeInputs(userNodeInputs) }
        if (input.isSelectableAsStart) {
            selectedCurrentNodeId = graphNodes.last().id
            selectedMapNodeId = selectedCurrentNodeId
        }
        currentScreen = AppScreen.SETTINGS
    }

    fun setEdgeOverride(edgeId: String, minutes: Int?) {
        userEdgeOverrides = if (minutes == null) {
            userEdgeOverrides.filterNot { it.baseEdgeId == edgeId }
        } else {
            userEdgeOverrides.filterNot { it.baseEdgeId == edgeId } + UserEdgeOverride(edgeId, minutes)
        }
        viewModelScope.launch { settingsRepository?.saveUserEdgeOverrides(userEdgeOverrides) }
    }

    fun edgeOverrideMinutes(edgeId: String): Int? =
        userEdgeOverrides.firstOrNull { it.baseEdgeId == edgeId }?.minutes

    fun selectMapNode(nodeId: String) {
        selectedMapNodeId = nodeId
    }

    fun confirmMapNodeSelection() {
        selectedCurrentNodeId = selectedMapNodeId
        currentScreen = AppScreen.HOME
    }

    fun selectMapNodeAndRecommend(nodeId: String) {
        selectedMapNodeId = nodeId
        selectedCurrentNodeId = nodeId
        findBestBus()
    }

    fun updateRainModeEnabled(enabled: Boolean) {
        rainModeEnabled = enabled
        rainForecastStatus = if (enabled) "手動ON" else "手動OFF"
        viewModelScope.launch { settingsRepository?.saveRainModeEnabled(enabled) }
    }

    fun selectFavoriteStartNode(nodeId: String?) {
        favoriteStartNodeId = nodeId
        viewModelScope.launch { settingsRepository?.saveFavoriteStartNodeId(nodeId) }
    }

    fun saveTravelTimeProfile(edgeId: String, standardMinutes: Int, measuredMinutes: Int) {
        if (standardMinutes <= 0 || measuredMinutes <= 0) return
        userTravelTimeProfile = UserTravelTimeProfile(
            calibrationEdgeId = edgeId,
            standardMinutes = standardMinutes,
            measuredMinutes = measuredMinutes,
            timeScaleFactor = measuredMinutes.toDouble() / standardMinutes.toDouble(),
        )
        viewModelScope.launch { settingsRepository?.saveUserTravelTimeProfile(userTravelTimeProfile) }
        currentScreen = AppScreen.SETTINGS
    }

    fun navigate(screen: AppScreen) {
        currentScreen = screen
    }

    fun navigateBack() {
        currentScreen = when (currentScreen) {
            AppScreen.HOME -> AppScreen.HOME
            AppScreen.RESULT -> AppScreen.HOME
            AppScreen.SETTINGS -> AppScreen.HOME
            AppScreen.ADD_NODE -> AppScreen.SETTINGS
            AppScreen.EDIT_TRAVEL_TIME -> AppScreen.SETTINGS
            AppScreen.TRAVEL_TIME_PROFILE -> AppScreen.SETTINGS
        }
    }

    fun goHome() {
        currentScreen = AppScreen.HOME
    }

    fun refreshClock() {
        currentDate = LocalDate.now()
        currentTime = LocalTime.now().withSecond(0).withNano(0)
    }

    private fun requestRainForecastOnce() {
        if (hasRequestedWeather) return
        hasRequestedWeather = true
        viewModelScope.launch {
            when (weatherRepository?.isRainExpected()) {
                true -> {
                    rainModeEnabled = true
                    rainForecastStatus = "自動: 雨予報のためON"
                    settingsRepository?.saveRainModeEnabled(true)
                }
                false -> {
                    rainForecastStatus = "自動: 雨予報なし"
                }
                null -> {
                    rainForecastStatus = "天気予報を取得できませんでした"
                }
            }
        }
    }
}
