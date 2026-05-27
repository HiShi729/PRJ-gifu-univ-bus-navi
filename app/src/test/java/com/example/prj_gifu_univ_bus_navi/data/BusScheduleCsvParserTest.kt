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
            id,岐阜大学病院,柳戸橋,岐阜大学,名鉄岐阜,JR岐阜,路線名,baseDayType,operationRule,operatingStartMonth,operatingEndMonth,mayBeArticulatedBus
            test,6:45,6:46,6:48,-,7:15,C,WEEKDAY,SCHOOL_HOLIDAY_EXCLUDED,,,TRUE
        """.trimIndent()

        val trip = BusScheduleCsvParser.parse(csv).single()

        assertEquals(BaseDayType.WEEKDAY, trip.baseDayType)
        assertEquals(OperationRule.SCHOOL_HOLIDAY_EXCLUDED, trip.operationRule)
        assertEquals(LocalTime.of(7, 15), trip.jrGifuArrivalTime)
        assertNull(trip.meitetsuGifuArrivalTime)
        assertEquals(true, trip.mayBeArticulatedBus)
    }

    @Test
    fun parsesBundledScheduleRows() {
        val csv = File("src/main/assets/bus_schedule.csv").readText()
        val trips = BusScheduleCsvParser.parse(csv)

        assertEquals(139, trips.size)
        assertFalse(trips.any { it.id.isBlank() })
    }
}
