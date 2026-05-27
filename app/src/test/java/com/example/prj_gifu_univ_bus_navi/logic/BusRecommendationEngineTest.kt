package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.model.BaseDayType
import com.example.prj_gifu_univ_bus_navi.model.BusStop
import com.example.prj_gifu_univ_bus_navi.model.BusStopId
import com.example.prj_gifu_univ_bus_navi.model.BusTrip
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode
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
        val result = recommend(safetyMargin = 0, destination = "JR岐阜")

        val yanagido = result.allCandidates.first { it.busStopId == BusStopId.YANAGIDO }
        assertTrue(yanagido.canCatch)
        assertEquals(0, yanagido.remainingMinutes)
    }

    @Test
    fun excludesCandidateWhenSafetyMarginMakesArrivalLate() {
        val result = recommend(safetyMargin = 1, destination = "JR岐阜")

        assertFalse(result.allCandidates.any { it.busStopId == BusStopId.YANAGIDO })
    }

    @Test
    fun returnsMessageWhenNoCatchableCandidateExists() {
        val result = recommend(nowTime = LocalTime.of(23, 0), safetyMargin = 0, destination = "JR岐阜")

        assertNull(result.recommendedCandidate)
        assertEquals("現在時刻以降に乗車可能な便がありません", result.message)
    }

    @Test
    fun meitetsuSelectionCanUseJrArrivalTrip() {
        val result = recommend(safetyMargin = 0, destination = "名鉄岐阜")

        assertNotNull(result.recommendedCandidate)
        assertEquals("JR岐阜", result.recommendedCandidate?.actualArrivalBusStopName)
    }

    @Test
    fun arbitraryDestinationRequiresStopTime() {
        val result = recommend(
            safetyMargin = 0,
            destination = "徹明町",
            trips = listOf(
                trip(id = "has_stop", stopTimes = mapOf("徹明町" to LocalTime.of(18, 20))),
                trip(id = "no_stop", stopTimes = mapOf("徹明町" to null)),
            ),
        )

        assertTrue(result.allCandidates.all { it.tripId == "has_stop" })
    }

    private fun recommend(
        nowTime: LocalTime = LocalTime.of(18, 0),
        safetyMargin: Int,
        destination: String,
        trips: List<BusTrip> = listOf(trip()),
    ) = BusRecommendationEngine.recommend(
        "start",
        LocalDate.of(2026, 5, 7),
        nowTime,
        safetyMargin,
        destination,
        trips,
        busStops(),
        nodes(),
        edges(),
        emptyList(),
    )

    private fun trip(
        id: String = "test",
        stopTimes: Map<String, LocalTime?> = mapOf("JR岐阜" to LocalTime.of(18, 35), "名鉄岐阜" to null),
    ) = BusTrip(
        id,
        "JR岐阜駅",
        BaseDayType.WEEKDAY,
        "テスト",
        LocalTime.of(18, 6),
        LocalTime.of(18, 8),
        LocalTime.of(18, 10),
        LocalTime.of(18, 35),
        null,
        OperationRule.NONE,
        null,
        null,
        false,
        stopTimes,
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
