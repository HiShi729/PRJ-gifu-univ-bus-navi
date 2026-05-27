package com.example.prj_gifu_univ_bus_navi.model

import java.time.LocalDate
import java.time.LocalTime

enum class BaseDayType {
    WEEKDAY,
    WEEKEND_HOLIDAY,
}

enum class OperationRule {
    NONE,
    SCHOOL_HOLIDAY_EXCLUDED,
    SCHOOL_HOLIDAY_ONLY,
    LIMITED_PERIOD,
    LIMITED_PERIOD_AND_SCHOOL_HOLIDAY_EXCLUDED,
}

enum class NodeType {
    STANDARD,
    USER_ADDED,
    BUS_STOP,
    TRANSIT,
}

enum class EdgeSourceType {
    STANDARD,
    USER_ADDED,
    USER_OVERRIDE,
}

enum class BusStopId {
    GIFU_UNIV_HOSPITAL,
    YANAGIDO,
    GIFU_UNIV,
}

enum class DestinationBusStop {
    JR_GIFU,
    MEITETSU_GIFU,
}

data class BusTrip(
    val id: String,
    val destination: String,
    val baseDayType: BaseDayType,
    val routeName: String,
    val hospitalDepartureTime: LocalTime,
    val yanagidoDepartureTime: LocalTime,
    val universityDepartureTime: LocalTime,
    val jrGifuArrivalTime: LocalTime?,
    val meitetsuGifuArrivalTime: LocalTime?,
    val operationRule: OperationRule,
    val operatingStartMonth: Int?,
    val operatingEndMonth: Int?,
    val mayBeArticulatedBus: Boolean,
)

data class BusStop(
    val id: BusStopId,
    val name: String,
    val nodeId: String,
)

data class CampusGraphNode(
    val id: String,
    val name: String,
    val nodeType: NodeType,
    val isSelectableAsStart: Boolean,
    val latitude: Double?,
    val longitude: Double?,
)

data class CampusGraphEdge(
    val id: String,
    val fromNodeId: String,
    val toNodeId: String,
    val minutes: Int,
    val isBidirectional: Boolean,
    val sourceType: EdgeSourceType,
    val isSelectableForUserEdit: Boolean,
)

data class UserGraphNodeInput(
    val name: String,
    val connectedNodeId: String,
    val minutesToConnectedNode: Int,
    val isSelectableAsStart: Boolean,
)

data class UserEdgeOverride(
    val baseEdgeId: String,
    val minutes: Int,
)

data class ShortestPathResult(
    val totalMinutes: Int,
    val nodePath: List<String>,
)

data class BusStopCandidate(
    val busStopId: BusStopId,
    val busStopName: String,
    val tripId: String,
    val departureTime: LocalTime,
    val travelMinutes: Int,
    val arrivalTimeAtBusStop: LocalTime,
    val remainingMinutes: Int,
    val canCatch: Boolean,
    val routeName: String,
    val destinationBusStop: DestinationBusStop,
    val actualArrivalBusStop: DestinationBusStop,
    val destinationArrivalTime: LocalTime?,
    val mayBeArticulatedBus: Boolean,
    val reason: String,
)

data class RecommendationResult(
    val recommendedCandidate: BusStopCandidate?,
    val allCandidates: List<BusStopCandidate>,
    val message: String?,
)

data class ServiceDateContext(
    val date: LocalDate,
    val baseDayType: BaseDayType,
    val isSchoolHoliday: Boolean,
    val month: Int,
)
