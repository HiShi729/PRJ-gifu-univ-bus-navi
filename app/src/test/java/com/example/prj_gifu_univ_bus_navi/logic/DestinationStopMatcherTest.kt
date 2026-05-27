package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.model.BaseDayType
import com.example.prj_gifu_univ_bus_navi.model.BusTrip
import com.example.prj_gifu_univ_bus_navi.model.OperationRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class DestinationStopMatcherTest {
    @Test
    fun jrSelectedRequiresJrArrival() {
        assertEquals("JR岐阜", DestinationStopMatcher.resolveArrival(trip(jr = LocalTime.of(18, 30)), "JR岐阜")?.first)
        assertNull(DestinationStopMatcher.resolveArrival(trip(jr = null, meitetsu = LocalTime.of(18, 35)), "JR岐阜"))
    }

    @Test
    fun meitetsuSelectedUsesMeitetsuArrivalWhenPresent() {
        val result = DestinationStopMatcher.resolveArrival(
            trip(jr = LocalTime.of(18, 30), meitetsu = LocalTime.of(18, 35)),
            "名鉄岐阜",
        )

        assertEquals("名鉄岐阜", result?.first)
    }

    @Test
    fun meitetsuSelectedFallsBackToJrArrival() {
        val result = DestinationStopMatcher.resolveArrival(trip(jr = LocalTime.of(18, 30)), "名鉄岐阜")

        assertEquals("JR岐阜", result?.first)
    }

    @Test
    fun meitetsuSelectedRejectsTripWithoutAnyArrival() {
        assertNull(DestinationStopMatcher.resolveArrival(trip(), "名鉄岐阜"))
    }

    @Test
    fun arbitraryStopRequiresStopTime() {
        assertEquals("徹明町", DestinationStopMatcher.resolveArrival(trip(stopTimes = mapOf("徹明町" to LocalTime.of(18, 20))), "徹明町")?.first)
        assertNull(DestinationStopMatcher.resolveArrival(trip(stopTimes = mapOf("徹明町" to null)), "徹明町"))
    }

    private fun trip(
        jr: LocalTime? = null,
        meitetsu: LocalTime? = null,
        stopTimes: Map<String, LocalTime?> = mapOf("JR岐阜" to jr, "名鉄岐阜" to meitetsu),
    ) = BusTrip(
        id = "test",
        destination = "岐阜駅",
        baseDayType = BaseDayType.WEEKDAY,
        routeName = "テスト",
        hospitalDepartureTime = LocalTime.of(18, 0),
        yanagidoDepartureTime = LocalTime.of(18, 4),
        universityDepartureTime = LocalTime.of(18, 7),
        jrGifuArrivalTime = jr,
        meitetsuGifuArrivalTime = meitetsu,
        operationRule = OperationRule.NONE,
        operatingStartMonth = null,
        operatingEndMonth = null,
        mayBeArticulatedBus = false,
        stopTimes = stopTimes,
    )
}
