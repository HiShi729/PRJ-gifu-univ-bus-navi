package com.example.prj_gifu_univ_bus_navi.data;

import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WeatherRepository {
    public interface RainForecastCallback {
        void onResult(Boolean rainExpected);
    }

    private static final String FORECAST_URL =
        "https://api.open-meteo.com/v1/forecast" +
            "?latitude=35.462718&longitude=136.736083" +
            "&hourly=precipitation_probability,precipitation,weather_code" +
            "&forecast_days=1&timezone=Asia%2FTokyo";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void isRainExpected(RainForecastCallback callback) {
        executor.execute(() -> {
            Boolean result = null;
            try {
                result = fetchRainExpected();
            } catch (Exception ignored) {
                result = null;
            }
            Boolean finalResult = result;
            mainHandler.post(() -> callback.onResult(finalResult));
        });
    }

    private Boolean fetchRainExpected() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(FORECAST_URL).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String json = builder.toString();
            double[] precipitation = parseDoubleArray(json, "precipitation");
            int[] probabilities = parseIntArray(json, "precipitation_probability");
            int[] weatherCodes = parseIntArray(json, "weather_code");
            int max = Math.max(precipitation.length, Math.max(probabilities.length, weatherCodes.length));
            for (int index = 0; index < max; index++) {
                if (doubleAt(precipitation, index) > 0.0 ||
                    intAt(probabilities, index) >= 50 ||
                    isRainOrSnowCode(intAt(weatherCodes, index))) {
                    return true;
                }
            }
            return false;
        } finally {
            connection.disconnect();
        }
    }

    private static double[] parseDoubleArray(String json, String key) {
        String[] values = parseArrayValues(json, key);
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            try {
                result[i] = Double.parseDouble(values[i].trim());
            } catch (NumberFormatException ex) {
                result[i] = 0.0;
            }
        }
        return result;
    }

    private static int[] parseIntArray(String json, String key) {
        String[] values = parseArrayValues(json, key);
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            try {
                result[i] = Integer.parseInt(values[i].trim());
            } catch (NumberFormatException ex) {
                result[i] = 0;
            }
        }
        return result;
    }

    private static String[] parseArrayValues(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\[(.*?)]");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return new String[0];
        }
        String body = matcher.group(1).trim();
        if (body.isEmpty()) {
            return new String[0];
        }
        return body.split(",");
    }

    private static double doubleAt(double[] values, int index) {
        return index >= 0 && index < values.length ? values[index] : 0.0;
    }

    private static int intAt(int[] values, int index) {
        return index >= 0 && index < values.length ? values[index] : 0;
    }

    private static boolean isRainOrSnowCode(int code) {
        return code == 51 || code == 53 || code == 55 || code == 56 || code == 57 ||
            code == 61 || code == 63 || code == 65 || code == 66 || code == 67 ||
            code == 71 || code == 73 || code == 75 || code == 77 ||
            code == 80 || code == 81 || code == 82 ||
            code == 85 || code == 86 ||
            code == 95 || code == 96 || code == 99;
    }
}
