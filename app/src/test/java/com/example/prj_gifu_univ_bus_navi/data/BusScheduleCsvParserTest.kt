package com.example.prj_gifu_univ_bus_navi.data

import com.example.prj_gifu_univ_bus_navi.model.BaseDayType
import com.example.prj_gifu_univ_bus_navi.model.OperationRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.time.LocalTime

class BusScheduleCsvParserTest {
    @Test
    fun parsesDashAsNullAndTimeWithoutLeadingZero() {
        assertNull(BusScheduleCsvParser.parseTimeOrNull("-"))
        assertNull(BusScheduleCsvParser.parseTimeOrNull(""))
        assertEquals(LocalTime.of(6, 45), BusScheduleCsvParser.parseTimeOrNull("6:45"))
    }

    @Test
    fun parsesEnumsAndBoolean() {
        val csv = """
            id,岐阜大学病院,柳戸橋,岐阜大学,名鉄岐阜,JR岐阜,徹明町,全便なし,路線名,baseDayType,operationRule,operatingStartMonth,operatingEndMonth,mayBeArticulatedBus
            test,6:45,6:46,6:48,-,7:15,7:00,-,C,WEEKDAY,SCHOOL_HOLIDAY_EXCLUDED,,,TRUE
        """.trimIndent()

        val trip = BusScheduleCsvParser.parse(csv).single()

        assertEquals(BaseDayType.WEEKDAY, trip.baseDayType)
        assertEquals(OperationRule.SCHOOL_HOLIDAY_EXCLUDED, trip.operationRule)
        assertEquals(LocalTime.of(7, 15), trip.jrGifuArrivalTime)
        assertNull(trip.meitetsuGifuArrivalTime)
        assertEquals(true, trip.mayBeArticulatedBus)
        assertEquals(LocalTime.of(7, 0), trip.stopTimes["徹明町"])
        assertNull(trip.stopTimes["全便なし"])
        assertFalse("id" in trip.stopTimes)
    }

    @Test
    fun generatesDestinationStopNamesFromValidStopColumns() {
        val csv = """
            id,岐阜大学病院,柳戸橋,岐阜大学,JR岐阜,名鉄岐阜,徹明町,全便なし,baseDayType,operationRule,mayBeArticulatedBus
            test,6:45,6:46,6:48,7:15,-,7:00,-,WEEKDAY,NONE,false
            test2,7:45,7:46,7:48,-,8:15,-,,WEEKDAY,NONE,false
        """.trimIndent()

        val stops = BusScheduleCsvParser.destinationStopNames(csv)

        assertFalse("岐阜大学病院" in stops)
        assertFalse("柳戸橋" in stops)
        assertFalse("岐阜大学" in stops)
        assertFalse("全便なし" in stops)
        assertEquals(true, "JR岐阜" in stops)
        assertEquals(true, "名鉄岐阜" in stops)
        assertEquals(true, "徹明町" in stops)
    }

    @Test
    fun parsesBundledScheduleRows() {
        val csv = File("src/main/assets/bus_schedule.csv").readText()
        val trips = BusScheduleCsvParser.parse(csv)

        assertEquals(139, trips.size)
        assertFalse(trips.any { it.id.isBlank() })
    }
}
