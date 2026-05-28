package com.example.prj_gifu_univ_bus_navi.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.prj_gifu_univ_bus_navi.model.UserEdgeOverride;
import com.example.prj_gifu_univ_bus_navi.model.UserGraphNodeInput;
import com.example.prj_gifu_univ_bus_navi.model.UserTravelTimeProfile;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public final class UserSettingsRepository {
    private static final String PREF_NAME = "user_settings";
    private static final String KEY_SELECTED_DESTINATION_LEGACY = "selected_destination";
    private static final String KEY_SELECTED_DESTINATION_STOP_NAME = "selected_destination_stop_name";
    private static final String KEY_SAFETY_MARGIN_MINUTES = "safety_margin_minutes";
    private static final String KEY_RAIN_MODE_ENABLED = "rain_mode_enabled";
    private static final String KEY_FAVORITE_START_NODE_ID = "favorite_start_node_id";
    private static final String KEY_USER_NODE_INPUTS_JSON = "user_node_inputs_json";
    private static final String KEY_USER_EDGE_OVERRIDES_JSON = "user_edge_overrides_json";
    private static final String KEY_USER_TRAVEL_TIME_PROFILE_JSON = "user_travel_time_profile_json";

    private final SharedPreferences preferences;

    public UserSettingsRepository(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public UserSettingsState loadSettings() {
        return new UserSettingsState(
            loadDestinationStopName(),
            preferences.getInt(KEY_SAFETY_MARGIN_MINUTES, 1),
            preferences.getBoolean(KEY_RAIN_MODE_ENABLED, false),
            preferences.getString(KEY_FAVORITE_START_NODE_ID, null),
            decodeUserNodeInputs(preferences.getString(KEY_USER_NODE_INPUTS_JSON, "")),
            decodeUserEdgeOverrides(preferences.getString(KEY_USER_EDGE_OVERRIDES_JSON, "")),
            decodeUserTravelTimeProfile(preferences.getString(KEY_USER_TRAVEL_TIME_PROFILE_JSON, ""))
        );
    }

    public void saveSelectedDestinationStopName(String stopName) {
        preferences.edit().putString(KEY_SELECTED_DESTINATION_STOP_NAME, stopName).apply();
    }

    public void saveSafetyMarginMinutes(int minutes) {
        preferences.edit().putInt(KEY_SAFETY_MARGIN_MINUTES, minutes).apply();
    }

    public void saveRainModeEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_RAIN_MODE_ENABLED, enabled).apply();
    }

    public void saveFavoriteStartNodeId(String nodeId) {
        SharedPreferences.Editor editor = preferences.edit();
        if (nodeId == null) {
            editor.remove(KEY_FAVORITE_START_NODE_ID);
        } else {
            editor.putString(KEY_FAVORITE_START_NODE_ID, nodeId);
        }
        editor.apply();
    }

    public void saveUserNodeInputs(List<UserGraphNodeInput> inputs) {
        preferences.edit().putString(KEY_USER_NODE_INPUTS_JSON, encodeUserNodeInputs(inputs)).apply();
    }

    public void saveUserEdgeOverrides(List<UserEdgeOverride> overrides) {
        preferences.edit().putString(KEY_USER_EDGE_OVERRIDES_JSON, encodeUserEdgeOverrides(overrides)).apply();
    }

    public void saveUserTravelTimeProfile(UserTravelTimeProfile profile) {
        SharedPreferences.Editor editor = preferences.edit();
        if (profile == null) {
            editor.remove(KEY_USER_TRAVEL_TIME_PROFILE_JSON);
        } else {
            editor.putString(KEY_USER_TRAVEL_TIME_PROFILE_JSON, profile.toJson().toString());
        }
        editor.apply();
    }

    private String loadDestinationStopName() {
        String selected = preferences.getString(KEY_SELECTED_DESTINATION_STOP_NAME, null);
        if (selected != null) {
            return selected;
        }
        String legacy = preferences.getString(KEY_SELECTED_DESTINATION_LEGACY, null);
        if ("MEITETSU_GIFU".equals(legacy)) {
            return "名鉄岐阜";
        }
        return "JR岐阜";
    }

    private static String encodeUserNodeInputs(List<UserGraphNodeInput> inputs) {
        JSONArray array = new JSONArray();
        for (UserGraphNodeInput input : inputs) {
            array.put(input.toJson());
        }
        return array.toString();
    }

    private static String encodeUserEdgeOverrides(List<UserEdgeOverride> overrides) {
        JSONArray array = new JSONArray();
        for (UserEdgeOverride override : overrides) {
            array.put(override.toJson());
        }
        return array.toString();
    }

    private static List<UserGraphNodeInput> decodeUserNodeInputs(String value) {
        List<UserGraphNodeInput> inputs = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return inputs;
        }
        try {
            JSONArray array = new JSONArray(value);
            for (int index = 0; index < array.length(); index++) {
                inputs.add(UserGraphNodeInput.fromJson(array.getJSONObject(index)));
            }
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
        return inputs;
    }

    private static List<UserEdgeOverride> decodeUserEdgeOverrides(String value) {
        List<UserEdgeOverride> overrides = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return overrides;
        }
        try {
            JSONArray array = new JSONArray(value);
            for (int index = 0; index < array.length(); index++) {
                overrides.add(UserEdgeOverride.fromJson(array.getJSONObject(index)));
            }
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
        return overrides;
    }

    private static UserTravelTimeProfile decodeUserTravelTimeProfile(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return UserTravelTimeProfile.fromJson(new JSONObject(value));
        } catch (Exception ignored) {
            return null;
        }
    }
}
