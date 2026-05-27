package com.example.prj_gifu_univ_bus_navi.data;

import android.content.Context;
import com.example.prj_gifu_univ_bus_navi.model.BusTrip;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class LocalBusScheduleData {
    private LocalBusScheduleData() {
    }

    public static List<BusTrip> loadBusTrips(Context context) {
        return BusScheduleCsvParser.parse(readAssetText(context));
    }

    public static List<String> loadDestinationStopNames(Context context) {
        return BusScheduleCsvParser.destinationStopNames(readAssetText(context));
    }

    private static String readAssetText(Context context) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            context.getAssets().open("bus_schedule.csv"),
            StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read bus_schedule.csv", ex);
        }
        return builder.toString();
    }
}
