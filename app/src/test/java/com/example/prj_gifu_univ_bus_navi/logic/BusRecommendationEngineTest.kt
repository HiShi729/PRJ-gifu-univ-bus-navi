package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.model.BaseDayType
import com.example.prj_gifu_univ_bus_navi.model.BusStop
import com.example.prj_gifu_univ_bus_navi.model.BusStopId
import com.example.prj_gifu_univ_bus_navi.model.BusTrip
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode
import com.example.prj_gifu_univ_bus_navi.model.DestinationBusStop
import com.example.prj_gifu_univ_bus_navi.model.EdgeSourceType
import com.example.prj_gifu_univ_bus_navi.model.NodeType
import com.example.prj_gifu_univ_bus_navi.model.OperationRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class BusRecommendationEngineTest {
    @Test
    fun canCatchWhenArrivalEqualsDepartureWithZeroMargin() {
        val result = recommend(safetyMargin = 0, destination = DestinationBusStop.JR_GIFU)

        val yanagido = result.allCandidates.first { it.busStopId == BusStopId.YANAGIDO }
        assertTrue(yanagido.canCatch)
    }

    @Test
    fun cannotCatchWhenSafetyMarginMakesArrivalLate() {
        val result = recommend(safetyMargin = 1, destination = DestinationBusStop.JR_GIFU)

        val yanagido = result.allCandidates.first { it.busStopId == BusStopId.YANAGIDO }
        assertFalse(yanagido.canCatch)
    }

    @Test
    fun returnsMessageWhenNoCatchableCandidateExists() {
        val result = recommend(nowTime = LocalTime.of(23, 0), safetyMargin = 0, destination = DestinationBusStop.JR_GIFU)

        assertNull(result.recommendedCandidate)
        assertEquals("現在時刻以降に乗車可能な便がありません", result.message)
    }

    @Test
    fun meitetsuSelectionCanUseJrArrivalTrip() {
        val result = recommend(safetyMargin = 0, destination = DestinationBusStop.MEITETSU_GIFU)

        assertNotNull(result.recommendedCandidate)
        assertEquals(DestinationBusStop.JR_GIFU, result.recommendedCandidate?.actualArrivalBusStop)
    }

    private fun recommend(
        nowTime: LocalTime = LocalTime.of(18, 0),
        safetyMargin: Int,
        destination: DestinationBusStop,
    ) = BusRecommendationEngine.recommend(
        currentNodeId = "start",
        nowDate = LocalDate.of(2026, 5, 4),
        nowTime = nowTime,
        safetyMarginMinutes = safetyMargin,
        selectedDestination = destination,
        busTrips = listOf(trip()),
        busStops = busStops(),
        graphNodes = nodes(),
        graphEdges = edges(),
        schoolHolidays = emptyList(),
    )

    private fun trip() = BusTrip(
        id = "test",
        destination = "JR岐阜駅",
        baseDayType = BaseDayType.WEEKDAY,
        routeName = "テスト",
        hospitalDepartureTime = LocalTime.of(18, 6),
        yanagidoDepartureTime = LocalTime.of(18, 8),
        universityDepartureTime = LocalTime.of(18, 10),
        jrGifuArrivalTime = LocalTime.of(18, 35),
        meitetsuGifuArrivalTime = null,
        operationRule = OperationRule.NONE,
        operatingStartMonth = null,
        operatingEndMonth = null,
        mayBeArticulatedBus = false,
    )

    private fun busStops() = listOf(
        BusStop(BusStopId.GIFU_UNIV_HOSPITAL, "岐阜大学病院", "hospital"),
        BusStop(BusStopId.YANAGIDO, "柳戸橋", "yanagido"),
        BusStop(BusStopId.GIFU_UNIV, "岐阜大学", "university"),
    )

    private fun nodes() = listOf(
        CampusGraphNode("start", "開始地点", NodeType.STANDARD, true, null, null),
        CampusGraphNode("hospital", "岐阜大学病院", NodeType.BUS_STOP, false, null, null),
        CampusGraphNode("yanagido", "柳戸橋", NodeType.BUS_STOP, false, null, null),
        CampusGraphNode("university", "岐阜大学", NodeType.BUS_STOP, false, null, null),
    )

    private fun edges() = listOf(
        CampusGraphEdge("start_hospital", "start", "hospital", 9, true, EdgeSourceType.STANDARD, true),
        CampusGraphEdge("start_yanagido", "start", "yanagido", 8, true, EdgeSourceType.STANDARD, true),
        CampusGraphEdge("start_university", "start", "university", 11, true, EdgeSourceType.STANDARD, true),
    )
}
