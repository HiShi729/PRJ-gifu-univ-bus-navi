package com.example.prj_gifu_univ_bus_navi.data

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class WeatherRepository {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun isRainExpected(): Boolean? = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(
                "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=35.462718&longitude=136.736083" +
                    "&hourly=precipitation_probability,precipitation,weather_code" +
                    "&forecast_days=1&timezone=Asia%2FTokyo",
            )
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
            }
            connection.inputStream.bufferedReader().use { reader ->
                val hourly = json.parseToJsonElement(reader.readText()).jsonObject["hourly"]?.jsonObject
                val precipitation = hourly?.get("precipitation")?.jsonArray.orEmpty()
                val probabilities = hourly?.get("precipitation_probability")?.jsonArray.orEmpty()
                val weatherCodes = hourly?.get("weather_code")?.jsonArray.orEmpty()
                precipitation.indices.any { index ->
                    precipitation[index].jsonPrimitive.content.toDoubleOrNull().orZero() > 0.0 ||
                        probabilities.getOrNull(index)?.jsonPrimitive?.content?.toIntOrNull().orZero() >= 50 ||
                        weatherCodes.getOrNull(index)?.jsonPrimitive?.content?.toIntOrNull().isRainOrSnowCode()
                }
            }
        }.getOrNull()
    }

    private fun Double?.orZero(): Double = this ?: 0.0

    private fun Int?.orZero(): Int = this ?: 0

    private fun Int?.isRainOrSnowCode(): Boolean = this in setOf(
        51, 53, 55, 56, 57,
        61, 63, 65, 66, 67,
        71, 73, 75, 77,
        80, 81, 82,
        85, 86,
        95, 96, 99,
    )
}
