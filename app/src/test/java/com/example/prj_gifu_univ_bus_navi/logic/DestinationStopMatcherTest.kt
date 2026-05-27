package com.example.prj_gifu_univ_bus_navi.logic

import com.example.prj_gifu_univ_bus_navi.model.BaseDayType
import com.example.prj_gifu_univ_bus_navi.model.BusTrip
import com.example.prj_gifu_univ_bus_navi.model.DestinationBusStop
import com.example.prj_gifu_univ_bus_navi.model.OperationRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class DestinationStopMatcherTest {
    @Test
    fun jrSelectedRequiresJrArrival() {
        assertEquals(DestinationBusStop.JR_GIFU, DestinationStopMatcher.resolveArrival(trip(jr = LocalTime.of(18, 30)), DestinationBusStop.JR_GIFU)?.first)
        assertNull(DestinationStopMatcher.resolveArrival(trip(jr = null, meitetsu = LocalTime.of(18, 35)), DestinationBusStop.JR_GIFU))
    }

    @Test
    fun meitetsuSelectedUsesMeitetsuArrivalWhenPresent() {
        val result = DestinationStopMatcher.resolveArrival(
            trip(jr = LocalTime.of(18, 30), meitetsu = LocalTime.of(18, 35)),
            DestinationBusStop.MEITETSU_GIFU,
        )

        assertEquals(DestinationBusStop.MEITETSU_GIFU, result?.first)
    }

    @Test
    fun meitetsuSelectedFallsBackToJrArrival() {
        val result = DestinationStopMatcher.resolveArrival(trip(jr = LocalTime.of(18, 30)), DestinationBusStop.MEITETSU_GIFU)

        assertEquals(DestinationBusStop.JR_GIFU, result?.first)
    }

    @Test
    fun meitetsuSelectedRejectsTripWithoutAnyArrival() {
        assertNull(DestinationStopMatcher.resolveArrival(trip(), DestinationBusStop.MEITETSU_GIFU))
    }

    private fun trip(jr: LocalTime? = null, meitetsu: LocalTime? = null) = BusTrip(
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
    )
}
