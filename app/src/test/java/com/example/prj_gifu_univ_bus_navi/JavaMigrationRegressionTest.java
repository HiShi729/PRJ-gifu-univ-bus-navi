package com.example.prj_gifu_univ_bus_navi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.prj_gifu_univ_bus_navi.data.BusScheduleCsvParser;
import com.example.prj_gifu_univ_bus_navi.data.LocalAcademicCalendarData;
import com.example.prj_gifu_univ_bus_navi.data.LocalCampusGraphData;
import com.example.prj_gifu_univ_bus_navi.data.LocalHolidayData;
import com.example.prj_gifu_univ_bus_navi.logic.BusRecommendationEngine;
import com.example.prj_gifu_univ_bus_navi.logic.BusTripServiceFilter;
import com.example.prj_gifu_univ_bus_navi.logic.CampusGraphBuilder;
import com.example.prj_gifu_univ_bus_navi.logic.DestinationStopMatcher;
import com.example.prj_gifu_univ_bus_navi.logic.EdgeTravelTimeResolver;
import com.example.prj_gifu_univ_bus_navi.logic.ServiceCalendar;
import com.example.prj_gifu_univ_bus_navi.logic.ShortestPathCalculator;
import com.example.prj_gifu_univ_bus_navi.model.BaseDayType;
import com.example.prj_gifu_univ_bus_navi.model.BusStop;
import com.example.prj_gifu_univ_bus_navi.model.BusStopCandidate;
import com.example.prj_gifu_univ_bus_navi.model.BusStopId;
import com.example.prj_gifu_univ_bus_navi.model.BusTrip;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode;
import com.example.prj_gifu_univ_bus_navi.model.EdgeSourceType;
import com.example.prj_gifu_univ_bus_navi.model.NodeType;
import com.example.prj_gifu_univ_bus_navi.model.OperationRule;
import com.example.prj_gifu_univ_bus_navi.model.RecommendationResult;
import com.example.prj_gifu_univ_bus_navi.model.UserEdgeOverride;
import com.example.prj_gifu_univ_bus_navi.model.UserGraphNodeInput;
import com.example.prj_gifu_univ_bus_navi.model.UserNodeCoordinateSource;
import com.example.prj_gifu_univ_bus_navi.model.UserTravelTimeProfile;
import com.example.prj_gifu_univ_bus_navi.ui.AppScreen;
import com.example.prj_gifu_univ_bus_navi.ui.CampusMapDefaults;
import com.example.prj_gifu_univ_bus_navi.ui.MainViewModel;
import com.example.prj_gifu_univ_bus_navi.ui.MapCoordinateProjector;
import com.example.prj_gifu_univ_bus_navi.ui.UiSelectionFilters;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kotlin.Pair;
import org.junit.Test;

public class JavaMigrationRegressionTest {
    @Test
    public void csvParserKeepsStopTimesAndBundledRowCount() throws Exception {
        assertNull(BusScheduleCsvParser.parseTimeOrNull("-"));
        assertNull(BusScheduleCsvParser.parseTimeOrNull(""));
        assertEquals(LocalTime.of(6, 45), BusScheduleCsvParser.parseTimeOrNull("6:45"));

        String csv = "id,岐阜大学病院,柳戸橋,岐阜大学,JR岐阜,名鉄岐阜,徹明町,全便なし,baseDayType,operationRule,mayBeArticulatedBus\n" +
            "test,6:45,6:46,6:48,7:15,-,7:00,-,WEEKDAY,NONE,false\n" +
            "test2,7:45,7:46,7:48,-,8:15,-,,WEEKDAY,NONE,false";
        List<String> stops = BusScheduleCsvParser.destinationStopNames(csv);
        assertFalse(stops.contains("岐阜大学病院"));
        assertFalse(stops.contains("柳戸橋"));
        assertFalse(stops.contains("岐阜大学"));
        assertFalse(stops.contains("全便なし"));
        assertTrue(stops.contains("JR岐阜"));
        assertTrue(stops.contains("名鉄岐阜"));
        assertTrue(stops.contains("徹明町"));

        String bundled = new String(Files.readAllBytes(new File("src/main/assets/bus_schedule.csv").toPath()), StandardCharsets.UTF_8);
        List<BusTrip> trips = BusScheduleCsvParser.parse(bundled);
        assertEquals(139, trips.size());
        assertFalse(trips.get(0).getId().isEmpty());
    }

