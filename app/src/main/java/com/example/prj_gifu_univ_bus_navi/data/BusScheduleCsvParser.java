package com.example.prj_gifu_univ_bus_navi.data;

import com.example.prj_gifu_univ_bus_navi.model.BaseDayType;
import com.example.prj_gifu_univ_bus_navi.model.BusTrip;
import com.example.prj_gifu_univ_bus_navi.model.OperationRule;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BusScheduleCsvParser {
    private static final Set<String> META_COLUMNS = new LinkedHashSet<>(Arrays.asList(
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
        "mayBeArticulatedBus"
    ));
    private static final Set<String> BOARDING_STOP_NAMES = new LinkedHashSet<>(Arrays.asList(
        "岐阜大学病院",
        "柳戸橋",
        "岐阜大学"
    ));

    private BusScheduleCsvParser() {
    }

    public static List<BusTrip> parse(String csvText) {
        List<List<String>> rows = nonBlankRows(parseRows(csvText));
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> header = removeByteOrderMark(rows.get(0));
        List<String> stopColumns = new ArrayList<>();
        for (String column : header) {
            if (!META_COLUMNS.contains(column)) {
                stopColumns.add(column);
            }
        }

        List<BusTrip> trips = new ArrayList<>();
        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            Map<String, String> values = valuesByHeader(header, rows.get(rowIndex));
            Map<String, LocalTime> stopTimes = new LinkedHashMap<>();
            for (String stopName : stopColumns) {
                stopTimes.put(stopName, parseTimeOrNull(value(values, stopName)));
            }
            trips.add(new BusTrip(
                defaultIfBlank(value(values, "id"), value(values, "busNo.")),
                buildDestination(values),
                BaseDayType.valueOf(value(values, "baseDayType")),
                defaultIfBlank(value(values, "routeName"), value(values, "路線名")),
                parseTimeOrNull(value(values, "岐阜大学病院")),
                parseTimeOrNull(value(values, "柳戸橋")),
                parseTimeOrNull(value(values, "岐阜大学")),
                parseTimeOrNull(value(values, "JR岐阜")),
                parseTimeOrNull(value(values, "名鉄岐阜")),
                OperationRule.valueOf(value(values, "operationRule")),
                parseIntegerOrNull(value(values, "operatingStartMonth")),
                parseIntegerOrNull(value(values, "operatingEndMonth")),
                "true".equalsIgnoreCase(value(values, "mayBeArticulatedBus")),
                stopTimes
            ));
        }
        return trips;
    }

    public static List<String> destinationStopNames(String csvText) {
        List<List<String>> rows = nonBlankRows(parseRows(csvText));
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> header = removeByteOrderMark(rows.get(0));
        List<String> stops = new ArrayList<>();
        for (String column : header) {
            if (META_COLUMNS.contains(column) || BOARDING_STOP_NAMES.contains(column)) {
                continue;
            }
            boolean hasValidTime = false;
            for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
                Map<String, String> values = valuesByHeader(header, rows.get(rowIndex));
                if (parseTimeOrNull(value(values, column)) != null) {
                    hasValidTime = true;
                    break;
                }
            }
            if (hasValidTime) {
                stops.add(column);
            }
        }
        return stops;
    }

    public static LocalTime parseTimeOrNull(String rawValue) {
        String normalized = rawValue == null ? "" : rawValue.trim();
        if (normalized.isEmpty() || "-".equals(normalized)) {
            return null;
        }
        String[] parts = normalized.split(":");
        if (parts.length != 2) {
            return null;
        }
        try {
            return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String buildDestination(Map<String, String> values) {
        List<String> stops = new ArrayList<>();
        if (parseTimeOrNull(value(values, "JR岐阜")) != null) {
            stops.add("JR岐阜駅");
        }
        if (parseTimeOrNull(value(values, "名鉄岐阜")) != null) {
            stops.add("名鉄岐阜駅");
        }
        if (stops.isEmpty()) {
            return "岐阜駅方面";
        }
        return String.join("・", stops);
    }

    private static List<List<String>> nonBlankRows(List<List<String>> rows) {
        List<List<String>> filtered = new ArrayList<>();
        for (List<String> row : rows) {
            boolean anyValue = false;
            for (String cell : row) {
                if (!cell.trim().isEmpty()) {
                    anyValue = true;
                    break;
                }
            }
            if (anyValue) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static List<String> removeByteOrderMark(List<String> header) {
        List<String> normalized = new ArrayList<>();
        for (String column : header) {
            normalized.add(column.startsWith("\uFEFF") ? column.substring(1) : column);
        }
        return normalized;
    }

    private static Map<String, String> valuesByHeader(List<String> header, List<String> row) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int index = 0; index < header.size(); index++) {
            String cell = index < row.size() ? row.get(index) : "";
            values.put(header.get(index), cell);
        }
        return values;
    }

    private static String value(Map<String, String> values, String key) {
        String raw = values.get(key);
        return raw == null ? "" : raw.trim();
    }

    private static Integer parseIntegerOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private static List<List<String>> parseRows(String csvText) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean inQuotes = false;
        int index = 0;
        while (index < csvText.length()) {
            char current = csvText.charAt(index);
            if (current == '"' && inQuotes && index + 1 < csvText.length() && csvText.charAt(index + 1) == '"') {
                cell.append('"');
                index++;
            } else if (current == '"') {
                inQuotes = !inQuotes;
            } else if (current == ',' && !inQuotes) {
                row.add(cell.toString());
                cell.setLength(0);
            } else if ((current == '\n' || current == '\r') && !inQuotes) {
                if (current == '\r' && index + 1 < csvText.length() && csvText.charAt(index + 1) == '\n') {
                    index++;
                }
                row.add(cell.toString());
                cell.setLength(0);
                rows.add(new ArrayList<>(row));
                row.clear();
            } else {
                cell.append(current);
            }
            index++;
        }
        if (cell.length() > 0 || !row.isEmpty()) {
            row.add(cell.toString());
            rows.add(new ArrayList<>(row));
        }
        return rows;
    }
}
