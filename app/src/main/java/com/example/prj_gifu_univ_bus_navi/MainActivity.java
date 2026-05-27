package com.example.prj_gifu_univ_bus_navi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.main);
        viewModel.loadInitialData(
            LocalBusScheduleData.loadBusTrips(this),
            LocalBusScheduleData.loadDestinationStopNames(this),
            new UserSettingsRepository(getApplicationContext()),
            new WeatherRepository()
        );
        requestGpsLocation();
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
        CampusMapView mapView = new CampusMapView(this);
        mapView.setNodes(viewModel.getMapSelectableNodes());
        mapView.setGpsLocation(gpsLocation);
        mapView.setOnNodeTapListener(node -> {
            viewModel.selectMapNodeAndRecommend(node.getId());
            showResult();
        });
        content.addView(mapView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        content.addView(label("現在地"));
        Spinner currentSpinner = spinner(nodeNames(viewModel.getSelectableStartNodes()));
        setSpinnerSelection(currentSpinner, selectedNodeIndex(viewModel.getSelectableStartNodes(), viewModel.getSelectedCurrentNodeId()));
        currentSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            List<CampusGraphNode> nodes = viewModel.getSelectableStartNodes();
            if (position >= 0 && position < nodes.size()) viewModel.selectCurrentNode(nodes.get(position).getId());
        }));
        content.addView(currentSpinner);
        Switch rainSwitch = new Switch(this);
        rainSwitch.setText("雨の日モード");
        rainSwitch.setChecked(viewModel.isRainModeEnabled());
        rainSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.updateRainModeEnabled(isChecked));
        content.addView(rainSwitch);
        Button search = button("最短バスを探す");
        search.setOnClickListener(v -> {
            viewModel.findBestBus();
            showResult();
        });
        content.addView(search);
        Button settings = button("設定");
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
        content.addView(label("余裕時間"));
        Spinner safety = spinner(optionLabels(viewModel.getSafetyMarginOptions()));
        setSpinnerSelection(safety, viewModel.getSafetyMarginOptions().indexOf(viewModel.getSelectedSafetyMargin()));
        safety.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            if (position >= 0 && position < viewModel.getSafetyMarginOptions().size()) {
                viewModel.selectSafetyMargin(viewModel.getSafetyMarginOptions().get(position));
            }
        }));
        content.addView(safety);
        content.addView(label("よく使う出発地点"));
        List<CampusGraphNode> starts = viewModel.getSelectableStartNodes();
        Spinner favorite = spinner(withUnset(nodeNames(starts)));
        favorite.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            viewModel.selectFavoriteStartNode(position <= 0 ? null : starts.get(position - 1).getId());
        }));
        content.addView(favorite);
        content.addView(label("降車バス停"));
        Spinner destination = spinner(viewModel.getDestinationStopNames());
        setSpinnerSelection(destination, viewModel.getDestinationStopNames().indexOf(viewModel.getSelectedDestinationStopName()));
        destination.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            if (position >= 0 && position < viewModel.getDestinationStopNames().size()) {
                viewModel.selectDestinationStop(viewModel.getDestinationStopNames().get(position));
            }
        }));
        content.addView(destination);
        Button addNode = button("ユーザー追加ノード");
        addNode.setOnClickListener(v -> showAddNode());
        content.addView(addNode);
        Button profile = button("移動時間補正設定");
        profile.setOnClickListener(v -> showTravelTimeProfile());
        content.addView(profile);
        setContent(content);
    }

    private void showResult() {
        viewModel.navigate(AppScreen.RESULT);
        LinearLayout content = baseContent();
        content.addView(backButton());
        RecommendationResult result = viewModel.getRecommendationResult();
        if (result == null || result.getRecommendedCandidate() == null) {
            content.addView(label(result == null ? "現在時刻以降に乗車可能な便がありません" : result.getMessage()));
        } else {
            content.addView(label("おすすめ"));
            content.addView(candidateView(result.getRecommendedCandidate()));
            List<BusStopCandidate> others = UiSelectionFilters.displayCandidatesExcludingRecommended(
                result.getAllCandidates(),
                result.getRecommendedCandidate()
            );
            content.addView(label("候補一覧"));
            for (BusStopCandidate candidate : others) {
                content.addView(candidateView(candidate));
            }
        }
        setContent(content);
    }

    private void showAddNode() {
        viewModel.navigate(AppScreen.ADD_NODE);
        LinearLayout content = baseContent();
        content.addView(backButton());
        EditText name = editText("ノード名", InputType.TYPE_CLASS_TEXT);
        content.addView(name);
        List<CampusGraphNode> connectable = viewModel.getGraphNodes();
        Spinner connected = spinner(nodeNames(connectable));
        content.addView(connected);
        EditText minutes = editText("接続先までの移動時間", InputType.TYPE_CLASS_NUMBER);
        content.addView(minutes);
        CheckBox selectable = new CheckBox(this);
        selectable.setText("スタート地点として選択可能");
        selectable.setChecked(true);
        content.addView(selectable);
        Spinner source = spinner(stringList("座標なし", "GPS", "手入力"));
        content.addView(source);
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
                requestGpsLocation();
                if (gpsLocation != null) {
                    latitude.setText(String.valueOf(gpsLocation.getLatitude()));
                    longitude.setText(String.valueOf(gpsLocation.getLongitude()));
                }
            }
        }));
        Button save = button("保存");
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
            } else if (sourcePosition == 2) {
                lat = parseDouble(latitude.getText().toString());
                lon = parseDouble(longitude.getText().toString());
                coordinateSource = UserNodeCoordinateSource.MANUAL;
            }
            if (lat != null && lon != null && !MapCoordinateProjector.isInBounds(lat, lon)) return;
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
        List<CampusGraphEdge> edges = viewModel.getEditableEdges();
        Spinner edgeSpinner = spinner(edgeLabels(edges));
        content.addView(edgeSpinner);
        EditText measured = editText("実測移動時間", InputType.TYPE_CLASS_NUMBER);
        content.addView(measured);
        Button save = button("保存");
        save.setOnClickListener(v -> {
            if (edges.isEmpty()) return;
            CampusGraphEdge edge = edges.get(edgeSpinner.getSelectedItemPosition());
            viewModel.saveTravelTimeProfile(edge.getId(), edge.getMinutes(), parseInt(measured.getText().toString(), edge.getMinutes()));
            showSettings();
        });
        content.addView(save);
        setContent(content);
    }

    private TextView candidateView(BusStopCandidate candidate) {
        String text = candidate.getBusStopName() +
            "  発車 " + candidate.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
            "  到着 " + candidate.getDestinationArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
            "\n徒歩 " + candidate.getTravelMinutes() + "分  余裕 " + candidate.getRemainingMinutes() + "分" +
            (candidate.isMayBeArticulatedBus() ? "  連接バス可能性あり" : "");
        TextView view = label(text);
        view.setPadding(12, 12, 12, 12);
        return view;
    }

    private LinearLayout baseContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 24, 24, 24);
        return content;
    }

    private void setContent(LinearLayout content) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(content);
        root.removeAllViews();
        root.addView(scrollView);
    }

    private TextView backButton() {
        TextView back = label("＜");
        back.setTextSize(28);
        back.setOnClickListener(v -> {
            viewModel.navigateBack();
            renderCurrentScreen();
        });
        return back;
    }

    private TextView label(String text) {
        TextView view = new TextView(this);
        view.setText(text == null ? "" : text);
        view.setTextSize(16);
        view.setPadding(0, 10, 0, 10);
        return view;
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        return button;
    }

    private EditText editText(String hint, int inputType) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setInputType(inputType);
        return editText;
    }

    private Spinner spinner(List<String> values) {
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values));
        return spinner;
    }

    private void requestGpsLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        gpsLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (gpsLocation == null) {
            gpsLocation = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
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

    private static final class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {
        interface Callback { void onSelected(int position); }
        private final Callback callback;
        private SimpleItemSelectedListener(Callback callback) { this.callback = callback; }
        @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { callback.onSelected(position); }
        @Override public void onNothingSelected(AdapterView<?> parent) { }
    }
}
