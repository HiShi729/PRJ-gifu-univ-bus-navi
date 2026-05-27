package com.example.prj_gifu_univ_bus_navi.model

import java.time.LocalDate
import java.time.LocalTime
import kotlinx.serialization.Serializable

data class BusTrip(
    val id: String,
    val destination: String,
    val baseDayType: BaseDayType,
    val routeName: String,
    val hospitalDepartureTime: LocalTime?,
    val yanagidoDepartureTime: LocalTime?,
    val universityDepartureTime: LocalTime?,
    val jrGifuArrivalTime: LocalTime?,
    val meitetsuGifuArrivalTime: LocalTime?,
    val operationRule: OperationRule,
    val operatingStartMonth: Int?,
    val operatingEndMonth: Int?,
    val mayBeArticulatedBus: Boolean,
    val stopTimes: Map<String, LocalTime?> = emptyMap(),
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

@Serializable
data class UserGraphNodeInput(
    val name: String,
    val connectedNodeId: String,
    val minutesToConnectedNode: Int,
    val isSelectableAsStart: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val coordinateSource: UserNodeCoordinateSource = UserNodeCoordinateSource.NONE,
)

@Serializable
data class UserEdgeOverride(
    val baseEdgeId: String,
    val minutes: Int,
)

@Serializable
data class UserTravelTimeProfile(
    val calibrationEdgeId: String,
    val standardMinutes: Int,
    val measuredMinutes: Int,
    val timeScaleFactor: Double,
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
    val destinationBusStopName: String,
    val actualArrivalBusStopName: String,
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
