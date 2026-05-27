package com.example.prj_gifu_univ_bus_navi.data;

import com.example.prj_gifu_univ_bus_navi.model.UserEdgeOverride;
import com.example.prj_gifu_univ_bus_navi.model.UserGraphNodeInput;
import com.example.prj_gifu_univ_bus_navi.model.UserTravelTimeProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UserSettingsState {
    private final String selectedDestinationStopName;
    private final int safetyMarginMinutes;
    private final boolean rainModeEnabled;
    private final String favoriteStartNodeId;
    private final List<UserGraphNodeInput> userNodeInputs;
    private final List<UserEdgeOverride> userEdgeOverrides;
    private final UserTravelTimeProfile userTravelTimeProfile;

    public UserSettingsState(
        String selectedDestinationStopName,
        int safetyMarginMinutes,
        boolean rainModeEnabled,
        String favoriteStartNodeId,
        List<UserGraphNodeInput> userNodeInputs,
        List<UserEdgeOverride> userEdgeOverrides,
        UserTravelTimeProfile userTravelTimeProfile
    ) {
        this.selectedDestinationStopName = selectedDestinationStopName;
        this.safetyMarginMinutes = safetyMarginMinutes;
        this.rainModeEnabled = rainModeEnabled;
        this.favoriteStartNodeId = favoriteStartNodeId;
        this.userNodeInputs = Collections.unmodifiableList(new ArrayList<>(userNodeInputs));
        this.userEdgeOverrides = Collections.unmodifiableList(new ArrayList<>(userEdgeOverrides));
        this.userTravelTimeProfile = userTravelTimeProfile;
    }

    public String getSelectedDestinationStopName() { return selectedDestinationStopName; }
    public int getSafetyMarginMinutes() { return safetyMarginMinutes; }
    public boolean isRainModeEnabled() { return rainModeEnabled; }
    public String getFavoriteStartNodeId() { return favoriteStartNodeId; }
    public List<UserGraphNodeInput> getUserNodeInputs() { return userNodeInputs; }
    public List<UserEdgeOverride> getUserEdgeOverrides() { return userEdgeOverrides; }
    public UserTravelTimeProfile getUserTravelTimeProfile() { return userTravelTimeProfile; }
}
