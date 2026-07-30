package com.example.prj_gifu_univ_bus_navi.ui;

import com.example.prj_gifu_univ_bus_navi.data.LocalBusStopData;
import com.example.prj_gifu_univ_bus_navi.data.LocalCampusGraphData;
import com.example.prj_gifu_univ_bus_navi.data.LocalSchoolHolidayData;
import com.example.prj_gifu_univ_bus_navi.data.UserSettingsRepository;
import com.example.prj_gifu_univ_bus_navi.data.UserSettingsState;
import com.example.prj_gifu_univ_bus_navi.data.WeatherRepository;
import com.example.prj_gifu_univ_bus_navi.logic.BusRecommendationEngine;
import com.example.prj_gifu_univ_bus_navi.logic.CampusGraphBuilder;
import com.example.prj_gifu_univ_bus_navi.model.BusTrip;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode;
import com.example.prj_gifu_univ_bus_navi.model.RecommendationResult;
import com.example.prj_gifu_univ_bus_navi.model.UserEdgeOverride;
import com.example.prj_gifu_univ_bus_navi.model.UserGraphNodeInput;
import com.example.prj_gifu_univ_bus_navi.model.UserTravelTimeProfile;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kotlin.Pair;

public class MainViewModel {
    private final List<SafetyMarginOption> safetyOptions = Collections.unmodifiableList(Arrays.asList(
        new SafetyMarginOption("急ぐ: 0分", 0),
        new SafetyMarginOption("標準: 1分", 1),
        new SafetyMarginOption("安全: 3分", 3)
    ));

    private AppScreen currentScreen = AppScreen.HOME;
    private String selectedCurrentNodeId = firstSelectableNodeId();
    private String selectedDestinationStopName = "JR岐阜";
    private SafetyMarginOption selectedSafetyMargin = safetyOptions.get(1);
    private String selectedMapNodeId = selectedCurrentNodeId;
    private RecommendationResult recommendationResult;
    private LocalDate currentDate = LocalDate.now();
    private LocalTime currentTime = LocalTime.now().withSecond(0).withNano(0);
    private List<UserGraphNodeInput> userNodeInputs = new ArrayList<>();
    private List<UserEdgeOverride> userEdgeOverrides = new ArrayList<>();
    private UserTravelTimeProfile userTravelTimeProfile;
    private boolean rainModeEnabled;
    private String favoriteStartNodeId;
    private String rainForecastStatus = "天気予報を確認中";
    private List<BusTrip> busTrips = new ArrayList<>();
    private List<String> destinationStopNames = new ArrayList<>(Arrays.asList("JR岐阜", "名鉄岐阜"));
    private UserSettingsRepository settingsRepository;
    private WeatherRepository weatherRepository;
    private boolean hasRequestedWeather;

    public void loadInitialData(
        List<BusTrip> busTrips,
        List<String> destinationStopNames,
        UserSettingsRepository repository,
        WeatherRepository weatherRepository
    ) {
        this.busTrips = new ArrayList<>(busTrips);
        this.destinationStopNames = destinationStopNames.isEmpty()
            ? new ArrayList<>(Arrays.asList("JR岐阜", "名鉄岐阜"))
            : new ArrayList<>(destinationStopNames);
        if (settingsRepository != null) {
            return;
        }
        this.settingsRepository = repository;
        this.weatherRepository = weatherRepository;
        UserSettingsState settings = repository.loadSettings();
        selectedDestinationStopName = this.destinationStopNames.contains(settings.getSelectedDestinationStopName())
            ? settings.getSelectedDestinationStopName()
            : "JR岐阜";
        selectedSafetyMargin = findSafetyOption(settings.getSafetyMarginMinutes());
        rainModeEnabled = settings.isRainModeEnabled();
        userNodeInputs = new ArrayList<>(settings.getUserNodeInputs());
        userEdgeOverrides = new ArrayList<>(settings.getUserEdgeOverrides());
        userTravelTimeProfile = settings.getUserTravelTimeProfile();
        favoriteStartNodeId = settings.getFavoriteStartNodeId();
        if (favoriteStartNodeId != null && containsSelectableStartNode(favoriteStartNodeId)) {
            selectedCurrentNodeId = favoriteStartNodeId;
            selectedMapNodeId = favoriteStartNodeId;
        }
        requestRainForecastOnce();
    }

