package com.example.prj_gifu_univ_bus_navi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.prj_gifu_univ_bus_navi.model.BusStopCandidate
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphEdge
import com.example.prj_gifu_univ_bus_navi.model.CampusGraphNode
import com.example.prj_gifu_univ_bus_navi.model.DestinationBusStop
import com.example.prj_gifu_univ_bus_navi.model.RecommendationResult
import com.example.prj_gifu_univ_bus_navi.model.UserGraphNodeInput
import com.example.prj_gifu_univ_bus_navi.model.displayName
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

@Composable
fun GifuBusNaviApp(viewModel: MainViewModel) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    showBack = viewModel.currentScreen != AppScreen.HOME,
                    onBack = viewModel::goHome,
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
                    AppScreen.MAP_SELECT -> MapSelectScreen(viewModel)
                    AppScreen.ADD_NODE -> AddNodeScreen(viewModel)
                    AppScreen.EDIT_TRAVEL_TIME -> EditTravelTimeScreen(viewModel)
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
                TextButton(onClick = onBack) { Text("戻る") }
            }
        },
    )
}

@Composable
private fun HomeScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("岐大バスナビ", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        CampusNodeDropdown(
            label = "現在地",
            nodes = viewModel.selectableStartNodes,
            selectedNodeId = viewModel.selectedCurrentNodeId,
            onSelected = viewModel::selectCurrentNode,
        )
        OutlinedButton(
            onClick = { viewModel.navigate(AppScreen.MAP_SELECT) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("地図から選択する")
        }
        Text("現在日付: ${viewModel.currentDate.format(dateFormatter)}")
        Text("現在時刻: ${viewModel.currentTime.format(timeFormatter)}")
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
        DestinationDropdown(
            selected = viewModel.selectedDestination,
            onSelected = viewModel::selectDestination,
        )
        SafetyMarginDropdown(
            options = viewModel.safetyMarginOptions,
            selected = viewModel.selectedSafetyMargin,
            onSelected = viewModel::selectSafetyMargin,
        )
        OutlinedButton(
            onClick = { viewModel.navigate(AppScreen.ADD_NODE) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("ユーザー追加ノード") }
        OutlinedButton(
            onClick = { viewModel.navigate(AppScreen.EDIT_TRAVEL_TIME) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("移動時間編集") }
        Button(onClick = viewModel::goHome, modifier = Modifier.fillMaxWidth()) {
            Text("ホームへ戻る")
        }
    }
}

@Composable
private fun MapSelectScreen(viewModel: MainViewModel) {
    val nodes = viewModel.mapSelectableNodes
    val selectedNode = nodes.firstOrNull { it.id == viewModel.selectedMapNodeId } ?: nodes.firstOrNull()
    val minLatitude = nodes.mapNotNull { it.latitude }.minOrNull() ?: 0.0
    val maxLatitude = nodes.mapNotNull { it.latitude }.maxOrNull() ?: 1.0
    val minLongitude = nodes.mapNotNull { it.longitude }.minOrNull() ?: 0.0
    val maxLongitude = nodes.mapNotNull { it.longitude }.maxOrNull() ?: 1.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("地図から現在地を選択", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            val mapWidth = maxWidth
            val mapHeight = maxHeight
            nodes.forEach { node ->
                val xRatio = normalizedRatio(node.longitude ?: minLongitude, minLongitude, maxLongitude)
                val yRatio = 1f - normalizedRatio(node.latitude ?: minLatitude, minLatitude, maxLatitude)
                val isSelected = node.id == selectedNode?.id
                Column(
                    modifier = Modifier
                        .offset(
                            x = mapWidth * xRatio - 12.dp,
                            y = mapHeight * yRatio - 12.dp,
                        )
                        .clickable { viewModel.selectMapNode(node.id) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 22.dp else 16.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary),
                    )
                    Text(node.name, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text("選択中: ${selectedNode?.name ?: "未選択"}")
        Button(
            onClick = {
                selectedNode?.id?.let { nodeId ->
                    viewModel.selectMapNode(nodeId)
                    viewModel.confirmMapNodeSelection()
                }
            },
            enabled = selectedNode != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("この地点を現在地にする")
        }
        OutlinedButton(onClick = viewModel::goHome, modifier = Modifier.fillMaxWidth()) {
            Text("戻る")
        }
    }
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
            Text("降車予定: ${candidate.actualArrivalBusStop.displayName()} ${candidate.destinationArrivalTime.formatNullable()}")
            Text("移動時間: ${candidate.travelMinutes}分 / 到着予想: ${candidate.arrivalTimeAtBusStop.format(timeFormatter)}")
            Text("発車までの余裕: ${candidate.remainingMinutes}分")
            Text("路線: ${candidate.routeName} / ${if (candidate.canCatch) "乗車可能" else "乗車不可"}")
            Text("理由: ${candidate.reason}")
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
    val minutes = minutesText.toIntOrNull()

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
        Button(
            onClick = {
                viewModel.addUserNode(
                    UserGraphNodeInput(
                        name = name.trim(),
                        connectedNodeId = connectedNodeId,
                        minutesToConnectedNode = minutes ?: 1,
                        isSelectableAsStart = selectable,
                    ),
                )
            },
            enabled = name.isNotBlank() && minutes != null && minutes > 0,
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
        Button(onClick = viewModel::goHome, modifier = Modifier.fillMaxWidth()) {
            Text("保存して戻る")
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
private fun DestinationDropdown(selected: DestinationBusStop, onSelected: (DestinationBusStop) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.displayName(),
            onValueChange = {},
            readOnly = true,
            label = { Text("降車バス停") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DestinationBusStop.entries.forEach { destination ->
                DropdownMenuItem(
                    text = { Text(destination.displayName()) },
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

private fun normalizedRatio(value: Double, min: Double, max: Double): Float {
    if (max == min) return 0.5f
    return ((value - min) / (max - min)).toFloat().coerceIn(0f, 1f)
}
