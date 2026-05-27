package com.example.prj_gifu_univ_bus_navi.data

import com.example.prj_gifu_univ_bus_navi.model.BaseDayType
import com.example.prj_gifu_univ_bus_navi.model.BusTrip
import com.example.prj_gifu_univ_bus_navi.model.OperationRule
import java.time.LocalTime

object BusScheduleCsvParser {
    private val metaColumns = setOf(
        "busNo.",
        "種類",
        "路線名",
        "routeName",
        "option",
        "id",
        "baseDayType",
        "operationRule",
        "operatingStartMonth",
        "operatingEndMonth",
        "mayBeArticulatedBus",
    )
    private val boardingStopNames = setOf("岐阜大学病院", "柳戸橋", "岐阜大学")

    fun parse(csvText: String): List<BusTrip> {
        val rows = parseRows(csvText).filter { row -> row.any { it.isNotBlank() } }
        if (rows.isEmpty()) return emptyList()
        val header = rows.first().map { it.removePrefix("\uFEFF") }
        val stopColumns = header.filterNot { it in metaColumns }
        val records = rows.drop(1)
        return records.map { row ->
            val values = header.mapIndexed { index, name -> name to row.getOrElse(index) { "" } }.toMap()
            val stopTimes = stopColumns.associateWith { stopName -> parseTimeOrNull(values.value(stopName)) }
            BusTrip(
                values.value("id").ifBlank { values.value("busNo.") },
                buildDestination(values),
                BaseDayType.valueOf(values.value("baseDayType")),
                values.value("routeName").ifBlank { values.value("路線名") },
                parseTimeOrNull(values.value("岐阜大学病院")),
                parseTimeOrNull(values.value("柳戸橋")),
                parseTimeOrNull(values.value("岐阜大学")),
                parseTimeOrNull(values.value("JR岐阜")),
                parseTimeOrNull(values.value("名鉄岐阜")),
                OperationRule.valueOf(values.value("operationRule")),
                values.value("operatingStartMonth").toIntOrNull(),
                values.value("operatingEndMonth").toIntOrNull(),
                values.value("mayBeArticulatedBus").equals("true", ignoreCase = true),
                stopTimes,
            )
        }
    }

    fun destinationStopNames(csvText: String): List<String> {
        val rows = parseRows(csvText).filter { row -> row.any { it.isNotBlank() } }
        if (rows.isEmpty()) return emptyList()
        val header = rows.first().map { it.removePrefix("\uFEFF") }
        val records = rows.drop(1)
        return header.filter { column ->
            column !in metaColumns &&
                column !in boardingStopNames &&
                records.any { row ->
                    val values = header.mapIndexed { index, name -> name to row.getOrElse(index) { "" } }.toMap()
                    parseTimeOrNull(values.value(column)) != null
                }
        }
    }

    fun parseTimeOrNull(value: String): LocalTime? {
        val normalized = value.trim()
        if (normalized.isBlank() || normalized == "-") return null
        val parts = normalized.split(":")
        if (parts.size != 2) return null
        return LocalTime.of(parts[0].toInt(), parts[1].toInt())
    }

    private fun buildDestination(values: Map<String, String>): String {
        val stops = listOfNotNull(
            "JR岐阜駅".takeIf { parseTimeOrNull(values.value("JR岐阜")) != null },
            "名鉄岐阜駅".takeIf { parseTimeOrNull(values.value("名鉄岐阜")) != null },
        )
        return stops.joinToString("・").ifBlank { "岐阜駅方面" }
    }

    private fun Map<String, String>.value(key: String): String = this[key].orEmpty().trim()

    private fun parseRows(csvText: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val cell = StringBuilder()
        var inQuotes = false
        var index = 0
        while (index < csvText.length) {
            val char = csvText[index]
            when {
                char == '"' && inQuotes && csvText.getOrNull(index + 1) == '"' -> {
                    cell.append('"')
                    index++
                }
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    row.add(cell.toString())
                    cell.clear()
                }
                (char == '\n' || char == '\r') && !inQuotes -> {
                    if (char == '\r' && csvText.getOrNull(index + 1) == '\n') index++
                    row.add(cell.toString())
                    cell.clear()
                    rows.add(row.toList())
                    row.clear()
                }
                else -> cell.append(char)
            }
            index++
        }
        if (cell.isNotEmpty() || row.isNotEmpty()) {
            row.add(cell.toString())
            rows.add(row.toList())
        }
        return rows
    }
}
