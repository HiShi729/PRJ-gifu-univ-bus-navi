package com.example.prj_gifu_univ_bus_navi.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.prj_gifu_univ_bus_navi.model.BusStopCandidate
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode
import com.example.prj_gifu_univ_bus_navi.model.NodeType
import com.example.prj_gifu_univ_bus_navi.model.RecommendationResult
import com.example.prj_gifu_univ_bus_navi.model.UserGraphNodeInput
import com.example.prj_gifu_univ_bus_navi.model.UserNodeCoordinateSource
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun GifuBusNaviApp(viewModel: MainViewModel) {
    BackHandler(enabled = viewModel.currentScreen != AppScreen.HOME) {
        viewModel.navigateBack()
    }
    MaterialTheme(
        colorScheme = lightColorScheme(),
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    showBack = viewModel.currentScreen != AppScreen.HOME,
                    onBack = viewModel::navigateBack,
                )
            },
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                when (viewModel.currentScreen) {
                    AppScreen.HOME -> HomeScreen(viewModel)
                    AppScreen.RESULT -> ResultScreen(viewModel.recommendationResult, viewModel::goHome)
                    AppScreen.SETTINGS -> SettingsScreen(viewModel)
                    AppScreen.ADD_NODE -> AddNodeScreen(viewModel)
                    AppScreen.EDIT_TRAVEL_TIME -> EditTravelTimeScreen(viewModel)
                    AppScreen.TRAVEL_TIME_PROFILE -> TravelTimeProfileScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(showBack: Boolean, onBack: () -> Unit) {
    TopAppBar(
        title = { Text("岐大バスナビ") },
        navigationIcon = {
            if (showBack) {
                TextButton(onClick = onBack) { Text("＜") }
            }
        },
    )
}

@Composable
private fun HomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var gpsLocation by remember { mutableStateOf<Location?>(null) }
    val homeLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true || grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            gpsLocation = context.lastKnownLocation()
        }
    }
    LaunchedEffect(Unit) {
        if (context.hasLocationPermission()) {
            gpsLocation = context.lastKnownLocation()
        } else {
            homeLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CampusMapView(
            nodes = viewModel.mapSelectableNodes,
            gpsLocation = gpsLocation,
            onNodeSelected = viewModel::selectMapNodeAndRecommend,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CampusMapDefaults.bounds.aspectRatio()),
        )
        CampusNodeDropdown(
            label = "現在地",
            nodes = viewModel.selectableStartNodes,
            selectedNodeId = viewModel.selectedCurrentNodeId,
            onSelected = viewModel::selectCurrentNode,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = viewModel.rainModeEnabled, onCheckedChange = viewModel::updateRainModeEnabled)
            Text("雨の日モード")
        }
        Button(
            onClick = viewModel::findBestBus,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("最短バスを探す")
        }
        OutlinedButton(
            onClick = { viewModel.navigate(AppScreen.SETTINGS) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("設定")
        }
    }
}

