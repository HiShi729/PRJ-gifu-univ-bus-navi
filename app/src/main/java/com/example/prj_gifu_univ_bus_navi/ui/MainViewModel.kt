package com.example.prj_gifu_univ_bus_navi.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.prj_gifu_univ_bus_navi.data.LocalBusScheduleData
import com.example.prj_gifu_univ_bus_navi.data.LocalBusStopData
import com.example.prj_gifu_univ_bus_navi.data.LocalCampusGraphData
import com.example.prj_gifu_univ_bus_navi.data.LocalSchoolHolidayData
import com.example.prj_gifu_univ_bus_navi.logic.BusRecommendationEngine
import com.example.prj_gifu_univ_bus_navi.logic.CampusGraphBuilder
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode
import com.example.prj_gifu_univ_bus_navi.model.DestinationBusStop
import com.example.prj_gifu_univ_bus_navi.model.RecommendationResult
import com.example.prj_gifu_univ_bus_navi.model.UserEdgeOverride
import com.example.prj_gifu_univ_bus_navi.model.UserGraphNodeInput
import java.time.LocalDate
import java.time.LocalTime

enum class AppScreen {
    HOME,
    RESULT,
    ADD_NODE,
    EDIT_TRAVEL_TIME,
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
    var selectedDestination by mutableStateOf(DestinationBusStop.JR_GIFU)
        private set
    var selectedSafetyMargin by mutableStateOf(safetyOptions[1])
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

    val safetyMarginOptions: List<SafetyMarginOption> = safetyOptions

    val graphNodes: List<CampusGraphNode>
        get() = CampusGraphBuilder.buildGraph(
            LocalCampusGraphData.nodes,
            LocalCampusGraphData.edges,
            userNodeInputs,
            userEdgeOverrides,
        ).first

    val graphEdges: List<CampusGraphEdge>
        get() = CampusGraphBuilder.buildGraph(
            LocalCampusGraphData.nodes,
            LocalCampusGraphData.edges,
            userNodeInputs,
            userEdgeOverrides,
        ).second

    val selectableStartNodes: List<CampusGraphNode>
        get() = graphNodes.filter { it.isSelectableAsStart }

    val editableEdges: List<CampusGraphEdge>
        get() = LocalCampusGraphData.edges.filter { it.isSelectableForUserEdit }

    fun selectCurrentNode(nodeId: String) {
        selectedCurrentNodeId = nodeId
    }

    fun selectDestination(destination: DestinationBusStop) {
        selectedDestination = destination
    }

    fun selectSafetyMargin(option: SafetyMarginOption) {
        selectedSafetyMargin = option
    }

    fun findBestBus() {
        refreshClock()
        recommendationResult = BusRecommendationEngine.recommend(
            currentNodeId = selectedCurrentNodeId,
            nowDate = currentDate,
            nowTime = currentTime,
            safetyMarginMinutes = selectedSafetyMargin.minutes,
            selectedDestination = selectedDestination,
            busTrips = LocalBusScheduleData.busTrips,
            busStops = LocalBusStopData.busStops,
            graphNodes = graphNodes,
            graphEdges = graphEdges,
            schoolHolidays = LocalSchoolHolidayData.schoolHolidays,
        )
        currentScreen = AppScreen.RESULT
    }

    fun addUserNode(input: UserGraphNodeInput) {
        userNodeInputs = userNodeInputs + input
        if (input.isSelectableAsStart) {
            selectedCurrentNodeId = graphNodes.last().id
        }
        currentScreen = AppScreen.HOME
    }

    fun setEdgeOverride(edgeId: String, minutes: Int?) {
        userEdgeOverrides = if (minutes == null) {
            userEdgeOverrides.filterNot { it.baseEdgeId == edgeId }
        } else {
            userEdgeOverrides.filterNot { it.baseEdgeId == edgeId } + UserEdgeOverride(edgeId, minutes)
        }
    }

    fun edgeOverrideMinutes(edgeId: String): Int? =
        userEdgeOverrides.firstOrNull { it.baseEdgeId == edgeId }?.minutes

    fun navigate(screen: AppScreen) {
        currentScreen = screen
    }

    fun goHome() {
        currentScreen = AppScreen.HOME
    }

    fun refreshClock() {
        currentDate = LocalDate.now()
        currentTime = LocalTime.now().withSecond(0).withNano(0)
    }
}
