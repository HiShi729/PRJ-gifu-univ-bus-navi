package com.example.prj_gifu_univ_bus_navi.data;

import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode;
import com.example.prj_gifu_univ_bus_navi.model.EdgeSourceType;
import com.example.prj_gifu_univ_bus_navi.model.NodeType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class LocalCampusGraphData {
    private static final List<CampusGraphNode> NODES = Collections.unmodifiableList(Arrays.asList(
        new CampusGraphNode("engineering_entrance", "工学部棟入口", NodeType.STANDARD, true, 35.4640, 136.7350),
        new CampusGraphNode("common_education", "全学共通教育棟", NodeType.STANDARD, true, 35.4632, 136.7362),
        new CampusGraphNode("library", "図書館", NodeType.STANDARD, true, 35.4637, 136.7370),
        new CampusGraphNode("university_hall", "大学会館", NodeType.STANDARD, true, 35.4629, 136.7366),
        new CampusGraphNode("applied_biological", "応用生物科学部", NodeType.STANDARD, true, 35.4618, 136.7354),
        new CampusGraphNode("medicine", "医学部", NodeType.STANDARD, true, 35.4670, 136.7332),
        new CampusGraphNode("bus_stop_hospital", "岐阜大学病院", NodeType.BUS_STOP, false, 35.467718, 136.732805),
        new CampusGraphNode("bus_stop_yanagido", "柳戸橋", NodeType.BUS_STOP, false, 35.467137, 136.735476),
        new CampusGraphNode("bus_stop_university", "岐阜大学", NodeType.BUS_STOP, false, 35.462718, 136.736083),
        new CampusGraphNode("transit_center", "経路探索用中継点", NodeType.TRANSIT, false, null, null)
    ));

    private static final List<CampusGraphEdge> EDGES = Collections.unmodifiableList(Arrays.asList(
        new CampusGraphEdge("edge_engineering_common", "engineering_entrance", "common_education", 4, true, EdgeSourceType.STANDARD, true),
        new CampusGraphEdge("edge_common_yanagido", "common_education", "bus_stop_yanagido", 5, true, EdgeSourceType.STANDARD, true),
        new CampusGraphEdge("edge_library_university", "library", "bus_stop_university", 3, true, EdgeSourceType.STANDARD, true),
        new CampusGraphEdge("edge_hall_university", "university_hall", "bus_stop_university", 4, true, EdgeSourceType.STANDARD, true),
        new CampusGraphEdge("edge_medicine_hospital", "medicine", "bus_stop_hospital", 3, true, EdgeSourceType.STANDARD, true),
        new CampusGraphEdge("edge_hospital_yanagido", "bus_stop_hospital", "bus_stop_yanagido", 4, true, EdgeSourceType.STANDARD, true),
        new CampusGraphEdge("edge_yanagido_university", "bus_stop_yanagido", "bus_stop_university", 3, true, EdgeSourceType.STANDARD, true),
        new CampusGraphEdge("edge_library_common", "library", "common_education", 4, true, EdgeSourceType.STANDARD, true),
        new CampusGraphEdge("edge_bio_university", "applied_biological", "bus_stop_university", 5, true, EdgeSourceType.STANDARD, true),
        new CampusGraphEdge("edge_common_transit", "common_education", "transit_center", 2, true, EdgeSourceType.STANDARD, false),
        new CampusGraphEdge("edge_transit_hall", "transit_center", "university_hall", 3, true, EdgeSourceType.STANDARD, false)
    ));

    private LocalCampusGraphData() {
    }

    public static List<CampusGraphNode> getNodes() {
        return NODES;
    }

    public static List<CampusGraphEdge> getEdges() {
        return EDGES;
    }
}