@Composable
private fun SettingsScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("設定", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        SafetyMarginDropdown(
            options = viewModel.safetyMarginOptions,
            selected = viewModel.selectedSafetyMargin,
            onSelected = viewModel::selectSafetyMargin,
        )
        FavoriteStartDropdown(
            nodes = viewModel.selectableStartNodes,
            selectedNodeId = viewModel.favoriteStartNodeId,
            onSelected = viewModel::selectFavoriteStartNode,
        )
        DestinationDropdown(
            options = viewModel.destinationStopNames,
            selected = viewModel.selectedDestinationStopName,
            onSelected = viewModel::selectDestinationStop,
        )
        OutlinedButton(
            onClick = { viewModel.navigate(AppScreen.ADD_NODE) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("ユーザー追加ノード") }
        OutlinedButton(
            onClick = { viewModel.navigate(AppScreen.TRAVEL_TIME_PROFILE) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("移動時間補正") }
        Button(onClick = viewModel::goHome, modifier = Modifier.fillMaxWidth()) {
            Text("ホームへ戻る")
        }
    }
}

@Composable
private fun CampusMapView(
    nodes: List<CampusGraphNode>,
    gpsLocation: Location?,
    onNodeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        CampusMapBackground(Modifier.fillMaxSize())
        val mapWidth = maxWidth
        val mapHeight = maxHeight
        if (MapCoordinateProjector.isGpsLocationVisibleOnCampusMap(gpsLocation?.latitude, gpsLocation?.longitude)) {
            val gpsPoint = MapCoordinateProjector.project(
                gpsLocation!!.latitude,
                gpsLocation.longitude,
                mapWidth.value,
                mapHeight.value,
            )
            if (gpsPoint != null) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = gpsPoint.x.dp - 7.dp,
                            y = gpsPoint.y.dp - 7.dp,
                        )
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00A152)),
                )
            }
        }
        nodes.forEach { node ->
            val point = MapCoordinateProjector.project(
                node.latitude ?: return@forEach,
                node.longitude ?: return@forEach,
                mapWidth.value,
                mapHeight.value,
            ) ?: return@forEach
            Column(
                modifier = Modifier
                    .offset(
                        x = point.x.dp - 12.dp,
                        y = point.y.dp - 12.dp,
                    )
                    .clickable { onNodeSelected(node.id) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (node.nodeType == NodeType.BUS_STOP) 20.dp else 16.dp)
                        .clip(CircleShape)
                        .background(mapPinColor(node.nodeType)),
                )
                Text(node.name, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun CampusMapBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(Color(0xFFEAF3E5))
        drawLine(
            color = Color(0xFFB9BEC6),
            start = Offset(size.width * 0.12f, size.height * 0.18f),
            end = Offset(size.width * 0.88f, size.height * 0.82f),
            strokeWidth = 18f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color(0xFFC8CCD2),
            start = Offset(size.width * 0.2f, size.height * 0.78f),
            end = Offset(size.width * 0.82f, size.height * 0.28f),
            strokeWidth = 12f,
            cap = StrokeCap.Round,
        )
        drawRoundRect(Color(0xFFD8E3F3), topLeft = Offset(size.width * 0.13f, size.height * 0.16f), size = Size(size.width * 0.2f, size.height * 0.16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
        drawRoundRect(Color(0xFFF1E5C9), topLeft = Offset(size.width * 0.44f, size.height * 0.18f), size = Size(size.width * 0.22f, size.height * 0.14f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
        drawRoundRect(Color(0xFFDCEAD5), topLeft = Offset(size.width * 0.52f, size.height * 0.5f), size = Size(size.width * 0.28f, size.height * 0.2f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
        drawRoundRect(Color(0xFFF0D6D6), topLeft = Offset(size.width * 0.1f, size.height * 0.62f), size = Size(size.width * 0.22f, size.height * 0.18f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
    }
}

private fun mapPinColor(nodeType: NodeType): Color = when (nodeType) {
    NodeType.BUS_STOP -> Color(0xFFD32F2F)
    NodeType.USER_ADDED -> Color(0xFF2E7D32)
    else -> Color(0xFF1565C0)
}

@Composable
private fun ResultScreen(result: RecommendationResult?, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val recommended = result?.recommendedCandidate
        if (recommended == null) {
            Text(result?.message ?: "現在時刻以降に乗車可能な便がありません", style = MaterialTheme.typography.titleMedium)
        } else {
            Text("おすすめ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            CandidateCard(recommended, emphasized = true)
        }
        val displayCandidates = displayCandidatesExcludingRecommended(result?.allCandidates.orEmpty(), recommended)
        Text("候補一覧", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        displayCandidates.forEach { candidate ->
            CandidateCard(candidate, emphasized = false)
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("条件を変更する")
        }
    }
}

@Composable
private fun CandidateCard(candidate: BusStopCandidate, emphasized: Boolean) {
    Card(
        colors = if (emphasized) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${candidate.busStopName} ${candidate.departureTime.format(timeFormatter)}発", fontWeight = FontWeight.Bold)
            Text("到着予定時刻: ${candidate.destinationArrivalTime.formatNullable()}")
            Text("移動時間: ${candidate.travelMinutes}分 / 到着予想: ${candidate.arrivalTimeAtBusStop.format(timeFormatter)}")
            Text("発車までの余裕: ${candidate.remainingMinutes}分")
            Text("路線: ${candidate.routeName} / ${if (candidate.canCatch) "乗車可能" else "乗車不可"}")
            if (candidate.mayBeArticulatedBus) {
                Text("連接バスの可能性あり", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AddNodeScreen(viewModel: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var connectedNodeId by remember { mutableStateOf(viewModel.graphNodes.first().id) }
    var minutesText by remember { mutableStateOf("3") }
    var selectable by remember { mutableStateOf(true) }
    var coordinateSource by remember { mutableStateOf(UserNodeCoordinateSource.NONE) }
    var latitudeText by remember { mutableStateOf("") }
    var longitudeText by remember { mutableStateOf("") }
    var coordinateMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    fun applyLastKnownLocation() {
        val location = context.lastKnownLocation()
        if (location == null) {
            coordinateMessage = "現在地を取得できませんでした。手入力してください。"
        } else {
            latitudeText = location.latitude.toString()
            longitudeText = location.longitude.toString()
            coordinateMessage = if (MapCoordinateProjector.isInBounds(location.latitude, location.longitude)) {
                "現在地を取得しました"
            } else {
                "現在地が大学敷地外です"
            }
        }
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true || grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            applyLastKnownLocation()
        } else {
            coordinateMessage = "位置情報権限が許可されていません。手入力してください。"
        }
    }
    fun requestCurrentLocation() {
        coordinateMessage = "取得中"
        if (context.hasLocationPermission()) {
            applyLastKnownLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }
    val minutes = minutesText.toIntOrNull()
    val latitude = latitudeText.toDoubleOrNull()
    val longitude = longitudeText.toDoubleOrNull()
    val coordinateInBounds = latitude != null && longitude != null && MapCoordinateProjector.isInBounds(latitude, longitude)
    val hasValidCoordinates = when (coordinateSource) {
        UserNodeCoordinateSource.NONE -> true
        UserNodeCoordinateSource.GPS -> coordinateInBounds
        UserNodeCoordinateSource.MANUAL -> latitude != null && longitude != null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("地点名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        CampusNodeDropdown(
            label = "接続先",
            nodes = viewModel.graphNodes,
            selectedNodeId = connectedNodeId,
            onSelected = { connectedNodeId = it },
        )
        OutlinedTextField(
            value = minutesText,
            onValueChange = { minutesText = it.filter(Char::isDigit) },
            label = { Text("接続先までの移動時間（分）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = selectable, onCheckedChange = { selectable = it })
            Text("スタート地点として選択可能にする")
        }
        CoordinateSourceDropdown(
            selected = coordinateSource,
            onSelected = { source ->
                coordinateSource = source
                coordinateMessage = ""
                if (source == UserNodeCoordinateSource.NONE) {
                    latitudeText = ""
                    longitudeText = ""
                }
                if (source == UserNodeCoordinateSource.GPS) {
                    requestCurrentLocation()
                }
            },
        )
        if (coordinateSource == UserNodeCoordinateSource.GPS) {
            Text("取得座標: ${latitudeText.ifBlank { "未取得" }}, ${longitudeText.ifBlank { "未取得" }}")
            if (coordinateMessage.isNotBlank()) {
                Text(coordinateMessage)
            }
        }
        if (coordinateSource == UserNodeCoordinateSource.MANUAL) {
            OutlinedTextField(
                value = latitudeText,
                onValueChange = { latitudeText = it.filterCoordinateChars() },
                label = { Text("latitude") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = longitudeText,
                onValueChange = { longitudeText = it.filterCoordinateChars() },
                label = { Text("longitude") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        if (coordinateSource == UserNodeCoordinateSource.GPS && latitude != null && longitude != null && !coordinateInBounds) {
            Text("大学敷地外のため保存できません", color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = {
                viewModel.addUserNode(
                    UserGraphNodeInput(
                        name = name.trim(),
                        connectedNodeId = connectedNodeId,
                        minutesToConnectedNode = minutes ?: 1,
                        isSelectableAsStart = selectable,
                        latitude = if (coordinateSource == UserNodeCoordinateSource.NONE) null else latitude,
                        longitude = if (coordinateSource == UserNodeCoordinateSource.NONE) null else longitude,
                        coordinateSource = coordinateSource,
                    ),
                )
            },
            enabled = name.isNotBlank() && minutes != null && minutes > 0 && hasValidCoordinates,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("保存")
        }
    }
}

@Composable
private fun EditTravelTimeScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        viewModel.editableEdges.forEach { edge ->
            EdgeEditor(edge, viewModel.graphNodes, viewModel.edgeOverrideMinutes(edge.id), viewModel::setEdgeOverride)
        }
        Button(onClick = { viewModel.navigate(AppScreen.SETTINGS) }, modifier = Modifier.fillMaxWidth()) {
            Text("保存して戻る")
        }
    }
}

@Composable
private fun TravelTimeProfileScreen(viewModel: MainViewModel) {
    val edges = viewModel.editableEdges
    var selectedEdgeId by remember { mutableStateOf(edges.firstOrNull()?.id.orEmpty()) }
    var measuredText by remember { mutableStateOf(viewModel.userTravelTimeProfile?.measuredMinutes?.toString().orEmpty()) }
    val selectedEdge = edges.firstOrNull { it.id == selectedEdgeId }
    val measuredMinutes = measuredText.toIntOrNull()
    val factor = if (selectedEdge != null && measuredMinutes != null && selectedEdge.minutes > 0 && measuredMinutes > 0) {
        measuredMinutes.toDouble() / selectedEdge.minutes.toDouble()
    } else {
        null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("移動時間補正", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        EdgeDropdown(
            label = "基準エッジ",
            edges = edges,
            nodes = viewModel.graphNodes,
            selectedEdgeId = selectedEdgeId,
            onSelected = { selectedEdgeId = it },
        )
        Text("標準移動時間: ${selectedEdge?.minutes ?: 0}分")
        OutlinedTextField(
            value = measuredText,
            onValueChange = { measuredText = it.filter(Char::isDigit) },
            label = { Text("実測移動時間（分）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Text("補正係数: ${factor?.let { "%.2f".format(it) } ?: "-"}")
        factor?.let {
            Text("あなたの移動時間は標準の約${(it * 100).toInt()}%として計算されます")
            if (it < 0.3 || it > 2.0) {
                Text("補正係数が極端です。入力値を確認してください。", color = MaterialTheme.colorScheme.error)
            }
        }
        Button(
            onClick = {
                val edge = selectedEdge ?: return@Button
                val measured = measuredMinutes ?: return@Button
                viewModel.saveTravelTimeProfile(edge.id, edge.minutes, measured)
            },
            enabled = selectedEdge != null && measuredMinutes != null && measuredMinutes > 0 && selectedEdge.minutes > 0,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("保存")
        }
        OutlinedButton(onClick = { viewModel.navigate(AppScreen.SETTINGS) }, modifier = Modifier.fillMaxWidth()) {
            Text("戻る")
        }
    }
}

@Composable
private fun EdgeEditor(
    edge: CampusGraphEdge,
    nodes: List<CampusGraphNode>,
    overrideMinutes: Int?,
    onChanged: (String, Int?) -> Unit,
) {
    var text by remember(edge.id, overrideMinutes) { mutableStateOf(overrideMinutes?.toString().orEmpty()) }
    val from = nodes.firstOrNull { it.id == edge.fromNodeId }?.name ?: edge.fromNodeId
    val to = nodes.firstOrNull { it.id == edge.toNodeId }?.name ?: edge.toNodeId
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$from - $to", fontWeight = FontWeight.Bold)
            Text("標準: ${edge.minutes}分")
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it.filter(Char::isDigit)
                    onChanged(edge.id, text.toIntOrNull())
                },
                label = { Text("ユーザー固有時間（分）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EdgeDropdown(
    label: String,
    edges: List<CampusGraphEdge>,
    nodes: List<CampusGraphNode>,
    selectedEdgeId: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedEdge = edges.firstOrNull { it.id == selectedEdgeId }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedEdge?.displayName(nodes).orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            edges.forEach { edge ->
                DropdownMenuItem(
                    text = { Text(edge.displayName(nodes)) },
                    onClick = {
                        onSelected(edge.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun CampusGraphEdge.displayName(nodes: List<CampusGraphNode>): String {
    val from = nodes.firstOrNull { it.id == fromNodeId }?.name ?: fromNodeId
    val to = nodes.firstOrNull { it.id == toNodeId }?.name ?: toNodeId
    return "$from - $to"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampusNodeDropdown(
    label: String,
    nodes: List<CampusGraphNode>,
    selectedNodeId: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = nodes.firstOrNull { it.id == selectedNodeId }?.name.orEmpty()
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            nodes.forEach { node ->
                DropdownMenuItem(
                    text = { Text(node.name) },
                    onClick = {
                        onSelected(node.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoordinateSourceDropdown(
    selected: UserNodeCoordinateSource,
    onSelected: (UserNodeCoordinateSource) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.displayName(),
            onValueChange = {},
            readOnly = true,
            label = { Text("座標設定") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            UserNodeCoordinateSource.entries.forEach { source ->
                DropdownMenuItem(
                    text = { Text(source.displayName()) },
                    onClick = {
                        onSelected(source)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun UserNodeCoordinateSource.displayName(): String = when (this) {
    UserNodeCoordinateSource.NONE -> "座標なし"
    UserNodeCoordinateSource.GPS -> "GPS"
    UserNodeCoordinateSource.MANUAL -> "手入力"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteStartDropdown(
    nodes: List<CampusGraphNode>,
    selectedNodeId: String?,
    onSelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = selectedNodeId?.let { id -> nodes.firstOrNull { it.id == id }?.name } ?: "未設定"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("よく使う出発地点") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("未設定") },
                onClick = {
                    onSelected(null)
                    expanded = false
                },
            )
            nodes.forEach { node ->
                DropdownMenuItem(
                    text = { Text(node.name) },
                    onClick = {
                        onSelected(node.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinationDropdown(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("降車バス停") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { destination ->
                DropdownMenuItem(
                    text = { Text(destination) },
                    onClick = {
                        onSelected(destination)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SafetyMarginDropdown(
    options: List<SafetyMarginOption>,
    selected: SafetyMarginOption,
    onSelected: (SafetyMarginOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("余裕時間") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun LocalTime?.formatNullable(): String = this?.format(timeFormatter) ?: "未設定"

private fun String.filterCoordinateChars(): String =
    filter { it.isDigit() || it == '.' || it == '-' }

private fun Context.lastKnownLocation(): Location? {
    if (!hasLocationPermission()) {
        return null
    }
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    return try {
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    } catch (_: SecurityException) {
        null
    }
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