    public AppScreen getCurrentScreen() { return currentScreen; }
    public String getSelectedCurrentNodeId() { return selectedCurrentNodeId; }

    public String getSelectedCurrentNodeName() {
        for (CampusGraphNode node : getGraphNodes()) {
            if (node.getId().equals(selectedCurrentNodeId)) {
                return node.getName();
            }
        }
        return "現在地";
    }

    public String getSelectedDestinationStopName() { return selectedDestinationStopName; }
    public SafetyMarginOption getSelectedSafetyMargin() { return selectedSafetyMargin; }
    public RecommendationResult getRecommendationResult() { return recommendationResult; }
    public boolean isRainModeEnabled() { return rainModeEnabled; }
    public String getFavoriteStartNodeId() { return favoriteStartNodeId; }
    public UserTravelTimeProfile getUserTravelTimeProfile() { return userTravelTimeProfile; }
    public List<SafetyMarginOption> getSafetyMarginOptions() { return safetyOptions; }
    public List<String> getDestinationStopNames() { return destinationStopNames; }

    public List<CampusGraphNode> getGraphNodes() {
        return buildGraph().getFirst();
    }

    public List<CampusGraphEdge> getGraphEdges() {
        return buildGraph().getSecond();
    }

    public List<CampusGraphNode> getSelectableStartNodes() {
        List<CampusGraphNode> result = new ArrayList<>();
        for (CampusGraphNode node : getGraphNodes()) {
            if (node.isSelectableAsStart()) {
                result.add(node);
            }
        }
        return result;
    }

    public List<CampusGraphNode> getMapSelectableNodes() {
        return UiSelectionFilters.selectableMapNodes(getGraphNodes());
    }

    public List<CampusGraphEdge> getEditableEdges() {
        List<CampusGraphEdge> result = new ArrayList<>();
        Map<String, CampusGraphNode> nodesById = new HashMap<>();
        for (CampusGraphNode node : getGraphNodes()) {
            nodesById.put(node.getId(), node);
        }
        for (CampusGraphEdge edge : LocalCampusGraphData.getEdges()) {
            CampusGraphNode fromNode = nodesById.get(edge.getFromNodeId());
            CampusGraphNode toNode = nodesById.get(edge.getToNodeId());
            if (edge.isSelectableForUserEdit()
                && fromNode != null && fromNode.isSelectableAsStart()
                && toNode != null && toNode.isSelectableAsStart()) {
                result.add(edge);
            }
        }
        return result;
    }

    public void selectCurrentNode(String nodeId) {
        selectedCurrentNodeId = nodeId;
        selectedMapNodeId = nodeId;
    }

    public CampusGraphNode selectNearestNode(double lat, double lon) {
        List<CampusGraphNode> selectable = getSelectableStartNodes();
        CampusGraphNode nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (CampusGraphNode node : selectable) {
            if (node.getLatitude() == null || node.getLongitude() == null) continue;
            double d = Math.hypot(node.getLatitude() - lat, node.getLongitude() - lon);
            if (d < minDistance) {
                minDistance = d;
                nearest = node;
            }
        }
        if (nearest != null) {
            selectCurrentNode(nearest.getId());
        }
        return nearest;
    }

    public void selectDestinationStop(String stopName) {
        selectedDestinationStopName = stopName;
        if (settingsRepository != null) settingsRepository.saveSelectedDestinationStopName(stopName);
    }

    public void selectSafetyMargin(SafetyMarginOption option) {
        selectedSafetyMargin = option;
        if (settingsRepository != null) settingsRepository.saveSafetyMarginMinutes(option.getMinutes());
    }

    public void findBestBus() {
        refreshClock();
        recommendationResult = BusRecommendationEngine.recommend(
            selectedCurrentNodeId,
            currentDate,
            currentTime,
            selectedSafetyMargin.getMinutes(),
            selectedDestinationStopName,
            busTrips,
            LocalBusStopData.getBusStops(),
            getGraphNodes(),
            getGraphEdges(),
            LocalSchoolHolidayData.getSchoolHolidays()
        );
        currentScreen = AppScreen.RESULT;
    }

