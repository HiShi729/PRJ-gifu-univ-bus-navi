package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.model.BusStop
import com.example.prj_gifu_univ_bus_navi.model.BusStopCandidate
import com.example.prj_gifu_univ_bus_navi.model.BusStopId
import com.example.prj_gifu_univ_bus_navi.model.BusTrip
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode
import com.example.prj_gifu_univ_bus_navi.model.RecommendationResult
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

object BusRecommendationEngine {
    private const val NO_CANDIDATE_MESSAGE = "現在時刻以降に乗車可能な便がありません"

    fun recommend(
        currentNodeId: String,
        nowDate: LocalDate,
        nowTime: LocalTime,
        safetyMarginMinutes: Int,
        selectedDestinationStopName: String,
        busTrips: List<BusTrip>,
        busStops: List<BusStop>,
        graphNodes: List<CampusGraphNode>,
        graphEdges: List<CampusGraphEdge>,
        schoolHolidays: List<LocalDate>,
    ): RecommendationResult {
        val context = ServiceCalendar.createContext(nowDate, schoolHolidays)
        val availableTrips = BusTripServiceFilter.filterAvailableTrips(busTrips, context)
        val pathsByStop = busStops.associateWith { busStop ->
            ShortestPathCalculator.findShortestPath(graphNodes, graphEdges, currentNodeId, busStop.nodeId)
        }

        val candidates = availableTrips.flatMap { trip ->
            val arrival = DestinationStopMatcher.resolveArrival(trip, selectedDestinationStopName) ?: return@flatMap emptyList()
            busStops.mapNotNull { busStop ->
                val path = pathsByStop[busStop] ?: return@mapNotNull null
                val departureTime = trip.departureTimeAt(busStop.id) ?: return@mapNotNull null
                createCandidate(
                    trip = trip,
                    busStop = busStop,
                    departureTime = departureTime,
                    travelMinutes = path.totalMinutes,
                    nowTime = nowTime,
                    safetyMarginMinutes = safetyMarginMinutes,
                    selectedDestinationStopName = selectedDestinationStopName,
                    resolvedArrival = arrival,
                )
            }
        }.sortedWith(compareBy<BusStopCandidate> { it.departureTime }.thenBy { it.travelMinutes })

        val recommended = candidates.minWithOrNull(compareBy<BusStopCandidate> { it.departureTime }.thenBy { it.travelMinutes })

        return RecommendationResult(
            recommendedCandidate = recommended,
            allCandidates = candidates.take(10),
            message = if (recommended == null) NO_CANDIDATE_MESSAGE else null,
        )
    }

    private fun createCandidate(
        trip: BusTrip,
        busStop: BusStop,
        departureTime: LocalTime,
        travelMinutes: Int,
        nowTime: LocalTime,
        safetyMarginMinutes: Int,
        selectedDestinationStopName: String,
        resolvedArrival: Pair<String, LocalTime>,
    ): BusStopCandidate? {
        val arrivalAtBusStop = nowTime.plusMinutes(travelMinutes.toLong())
        val latestArrivalWithMargin = arrivalAtBusStop.plusMinutes(safetyMarginMinutes.toLong())
        val canCatch = !latestArrivalWithMargin.isAfter(departureTime)
        val remainingMinutes = Duration.between(latestArrivalWithMargin, departureTime).toMinutes().toInt()
        if (remainingMinutes < 0 || !canCatch) return null
        return BusStopCandidate(
            busStopId = busStop.id,
            busStopName = busStop.name,
            tripId = trip.id,
            departureTime = departureTime,
            travelMinutes = travelMinutes,
            arrivalTimeAtBusStop = arrivalAtBusStop,
            remainingMinutes = remainingMinutes,
            canCatch = canCatch,
            routeName = trip.routeName,
            destinationBusStopName = selectedDestinationStopName,
            actualArrivalBusStopName = resolvedArrival.first,
            destinationArrivalTime = resolvedArrival.second,
            mayBeArticulatedBus = trip.mayBeArticulatedBus,
            reason = if (canCatch) {
                "発車時刻までに到着でき、候補の中で発車時刻と移動時間を比較できます"
            } else {
                "余裕時間を含めると発車時刻に間に合いません"
            },
        )
    }

    private fun BusTrip.departureTimeAt(busStopId: BusStopId): LocalTime? = when (busStopId) {
        BusStopId.GIFU_UNIV_HOSPITAL -> hospitalDepartureTime
        BusStopId.YANAGIDO -> yanagidoDepartureTime
        BusStopId.GIFU_UNIV -> universityDepartureTime
    }
}
