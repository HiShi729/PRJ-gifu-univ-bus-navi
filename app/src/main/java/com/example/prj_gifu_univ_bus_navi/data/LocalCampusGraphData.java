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
    new CampusGraphNode("engineering_nw_entrance", "工学部棟北西出入口", NodeType.STANDARD, true, 35.464648949602555, 136.7382510209942),
    new CampusGraphNode("engineering_entrance", "工学部棟エントランス", NodeType.STANDARD, true, 35.46404818550222, 136.73836367377558),
    new CampusGraphNode("information_building_entrance", "情報館出入口", NodeType.STANDARD, true, 35.46396640236497, 136.73917107571324),
    new CampusGraphNode("ipteca_entrance", "IPTeCA出入口", NodeType.STANDARD, true, 35.4649977134703, 136.73996384481282),
    new CampusGraphNode("first_cafeteria", "第一食堂", NodeType.STANDARD, true, 35.46331247984927, 136.7373326443138),
    new CampusGraphNode("academic_core_south_entrance", "アカデミックコア南出入口", NodeType.STANDARD, true, 35.46405398195265, 136.73718007945234),
    new CampusGraphNode("auditorium_entrance", "講堂出入口", NodeType.STANDARD, true, 35.46444500595423, 136.73751884104192),
    new CampusGraphNode("library_entrance", "図書館出入口", NodeType.STANDARD, true, 35.464289353476104, 136.7374309987023),
    new CampusGraphNode("education_building_entrance", "教育学部棟エントランス", NodeType.STANDARD, true, 35.46483591380951, 136.7363386443416),
    new CampusGraphNode("general_education_building_entrance", "全学教育学部棟エントランス", NodeType.STANDARD, true, 35.4650604266311, 136.7375480097516),
    new CampusGraphNode("second_cafeteria_entrance", "第二食堂入口", NodeType.STANDARD, true, 35.46569057007385, 136.7379823995085),
    new CampusGraphNode("second_cafeteria_exit", "第二食堂出口", NodeType.STANDARD, true, 35.465718667045316, 136.73822388230232),
    new CampusGraphNode("peco_entrance", "PECO出入口", NodeType.STANDARD, true, 35.46589076078144, 136.73788106297894),
    new CampusGraphNode("nursing_entrance", "看護学科出入口", NodeType.STANDARD, true, 35.46716771757558, 136.73564761216485),
    new CampusGraphNode("music_building_entrance", "音楽棟出入口", NodeType.STANDARD, true, 35.46635151898763, 136.7359451312858),
    new CampusGraphNode("medical_memorial_hall_entrance", "医学部記念会館出入口", NodeType.STANDARD, true, 35.46722082448828, 136.7340784779407),
    new CampusGraphNode("kurono_dormitory", "黒野寮", NodeType.STANDARD, true, 35.46922947164818, 136.73842916597033),

    new CampusGraphNode("bus_stop_hospital", "岐阜大学病院", NodeType.BUS_STOP, false, 35.467718, 136.732805),
    new CampusGraphNode("bus_stop_yanagido", "柳戸橋", NodeType.BUS_STOP, false, 35.467137, 136.735476),
    new CampusGraphNode("bus_stop_university", "岐阜大学", NodeType.BUS_STOP, false, 35.462718, 136.736083)
));

    private static final List<CampusGraphEdge> EDGES = Collections.unmodifiableList(Arrays.asList(
    new CampusGraphEdge("edge_engineering_nw_engineering", "engineering_nw_entrance", "engineering_entrance", 2, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_engineering_information", "engineering_entrance", "information_building_entrance", 2, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_information_ipteca", "information_building_entrance", "ipteca_entrance", 3, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_engineering_academic_core", "engineering_entrance", "academic_core_south_entrance", 2, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_academic_core_first_cafeteria", "academic_core_south_entrance", "first_cafeteria", 3, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_academic_core_library", "academic_core_south_entrance", "library_entrance", 2, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_library_auditorium", "library_entrance", "auditorium_entrance", 1, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_auditorium_general_education", "auditorium_entrance", "general_education_building_entrance", 2, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_general_education_education", "general_education_building_entrance", "education_building_entrance", 3, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_general_education_second_cafeteria_entrance", "general_education_building_entrance", "second_cafeteria_entrance", 2, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_second_cafeteria_entrance_exit", "second_cafeteria_entrance", "second_cafeteria_exit", 1, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_second_cafeteria_entrance_peco", "second_cafeteria_entrance", "peco_entrance", 1, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_peco_music", "peco_entrance", "music_building_entrance", 3, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_music_nursing", "music_building_entrance", "nursing_entrance", 3, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_nursing_medical_memorial", "nursing_entrance", "medical_memorial_hall_entrance", 3, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_peco_kurono_dormitory", "peco_entrance", "kurono_dormitory", 7, true, EdgeSourceType.STANDARD, true),

    new CampusGraphEdge("edge_first_cafeteria_bus_stop_university", "first_cafeteria", "bus_stop_university", 3, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_academic_core_bus_stop_university", "academic_core_south_entrance", "bus_stop_university", 4, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_nursing_bus_stop_yanagido", "nursing_entrance", "bus_stop_yanagido", 3, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_music_bus_stop_yanagido", "music_building_entrance", "bus_stop_yanagido", 4, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_medical_memorial_bus_stop_hospital", "medical_memorial_hall_entrance", "bus_stop_hospital", 3, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_nursing_bus_stop_hospital", "nursing_entrance", "bus_stop_hospital", 5, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_bus_stop_hospital_bus_stop_yanagido", "bus_stop_hospital", "bus_stop_yanagido", 4, true, EdgeSourceType.STANDARD, true),
    new CampusGraphEdge("edge_bus_stop_yanagido_bus_stop_university", "bus_stop_yanagido", "bus_stop_university", 3, true, EdgeSourceType.STANDARD, true)
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
