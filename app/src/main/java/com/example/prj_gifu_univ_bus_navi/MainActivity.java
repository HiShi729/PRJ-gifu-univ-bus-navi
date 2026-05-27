package com.example.prj_gifu_univ_bus_navi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.prj_gifu_univ_bus_navi.data.LocalBusScheduleData;
import com.example.prj_gifu_univ_bus_navi.data.UserSettingsRepository;
import com.example.prj_gifu_univ_bus_navi.data.WeatherRepository;
import com.example.prj_gifu_univ_bus_navi.model.BusStopCandidate;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge;
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode;
import com.example.prj_gifu_univ_bus_navi.model.RecommendationResult;
import com.example.prj_gifu_univ_bus_navi.model.UserGraphNodeInput;
import com.example.prj_gifu_univ_bus_navi.model.UserNodeCoordinateSource;
import com.example.prj_gifu_univ_bus_navi.ui.AppScreen;
import com.example.prj_gifu_univ_bus_navi.ui.CampusMapView;
import com.example.prj_gifu_univ_bus_navi.ui.MainViewModel;
import com.example.prj_gifu_univ_bus_navi.ui.MapCoordinateProjector;
import com.example.prj_gifu_univ_bus_navi.ui.SafetyMarginOption;
import com.example.prj_gifu_univ_bus_navi.ui.UiSelectionFilters;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 42;
    private final MainViewModel viewModel = new MainViewModel();
    private FrameLayout root;
    private Location gpsLocation;
    private String gpsStatusMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        viewModel.loadInitialData(
            LocalBusScheduleData.loadBusTrips(this),
            LocalBusScheduleData.loadDestinationStopNames(this),
            new UserSettingsRepository(getApplicationContext()),
            new WeatherRepository()
        );
        refreshGpsLocation();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewModel.getCurrentScreen() == AppScreen.HOME) {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                } else {
                    viewModel.navigateBack();
                    renderCurrentScreen();
                }
            }
        });
        showHome();
    }

    private void renderCurrentScreen() {
        switch (viewModel.getCurrentScreen()) {
            case SETTINGS:
                showSettings();
                break;
            case RESULT:
                showResult();
                break;
            case ADD_NODE:
                showAddNode();
                break;
            case TRAVEL_TIME_PROFILE:
                showTravelTimeProfile();
                break;
            case HOME:
            default:
                showHome();
                break;
        }
    }

    private void showHome() {
        viewModel.navigate(AppScreen.HOME);
        LinearLayout content = baseContent();
        content.addView(screenTitle("岐大バスナビ"));

        CampusMapView mapView = new CampusMapView(this);
        mapView.setNodes(viewModel.getMapSelectableNodes());
        mapView.setGpsLocation(gpsLocation);
        mapView.setSelectedNodeId(viewModel.getSelectedCurrentNodeId());
        mapView.setOnNodeTapListener(node -> {
            viewModel.selectMapNodeAndRecommend(node.getId());
            showResult();
        });
        content.addView(mapView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        content.addView(fieldLabel("現在地"));
        Spinner currentSpinner = spinner(nodeNames(viewModel.getSelectableStartNodes()));
        setSpinnerSelection(currentSpinner, selectedNodeIndex(viewModel.getSelectableStartNodes(), viewModel.getSelectedCurrentNodeId()));
        currentSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            List<CampusGraphNode> nodes = viewModel.getSelectableStartNodes();
            if (position >= 0 && position < nodes.size()) viewModel.selectCurrentNode(nodes.get(position).getId());
        }));
        content.addView(currentSpinner);
        Switch rainSwitch = new Switch(this);
        rainSwitch.setText("雨の日モード");
        rainSwitch.setTextSize(16);
        rainSwitch.setPadding(0, dp(8), 0, dp(8));
        rainSwitch.setChecked(viewModel.isRainModeEnabled());
        rainSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.updateRainModeEnabled(isChecked));
        content.addView(rainSwitch);
        Button search = primaryButton("最短バスを探す");
        search.setOnClickListener(v -> {
            viewModel.findBestBus();
            showResult();
        });
        content.addView(search);
        Button settings = secondaryButton("設定");
        settings.setOnClickListener(v -> {
            viewModel.navigate(AppScreen.SETTINGS);
            showSettings();
        });
        content.addView(settings);
        setContent(content);
    }

    private void showSettings() {
        viewModel.navigate(AppScreen.SETTINGS);
        LinearLayout content = baseContent();
        content.addView(backButton());
        content.addView(screenTitle("設定"));
        content.addView(fieldLabel("余裕時間"));
        Spinner safety = spinner(optionLabels(viewModel.getSafetyMarginOptions()));
        setSpinnerSelection(safety, viewModel.getSafetyMarginOptions().indexOf(viewModel.getSelectedSafetyMargin()));
        safety.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            if (position >= 0 && position < viewModel.getSafetyMarginOptions().size()) {
                viewModel.selectSafetyMargin(viewModel.getSafetyMarginOptions().get(position));
            }
        }));
        content.addView(safety);
        content.addView(fieldLabel("よく使う出発地点"));
        List<CampusGraphNode> starts = viewModel.getSelectableStartNodes();
        Spinner favorite = spinner(withUnset(nodeNames(starts)));
        setSpinnerSelection(favorite, favoriteStartIndex(starts, viewModel.getFavoriteStartNodeId()));
        favorite.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            viewModel.selectFavoriteStartNode(position <= 0 ? null : starts.get(position - 1).getId());
        }));
        content.addView(favorite);
        content.addView(fieldLabel("降車バス停"));
        Spinner destination = spinner(viewModel.getDestinationStopNames());
        setSpinnerSelection(destination, viewModel.getDestinationStopNames().indexOf(viewModel.getSelectedDestinationStopName()));
        destination.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            if (position >= 0 && position < viewModel.getDestinationStopNames().size()) {
                viewModel.selectDestinationStop(viewModel.getDestinationStopNames().get(position));
            }
        }));
        content.addView(destination);
        Button addNode = secondaryButton("ユーザー追加ノード");
        addNode.setOnClickListener(v -> showAddNode());
        content.addView(addNode);
        Button profile = secondaryButton("移動時間補正設定");
        profile.setOnClickListener(v -> showTravelTimeProfile());
        content.addView(profile);
        setContent(content);
    }

    private void showResult() {
        viewModel.navigate(AppScreen.RESULT);
        LinearLayout content = baseContent();
        content.addView(backButton());
        content.addView(screenTitle("バス候補"));
        RecommendationResult result = viewModel.getRecommendationResult();
        if (result == null || result.getRecommendedCandidate() == null) {
            content.addView(messageLabel("現在時刻以降に乗車可能な便がありません"));
        } else {
            content.addView(sectionLabel("おすすめ候補"));
            content.addView(candidateView(result.getRecommendedCandidate(), true));
            List<BusStopCandidate> others = UiSelectionFilters.displayCandidatesExcludingRecommended(
                result.getAllCandidates(),
                result.getRecommendedCandidate()
            );
            content.addView(sectionLabel("候補一覧"));
            if (others.isEmpty()) {
                content.addView(messageLabel("ほかの候補はありません"));
            }
            for (BusStopCandidate candidate : others) {
                content.addView(candidateView(candidate, false));
            }
        }
        setContent(content);
    }

    private void showAddNode() {
        viewModel.navigate(AppScreen.ADD_NODE);
        LinearLayout content = baseContent();
        content.addView(backButton());
        content.addView(screenTitle("ノード追加"));
        content.addView(fieldLabel("ノード名"));
        EditText name = editText("ノード名", InputType.TYPE_CLASS_TEXT);
        content.addView(name);
        List<CampusGraphNode> connectable = viewModel.getGraphNodes();
        content.addView(fieldLabel("接続先ノード"));
        Spinner connected = spinner(nodeNames(connectable));
        content.addView(connected);
        content.addView(fieldLabel("接続先までの移動時間"));
        EditText minutes = editText("接続先までの移動時間", InputType.TYPE_CLASS_NUMBER);
        content.addView(minutes);
        CheckBox selectable = new CheckBox(this);
        selectable.setText("スタート地点として選択可能");
        selectable.setTextSize(16);
        selectable.setChecked(true);
        content.addView(selectable);
        content.addView(fieldLabel("座標設定方法"));
        Spinner source = spinner(stringList("座標なし", "GPS", "手入力"));
        content.addView(source);
        TextView gpsStatus = messageLabel("");
        gpsStatus.setVisibility(View.GONE);
        content.addView(gpsStatus);
        EditText latitude = editText("緯度", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        EditText longitude = editText("経度", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        latitude.setVisibility(View.GONE);
        longitude.setVisibility(View.GONE);
        content.addView(latitude);
        content.addView(longitude);
        source.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            boolean manual = position == 2;
            latitude.setVisibility(manual ? View.VISIBLE : View.GONE);
            longitude.setVisibility(manual ? View.VISIBLE : View.GONE);
            if (position == 1) {
                gpsStatus.setVisibility(View.VISIBLE);
                gpsStatus.setText("GPS取得中...");
                gpsStatus.setText(refreshGpsLocation());
                if (gpsLocation != null) {
                    latitude.setText(String.valueOf(gpsLocation.getLatitude()));
                    longitude.setText(String.valueOf(gpsLocation.getLongitude()));
                }
            } else if (position == 0) {
                gpsStatus.setVisibility(View.GONE);
                gpsStatus.setText("");
            } else {
                gpsStatus.setVisibility(View.GONE);
            }
        }));
        Button save = primaryButton("保存");
        save.setOnClickListener(v -> {
            int minutesValue = parseInt(minutes.getText().toString(), 1);
            int sourcePosition = source.getSelectedItemPosition();
            Double lat = null;
            Double lon = null;
            UserNodeCoordinateSource coordinateSource = UserNodeCoordinateSource.NONE;
            if (sourcePosition == 1 && gpsLocation != null) {
                lat = gpsLocation.getLatitude();
                lon = gpsLocation.getLongitude();
                coordinateSource = UserNodeCoordinateSource.GPS;
            } else if (sourcePosition == 1) {
                gpsStatus.setVisibility(View.VISIBLE);
                gpsStatus.setText("現在地を取得できませんでした");
                return;
            } else if (sourcePosition == 2) {
                lat = parseDouble(latitude.getText().toString());
                lon = parseDouble(longitude.getText().toString());
                coordinateSource = UserNodeCoordinateSource.MANUAL;
            }
            if (lat != null && lon != null && !MapCoordinateProjector.isInBounds(lat, lon)) {
                gpsStatus.setVisibility(View.VISIBLE);
                gpsStatus.setText("取得座標が大学敷地外です");
                return;
            }
            viewModel.addUserNode(new UserGraphNodeInput(
                name.getText().toString().trim(),
                connectable.get(connected.getSelectedItemPosition()).getId(),
                minutesValue,
                selectable.isChecked(),
                lat,
                lon,
                coordinateSource
            ));
            showSettings();
        });
        content.addView(save);
        setContent(content);
    }

    private void showTravelTimeProfile() {
        viewModel.navigate(AppScreen.TRAVEL_TIME_PROFILE);
        LinearLayout content = baseContent();
        content.addView(backButton());
        content.addView(screenTitle("移動時間補正"));
        List<CampusGraphEdge> edges = viewModel.getEditableEdges();
        content.addView(fieldLabel("基準エッジ"));
        Spinner edgeSpinner = spinner(edgeLabels(edges));
        content.addView(edgeSpinner);
        TextView standard = messageLabel("");
        content.addView(standard);
        content.addView(fieldLabel("実測移動時間"));
        EditText measured = editText("実測移動時間", InputType.TYPE_CLASS_NUMBER);
        content.addView(measured);
        TextView coefficient = messageLabel("");
        content.addView(coefficient);
        Button save = primaryButton("保存");
        Runnable updateProfilePreview = () -> {
            if (edges.isEmpty()) {
                standard.setText("編集できる基準エッジがありません");
                coefficient.setText("");
                save.setEnabled(false);
                return;
            }
            CampusGraphEdge edge = edges.get(edgeSpinner.getSelectedItemPosition());
            int measuredValue = parseInt(measured.getText().toString(), 0);
            standard.setText("標準移動時間: " + edge.getMinutes() + "分");
            if (measuredValue <= 0) {
                coefficient.setText("補正係数: 実測時間を入力してください");
                save.setEnabled(false);
            } else {
                double ratio = (double) measuredValue / (double) edge.getMinutes();
                coefficient.setText(String.format("補正係数: %.2f倍 (標準時間に掛ける倍率)", ratio));
                save.setEnabled(true);
            }
        };
        edgeSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> updateProfilePreview.run()));
        measured.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateProfilePreview.run(); }
            @Override public void afterTextChanged(Editable s) { }
        });
        save.setOnClickListener(v -> {
            if (edges.isEmpty()) return;
            CampusGraphEdge edge = edges.get(edgeSpinner.getSelectedItemPosition());
            int measuredValue = parseInt(measured.getText().toString(), 0);
            if (measuredValue <= 0) return;
            viewModel.saveTravelTimeProfile(edge.getId(), edge.getMinutes(), measuredValue);
            showSettings();
        });
        updateProfilePreview.run();
        content.addView(save);
        setContent(content);
    }

    private TextView candidateView(BusStopCandidate candidate, boolean recommended) {
        StringBuilder sb = new StringBuilder();
        sb.append(candidate.getBusStopName()).append("\n");
        sb.append("発車: ").append(candidate.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm"))).append(" ");
        sb.append("(到着予定: ").append(candidate.getDestinationArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm"))).append(")\n");
        sb.append("徒歩: ").append(candidate.getTravelMinutes()).append("分 ");
        sb.append("余裕: ").append(candidate.getRemainingMinutes()).append("分");
        if (candidate.isMayBeArticulatedBus()) {
            sb.append("\n※連接バスの可能性あり");
        }

        TextView view = label(sb.toString());
        view.setPadding(dp(16), dp(16), dp(16), dp(16));
        view.setLineSpacing(0, 1.2f);
        view.setBackground(cardBackground(recommended ? Color.rgb(225, 245, 254) : Color.rgb(250, 250, 250)));
        if (recommended) {
            view.setTypeface(Typeface.DEFAULT_BOLD);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(4), 0, dp(12));
        view.setLayoutParams(params);
        return view;
    }

    private LinearLayout baseContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(16), dp(20), dp(32));
        content.setBackgroundColor(Color.WHITE);
        return content;
    }

    private void setContent(LinearLayout content) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.WHITE);
        scrollView.addView(content);
        root.removeAllViews();
        root.addView(scrollView);
    }

    private TextView backButton() {
        TextView back = new TextView(this);
        back.setText("← 戻る");
        back.setTextSize(16);
        back.setTextColor(Color.rgb(2, 136, 209));
        back.setPadding(0, dp(8), dp(16), dp(16));
        back.setOnClickListener(v -> {
            viewModel.navigateBack();
            renderCurrentScreen();
        });
        return back;
    }

    private TextView label(String text) {
        TextView view = new TextView(this);
        view.setText(text == null ? "" : text);
        view.setTextSize(15);
        view.setTextColor(Color.rgb(55, 71, 79));
        view.setPadding(0, dp(6), 0, dp(6));
        return view;
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setElevation(dp(2));
        button.setMinHeight(dp(54));
        button.setAllCaps(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(12), 0, dp(8));
        button.setLayoutParams(params);
        return button;
    }

    private Button primaryButton(String text) {
        Button button = button(text);
        button.setTextColor(Color.WHITE);
        button.setBackground(buttonBackground(Color.rgb(2, 136, 209)));
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = button(text);
        button.setTextColor(Color.rgb(2, 136, 209));
        button.setBackground(buttonBackground(Color.rgb(225, 245, 254)));
        button.setElevation(0);
        return button;
    }

    private EditText editText(String hint, int inputType) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setInputType(inputType);
        editText.setTextSize(16);
        editText.setMinHeight(dp(54));
        editText.setPadding(dp(12), dp(12), dp(12), dp(12));
        editText.setBackground(cardBackground(Color.rgb(250, 250, 250)));
        editText.setSingleLine(true);
        return editText;
    }

    private Spinner spinner(List<String> values) {
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values));
        spinner.setMinimumHeight(dp(54));
        spinner.setPadding(dp(8), dp(8), dp(8), dp(8));
        spinner.setBackground(cardBackground(Color.rgb(250, 250, 250)));
        return spinner;
    }

    private TextView screenTitle(String text) {
        TextView view = label(text);
        view.setTextSize(24);
        view.setTextColor(Color.rgb(38, 50, 56));
        view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        view.setPadding(0, dp(8), 0, dp(20));
        return view;
    }

    private TextView sectionLabel(String text) {
        TextView view = label(text);
        view.setTextSize(18);
        view.setTextColor(Color.rgb(69, 90, 100));
        view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        view.setPadding(0, dp(20), 0, dp(8));
        return view;
    }

    private TextView fieldLabel(String text) {
        TextView view = label(text);
        view.setTextSize(14);
        view.setTextColor(Color.rgb(120, 144, 156));
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setPadding(0, dp(16), 0, dp(4));
        return view;
    }

    private TextView messageLabel(String text) {
        TextView view = label(text);
        view.setBackground(cardBackground(Color.rgb(248, 250, 252)));
        view.setPadding(dp(12), dp(10), dp(12), dp(10));
        return view;
    }

    private String refreshGpsLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            gpsStatusMessage = "位置情報権限が必要です";
            return gpsStatusMessage;
        }
        try {
            LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            gpsLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (gpsLocation == null) {
                gpsLocation = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (SecurityException ex) {
            gpsLocation = null;
            gpsStatusMessage = "位置情報権限が必要です";
            return gpsStatusMessage;
        } catch (RuntimeException ex) {
            gpsLocation = null;
            gpsStatusMessage = "現在地を取得できませんでした";
            return gpsStatusMessage;
        }
        if (gpsLocation == null) {
            gpsStatusMessage = "現在地を取得できませんでした";
        } else if (!MapCoordinateProjector.isInBounds(gpsLocation.getLatitude(), gpsLocation.getLongitude())) {
            gpsStatusMessage = "取得座標: " + formatCoordinate(gpsLocation.getLatitude()) + ", " +
                formatCoordinate(gpsLocation.getLongitude()) + "\n取得座標が大学敷地外です";
        } else {
            gpsStatusMessage = "取得座標: " + formatCoordinate(gpsLocation.getLatitude()) + ", " +
                formatCoordinate(gpsLocation.getLongitude());
        }
        return gpsStatusMessage;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            refreshGpsLocation();
            renderCurrentScreen();
        }
    }

    private GradientDrawable cardBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(12));
        drawable.setStroke(dp(1), Color.rgb(236, 239, 241));
        return drawable;
    }

    private GradientDrawable buttonBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(24));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static List<String> nodeNames(List<CampusGraphNode> nodes) {
        List<String> names = new ArrayList<>();
        for (CampusGraphNode node : nodes) names.add(node.getName());
        return names;
    }

    private static List<String> optionLabels(List<SafetyMarginOption> options) {
        List<String> labels = new ArrayList<>();
        for (SafetyMarginOption option : options) labels.add(option.getLabel());
        return labels;
    }

    private static List<String> edgeLabels(List<CampusGraphEdge> edges) {
        List<String> labels = new ArrayList<>();
        for (CampusGraphEdge edge : edges) labels.add(edge.getId() + " (" + edge.getMinutes() + "分)");
        return labels;
    }

    private static List<String> withUnset(List<String> values) {
        List<String> result = new ArrayList<>();
        result.add("未設定");
        result.addAll(values);
        return result;
    }

    private static List<String> stringList(String... values) {
        List<String> result = new ArrayList<>();
        for (String value : values) result.add(value);
        return result;
    }

    private static int selectedNodeIndex(List<CampusGraphNode> nodes, String nodeId) {
        for (int index = 0; index < nodes.size(); index++) {
            if (nodes.get(index).getId().equals(nodeId)) return index;
        }
        return 0;
    }

    private static int favoriteStartIndex(List<CampusGraphNode> nodes, String nodeId) {
        if (nodeId == null) return 0;
        for (int index = 0; index < nodes.size(); index++) {
            if (nodes.get(index).getId().equals(nodeId)) return index + 1;
        }
        return 0;
    }

    private static void setSpinnerSelection(Spinner spinner, int index) {
        if (index >= 0) spinner.setSelection(index);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String formatCoordinate(double value) {
        return String.format("%.6f", value);
    }

    private static final class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {
        interface Callback { void onSelected(int position); }
        private final Callback callback;
        private SimpleItemSelectedListener(Callback callback) { this.callback = callback; }
        @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { callback.onSelected(position); }
        @Override public void onNothingSelected(AdapterView<?> parent) { }
    }
}