    public void addUserNode(UserGraphNodeInput input) {
        userNodeInputs = new ArrayList<>(userNodeInputs);
        userNodeInputs.add(input);
        if (settingsRepository != null) settingsRepository.saveUserNodeInputs(userNodeInputs);
        if (input.isSelectableAsStart()) {
            List<CampusGraphNode> nodes = getGraphNodes();
            selectedCurrentNodeId = nodes.get(nodes.size() - 1).getId();
            selectedMapNodeId = selectedCurrentNodeId;
        }
        currentScreen = AppScreen.SETTINGS;
    }

    public void selectMapNodeAndRecommend(String nodeId) {
        selectedMapNodeId = nodeId;
        selectedCurrentNodeId = nodeId;
        findBestBus();
    }

    public void updateRainModeEnabled(boolean enabled) {
        rainModeEnabled = enabled;
        rainForecastStatus = enabled ? "手動ON" : "手動OFF";
        if (settingsRepository != null) settingsRepository.saveRainModeEnabled(enabled);
    }

    public void selectFavoriteStartNode(String nodeId) {
        favoriteStartNodeId = nodeId;
        if (settingsRepository != null) settingsRepository.saveFavoriteStartNodeId(nodeId);
    }

    public void saveTravelTimeProfile(String edgeId, int standardTravelTimeSeconds, int measuredTravelTimeSeconds) {
        if (standardTravelTimeSeconds <= 0 || measuredTravelTimeSeconds <= 0) return;
        userTravelTimeProfile = new UserTravelTimeProfile(
            edgeId,
            standardTravelTimeSeconds,
            measuredTravelTimeSeconds,
            (double) measuredTravelTimeSeconds / (double) standardTravelTimeSeconds
        );
        if (settingsRepository != null) settingsRepository.saveUserTravelTimeProfile(userTravelTimeProfile);
        currentScreen = AppScreen.SETTINGS;
    }

    public void navigate(AppScreen screen) {
        currentScreen = screen;
    }

    public void navigateBack() {
        switch (currentScreen) {
            case RESULT:
            case SETTINGS:
                currentScreen = AppScreen.HOME;
                break;
            case ADD_NODE:
            case EDIT_TRAVEL_TIME:
            case TRAVEL_TIME_PROFILE:
                currentScreen = AppScreen.SETTINGS;
                break;
            case HOME:
            default:
                currentScreen = AppScreen.HOME;
                break;
        }
    }

    public void refreshClock() {
        currentDate = LocalDate.now();
        currentTime = LocalTime.now().withSecond(0).withNano(0);
    }

    private Pair<List<CampusGraphNode>, List<CampusGraphEdge>> buildGraph() {
        return CampusGraphBuilder.buildGraph(
            LocalCampusGraphData.getNodes(),
            LocalCampusGraphData.getEdges(),
            userNodeInputs,
            userEdgeOverrides,
            userTravelTimeProfile,
            rainModeEnabled
        );
    }

    private void requestRainForecastOnce() {
        if (hasRequestedWeather || weatherRepository == null) return;
        hasRequestedWeather = true;
        weatherRepository.isRainExpected(rainExpected -> {
            if (Boolean.TRUE.equals(rainExpected)) {
                rainModeEnabled = true;
                rainForecastStatus = "自動: 雨予報のためON";
                if (settingsRepository != null) settingsRepository.saveRainModeEnabled(true);
            } else if (Boolean.FALSE.equals(rainExpected)) {
                rainForecastStatus = "自動: 雨予報なし";
            } else {
                rainForecastStatus = "天気予報を取得できませんでした";
            }
        });
    }

    private SafetyMarginOption findSafetyOption(int minutes) {
        for (SafetyMarginOption option : safetyOptions) {
            if (option.getMinutes() == minutes) {
                return option;
            }
        }
        return selectedSafetyMargin;
    }

    private boolean containsSelectableStartNode(String nodeId) {
        for (CampusGraphNode node : getSelectableStartNodes()) {
            if (node.getId().equals(nodeId)) {
                return true;
            }
        }
        return false;
    }

    private static String firstSelectableNodeId() {
        for (CampusGraphNode node : LocalCampusGraphData.getNodes()) {
            if (node.isSelectableAsStart()) {
                return node.getId();
            }
        }
        return LocalCampusGraphData.getNodes().get(0).getId();
    }
}