    @Test
    public void calendarAndServiceRulesAreMaintained() {
        assertTrue(LocalAcademicCalendarData.isClassDay(LocalDate.of(2026, 4, 10)));
        assertTrue(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2026, 10, 30)));
        assertTrue(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2027, 1, 4)));
        assertFalse(LocalAcademicCalendarData.isSchoolHoliday(LocalDate.of(2027, 1, 5)));
        assertEquals(BaseDayType.WEEKEND_HOLIDAY, ServiceCalendar.createContext(LocalHolidayData.getHolidays().iterator().next(), Collections.emptyList()).getBaseDayType());

        BusTrip excluded = trip("test", OperationRule.SCHOOL_HOLIDAY_EXCLUDED, null, null, defaultStopTimes());
        assertFalse(BusTripServiceFilter.isTripAvailable(excluded, ServiceCalendar.createContext(LocalDate.of(2026, 10, 30), Collections.emptyList())));
        BusTrip limited = trip("test", OperationRule.LIMITED_PERIOD, 4, 8, defaultStopTimes());
        assertTrue(BusTripServiceFilter.isTripAvailable(limited, ServiceCalendar.createContext(LocalDate.of(2026, 5, 7), Collections.emptyList())));
        assertFalse(BusTripServiceFilter.isTripAvailable(limited, ServiceCalendar.createContext(LocalDate.of(2026, 9, 1), Collections.emptyList())));
    }

    @Test
    public void recommendationKeepsDestinationFallbackAndNegativeMarginFilter() {
        RecommendationResult result = recommend(LocalTime.of(18, 0), 0, "JR岐阜", Collections.singletonList(trip()));
        BusStopCandidate yanagido = null;
        for (BusStopCandidate candidate : result.getAllCandidates()) {
            if (candidate.getBusStopId() == BusStopId.YANAGIDO) yanagido = candidate;
        }
        assertNotNull(yanagido);
        assertEquals(0, yanagido.getRemainingMinutes());

        RecommendationResult filtered = recommend(LocalTime.of(18, 0), 1, "JR岐阜", Collections.singletonList(trip()));
        for (BusStopCandidate candidate : filtered.getAllCandidates()) {
            assertFalse(candidate.getBusStopId() == BusStopId.YANAGIDO);
        }

        RecommendationResult meitetsu = recommend(LocalTime.of(18, 0), 0, "名鉄岐阜", Collections.singletonList(trip()));
        assertNotNull(meitetsu.getRecommendedCandidate());
        assertEquals("JR岐阜", meitetsu.getRecommendedCandidate().getActualArrivalBusStopName());

        Map<String, LocalTime> stopTimes = new LinkedHashMap<>();
        stopTimes.put("徹明町", LocalTime.of(18, 20));
        RecommendationResult arbitrary = recommend(LocalTime.of(18, 0), 0, "徹明町", Collections.singletonList(trip("has_stop", OperationRule.NONE, null, null, stopTimes)));
        assertTrue(arbitrary.getAllCandidates().size() > 0);
    }

    @Test
    public void recommendationKeepsSummaryWhenNoRideCandidatesExist() {
        RecommendationResult result = recommend(LocalTime.of(23, 0), 0, "JR岐阜", Collections.singletonList(trip()));

        assertNull(result.getRecommendedCandidate());
        assertEquals(0, result.getAllCandidates().size());
        assertEquals("現在時刻以降に乗車可能な便がありません", result.getMessage());
        assertEquals(3, result.getSummaryCandidates().size());

        BusStopCandidate yanagido = null;
        for (BusStopCandidate candidate : result.getSummaryCandidates()) {
            if (candidate.getBusStopId() == BusStopId.YANAGIDO) yanagido = candidate;
        }
        assertNotNull(yanagido);
        assertEquals(LocalTime.of(23, 8), yanagido.getArrivalTimeAtBusStop());
        assertEquals(8, yanagido.getTravelMinutes());
        assertNull(yanagido.getDepartureTime());
        assertNull(yanagido.getDestinationArrivalTime());
    }

    @Test
    public void pathMapAndUiFiltersAreMaintained() {
        assertEquals(9, ShortestPathCalculator.findShortestPath(
            LocalCampusGraphData.getNodes(),
            LocalCampusGraphData.getEdges(),
            "engineering_entrance",
            "bus_stop_yanagido"
        ).getTotalMinutes());

        CampusGraphEdge edge = new CampusGraphEdge("edge", "a", "b", 5, true, EdgeSourceType.STANDARD, true);
        assertEquals(2, EdgeTravelTimeResolver.resolveMinutes(edge, Collections.singletonList(new UserEdgeOverride("edge", 2)), new UserTravelTimeProfile("edge", 5, 3, 0.6), false));
        assertEquals(4, EdgeTravelTimeResolver.resolveMinutes(edge, Collections.emptyList(), new UserTravelTimeProfile("edge", 5, 3, 0.6), true));

        assertNotNull(MapCoordinateProjector.project(35.462718, 136.736083, 1000f, 800f));
        assertFalse(MapCoordinateProjector.isGpsLocationVisibleOnCampusMap(37.421998333333335, -122.084));
        assertTrue(CampusMapDefaults.bounds.aspectRatio() > 0f);

        List<CampusGraphNode> nodes = Arrays.asList(
            new CampusGraphNode("standard", "standard", NodeType.STANDARD, true, 35.4640, 136.7350),
            new CampusGraphNode("hidden", "hidden", NodeType.STANDARD, false, 35.4641, 136.7351),
            new CampusGraphNode("bus", "bus", NodeType.BUS_STOP, false, 35.467137, 136.735476)
        );
        assertEquals(2, UiSelectionFilters.selectableMapNodes(nodes).size());

        Pair<List<CampusGraphNode>, List<CampusGraphEdge>> graph = CampusGraphBuilder.buildGraph(
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.singletonList(new UserGraphNodeInput("研究室", "base", 3, true, 35.4645, 136.7355, UserNodeCoordinateSource.MANUAL)),
            Collections.emptyList()
        );
        assertEquals(1, UiSelectionFilters.selectableMapNodes(graph.getFirst()).size());
    }

    @Test
    public void javaViewModelBackNavigationWorks() {
        MainViewModel viewModel = new MainViewModel();
        viewModel.navigate(AppScreen.SETTINGS);
        viewModel.navigateBack();
        assertEquals(AppScreen.HOME, viewModel.getCurrentScreen());
        viewModel.navigate(AppScreen.ADD_NODE);
        viewModel.navigateBack();
        assertEquals(AppScreen.SETTINGS, viewModel.getCurrentScreen());
    }

    private RecommendationResult recommend(LocalTime nowTime, int safetyMargin, String destination, List<BusTrip> trips) {
        return BusRecommendationEngine.recommend(
            "start",
            LocalDate.of(2026, 5, 7),
            nowTime,
            safetyMargin,
            destination,
            trips,
            busStops(),
            nodes(),
            edges(),
            Collections.emptyList()
        );
    }

    private BusTrip trip() {
        return trip("test", OperationRule.NONE, null, null, defaultStopTimes());
    }

    private BusTrip trip(String id, OperationRule rule, Integer startMonth, Integer endMonth, Map<String, LocalTime> stopTimes) {
        return new BusTrip(
            id,
            "JR岐阜駅",
            BaseDayType.WEEKDAY,
            "テスト",
            LocalTime.of(18, 6),
            LocalTime.of(18, 8),
            LocalTime.of(18, 10),
            stopTimes.get("JR岐阜"),
            stopTimes.get("名鉄岐阜"),
            rule,
            startMonth,
            endMonth,
            false,
            "",
            stopTimes
        );
    }

    private Map<String, LocalTime> defaultStopTimes() {
        Map<String, LocalTime> stopTimes = new LinkedHashMap<>();
        stopTimes.put("JR岐阜", LocalTime.of(18, 35));
        stopTimes.put("名鉄岐阜", null);
        return stopTimes;
    }

    private List<BusStop> busStops() {
        return Arrays.asList(
            new BusStop(BusStopId.GIFU_UNIV_HOSPITAL, "岐阜大学病院", "hospital"),
            new BusStop(BusStopId.YANAGIDO, "柳戸橋", "yanagido"),
            new BusStop(BusStopId.GIFU_UNIV, "岐阜大学", "university")
        );
    }

    private List<CampusGraphNode> nodes() {
        return Arrays.asList(
            new CampusGraphNode("start", "開始地点", NodeType.STANDARD, true, null, null),
            new CampusGraphNode("hospital", "岐阜大学病院", NodeType.BUS_STOP, false, null, null),
            new CampusGraphNode("yanagido", "柳戸橋", NodeType.BUS_STOP, false, null, null),
            new CampusGraphNode("university", "岐阜大学", NodeType.BUS_STOP, false, null, null)
        );
    }

    private List<CampusGraphEdge> edges() {
        return Arrays.asList(
            new CampusGraphEdge("start_hospital", "start", "hospital", 9, true, EdgeSourceType.STANDARD, true),
            new CampusGraphEdge("start_yanagido", "start", "yanagido", 8, true, EdgeSourceType.STANDARD, true),
            new CampusGraphEdge("start_university", "start", "university", 11, true, EdgeSourceType.STANDARD, true)
        );
    }
}
