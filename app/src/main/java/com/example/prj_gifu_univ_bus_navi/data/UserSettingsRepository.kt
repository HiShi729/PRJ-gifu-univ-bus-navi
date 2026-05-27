package com.example.prj_gifu_univ_bus_navi.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.prj_gifu_univ_bus_navi.model.DestinationBusStop
import com.example.prj_gifu_univ_bus_navi.model.UserEdgeOverride
import com.example.prj_gifu_univ_bus_navi.model.UserGraphNodeInput
import com.example.prj_gifu_univ_bus_navi.model.UserTravelTimeProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.userSettingsDataStore by preferencesDataStore(name = "user_settings")

data class UserSettingsState(
    val selectedDestination: DestinationBusStop = DestinationBusStop.JR_GIFU,
    val safetyMarginMinutes: Int = 1,
    val rainModeEnabled: Boolean = false,
    val favoriteStartNodeId: String? = null,
    val userNodeInputs: List<UserGraphNodeInput> = emptyList(),
    val userEdgeOverrides: List<UserEdgeOverride> = emptyList(),
    val userTravelTimeProfile: UserTravelTimeProfile? = null,
)

class UserSettingsRepository(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val settingsFlow: Flow<UserSettingsState> = context.userSettingsDataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { preferences ->
            UserSettingsState(
                selectedDestination = preferences[Keys.selectedDestination]?.let { runCatching { DestinationBusStop.valueOf(it) }.getOrNull() } ?: DestinationBusStop.JR_GIFU,
                safetyMarginMinutes = preferences[Keys.safetyMarginMinutes] ?: 1,
                rainModeEnabled = preferences[Keys.rainModeEnabled] ?: false,
                favoriteStartNodeId = preferences[Keys.favoriteStartNodeId],
                userNodeInputs = decodeList(preferences[Keys.userNodeInputsJson].orEmpty()),
                userEdgeOverrides = decodeList(preferences[Keys.userEdgeOverridesJson].orEmpty()),
                userTravelTimeProfile = decodeOrNull(preferences[Keys.userTravelTimeProfileJson].orEmpty()),
            )
        }

    suspend fun saveSelectedDestination(destination: DestinationBusStop) {
        context.userSettingsDataStore.edit { it[Keys.selectedDestination] = destination.name }
    }

    suspend fun saveSafetyMarginMinutes(minutes: Int) {
        context.userSettingsDataStore.edit { it[Keys.safetyMarginMinutes] = minutes }
    }

    suspend fun saveRainModeEnabled(enabled: Boolean) {
        context.userSettingsDataStore.edit { it[Keys.rainModeEnabled] = enabled }
    }

    suspend fun saveFavoriteStartNodeId(nodeId: String?) {
        context.userSettingsDataStore.edit { preferences ->
            if (nodeId == null) preferences.remove(Keys.favoriteStartNodeId) else preferences[Keys.favoriteStartNodeId] = nodeId
        }
    }

    suspend fun saveUserNodeInputs(inputs: List<UserGraphNodeInput>) {
        context.userSettingsDataStore.edit { it[Keys.userNodeInputsJson] = json.encodeToString(inputs) }
    }

    suspend fun saveUserEdgeOverrides(overrides: List<UserEdgeOverride>) {
        context.userSettingsDataStore.edit { it[Keys.userEdgeOverridesJson] = json.encodeToString(overrides) }
    }

    suspend fun saveUserTravelTimeProfile(profile: UserTravelTimeProfile?) {
        context.userSettingsDataStore.edit { preferences ->
            if (profile == null) preferences.remove(Keys.userTravelTimeProfileJson) else preferences[Keys.userTravelTimeProfileJson] = json.encodeToString(profile)
        }
    }

    private inline fun <reified T> decodeList(value: String): List<T> =
        if (value.isBlank()) emptyList() else runCatching { json.decodeFromString<List<T>>(value) }.getOrDefault(emptyList())

    private inline fun <reified T> decodeOrNull(value: String): T? =
        if (value.isBlank()) null else runCatching { json.decodeFromString<T>(value) }.getOrNull()

    private object Keys {
        val selectedDestination = stringPreferencesKey("selected_destination")
        val safetyMarginMinutes = intPreferencesKey("safety_margin_minutes")
        val rainModeEnabled = booleanPreferencesKey("rain_mode_enabled")
        val favoriteStartNodeId = stringPreferencesKey("favorite_start_node_id")
        val userNodeInputsJson = stringPreferencesKey("user_node_inputs_json")
        val userEdgeOverridesJson = stringPreferencesKey("user_edge_overrides_json")
        val userTravelTimeProfileJson = stringPreferencesKey("user_travel_time_profile_json")
    }
}
