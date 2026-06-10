const ORIGINAL_WIDTH = 1632;
const ORIGINAL_HEIGHT = 1904;
const DEFAULT_WALKING_SPEED_METERS_PER_SECOND = 1.2;
const EARTH_RADIUS_METERS = 6371000;
const TRANSFORM = {
  A: 3.10327166e-8,
  B: -5.61667690e-6,
  C: 35.47025654998892,
  D: 6.87971687e-6,
  E: -1.51774483e-8,
  F: 136.7318515502264,
};
TRANSFORM.DET = TRANSFORM.A * TRANSFORM.E - TRANSFORM.B * TRANSFORM.D;

let nodes = [];
let edges = [];
let nodeTypes = [];
let selectedNodeId = "";
let selectedEdgeIndex = -1;
let mode = "select";
let pendingFrom = "";
let shiftEdgeIndexes = [];

const el = {
  mapStage: document.getElementById("mapStage"),
  mapFrame: document.getElementById("mapFrame"),
  mapImage: document.getElementById("mapImage"),
  overlay: document.getElementById("overlay"),
  nodeTable: document.getElementById("nodeTable"),
  edgeTable: document.getElementById("edgeTable"),
  summary: document.getElementById("summary"),
  validation: document.getElementById("validation"),
  status: document.getElementById("status"),
  labelToggle: document.getElementById("labelToggle"),
  edgeToggle: document.getElementById("edgeToggle"),
  selectModeButton: document.getElementById("selectModeButton"),
  nodeModeButton: document.getElementById("nodeModeButton"),
  edgeModeButton: document.getElementById("edgeModeButton"),
};

document.getElementById("reloadButton").addEventListener("click", loadGraph);
document.getElementById("saveButton").addEventListener("click", saveGraph);
document.getElementById("addNodeButton").addEventListener("click", addNode);
document.getElementById("addEdgeButton").addEventListener("click", () => addEdge("", ""));
el.labelToggle.addEventListener("change", render);
el.edgeToggle.addEventListener("change", render);
el.selectModeButton.addEventListener("click", () => setMode("select"));
el.nodeModeButton.addEventListener("click", () => setMode("add-node"));
el.edgeModeButton.addEventListener("click", () => setMode("edge"));
el.mapFrame.addEventListener("click", (event) => {
  if (mode !== "add-node" || event.target !== el.overlay) return;
  const pixel = eventToImagePixel(event);
  addNodeAtPixel(pixel.x, pixel.y);
  setStatus("クリック位置にノードを追加しました");
});
el.mapImage.addEventListener("load", syncMapImageSize);
window.addEventListener("resize", syncMapImageSize);
document.addEventListener("keydown", (event) => {
  if (event.key !== "Escape") return;
  clearTransientState();
  setMode("select", false);
  render();
  setStatus("選択状態を解除しました");
});

loadGraph();

async function loadGraph() {
  setStatus("読み込み中...");
  const response = await fetch("/api/graph");
  const graph = await response.json();
  nodes = graph.nodes || [];
  edges = graph.edges || [];
  nodeTypes = graph.nodeTypes || [];
  selectedNodeId = nodes[0]?.id || "";
  selectedEdgeIndex = -1;
  shiftEdgeIndexes = [];
  pendingFrom = "";
  render(graph.summary);
  syncMapImageSize();
  setStatus("読み込みました");
}

async function saveGraph() {
  const local = validate();
  if (local.errors.length > 0) {
    renderValidation(local);
    setStatus("重大エラーがあるため保存しません");
    return;
  }
  setStatus("保存中...");
  const response = await fetch("/api/graph", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nodes, edges }),
  });
  const result = await response.json();
  if (!response.ok) {
    renderValidation({ errors: result.errors || [], warnings: result.warnings || [] });
    setStatus("保存に失敗しました");
    return;
  }
  nodes = result.nodes || nodes;
  edges = result.edges || edges;
  render(result.summary);
  renderValidation({ errors: [], warnings: result.warnings || [] });
  setStatus("保存しました");
}

function render(serverSummary) {
  renderMap();
  renderNodeTable();
  renderEdgeTable();
  const local = validate();
  renderSummary(serverSummary || local.summary);
  renderValidation(local);
}

function renderMap() {
  el.overlay.innerHTML = "";
  el.overlay.classList.toggle("show-labels", el.labelToggle.checked);
  el.mapStage.classList.toggle("add-node-mode", mode === "add-node");
  const showEdges = el.edgeToggle.checked;
  if (showEdges) {
    edges.forEach((edge, index) => {
      const from = nodes.find((node) => node.id === edge.from);
      const to = nodes.find((node) => node.id === edge.to);
      if (!from || !to) return;
      const a = latLonToPixel(Number(from.latitude), Number(from.longitude));
      const b = latLonToPixel(Number(to.latitude), Number(to.longitude));
      const classes = [
        "edge-line",
        index === selectedEdgeIndex ? "selected" : "",
        shiftEdgeIndexes.includes(index) ? "shift-selected" : "",
      ].filter(Boolean).join(" ");
      const groupClasses = [
        "edge-group",
        shiftEdgeIndexes.includes(index) ? "shift-selected" : "",
      ].filter(Boolean).join(" ");
      const group = svg("g", { class: groupClasses });
      const line = svg("line", { x1: a.x, y1: a.y, x2: b.x, y2: b.y, class: classes });
      line.addEventListener("click", (event) => {
        event.stopPropagation();
        selectEdge(index, true, event);
      });
      group.appendChild(line);
      group.appendChild(svg("text", { x: (a.x + b.x) / 2, y: (a.y + b.y) / 2, class: "edge-label" }, formatTime(edge.travelTimeSeconds)));
      el.overlay.appendChild(group);
    });
  }
  nodes.forEach((node) => {
    const point = latLonToPixel(Number(node.latitude), Number(node.longitude));
    const group = svg("g", { class: "node-group" });
    const dot = svg("circle", {
      cx: point.x,
      cy: point.y,
      r: node.type === "BUS_STOP" ? 18 : 14,
      class: [
        "node-dot",
        node.type === "BUS_STOP" ? "bus" : "",
        node.visible === false ? "hidden-node" : "",
        node.id === selectedNodeId ? "selected" : "",
      ].filter(Boolean).join(" "),
    });
    dot.addEventListener("click", (event) => {
      event.stopPropagation();
      selectNode(node.id);
    });
    group.appendChild(dot);
    group.appendChild(svg("text", { x: point.x + 18, y: point.y - 14, class: "node-label" }, node.name || node.id));
    el.overlay.appendChild(group);
  });
}

function renderNodeTable() {
  el.nodeTable.innerHTML = `<thead><tr><th>id</th><th>name</th><th>type</th><th>visible</th><th>latitude</th><th>longitude</th><th>操作</th></tr></thead>`;
  const tbody = document.createElement("tbody");
  nodes.forEach((node, index) => {
    const row = document.createElement("tr");
    if (node.id === selectedNodeId) row.classList.add("selected");
    row.innerHTML = `
      <td><input value="${html(node.id)}"></td>
      <td><input value="${html(node.name)}"></td>
      <td>${selectHtml(nodeTypes, node.type)}</td>
      <td><input type="checkbox" ${node.visible ? "checked" : ""}></td>
      <td><input class="narrow" type="number" step="0.0000001" value="${node.latitude ?? ""}"></td>
      <td><input class="narrow" type="number" step="0.0000001" value="${node.longitude ?? ""}"></td>
      <td>
        ${hasNodeCoordinates(node) ? `<button class="nearest-edge-button" type="button">最寄り接続</button>` : ""}
        <button class="delete-node-button" type="button">削除</button>
      </td>`;
    const inputs = row.querySelectorAll("input,select");
    inputs[0].addEventListener("input", () => {
      const oldId = node.id;
      node.id = inputs[0].value.trim();
      edges.forEach((edge) => {
        if (edge.from === oldId) edge.from = node.id;
        if (edge.to === oldId) edge.to = node.id;
      });
      selectedNodeId = node.id;
      render();
    });
    inputs[1].addEventListener("input", () => { node.name = inputs[1].value; renderMap(); });
    inputs[2].addEventListener("change", () => { node.type = inputs[2].value; render(); });
    inputs[3].addEventListener("change", () => { node.visible = inputs[3].checked; renderMap(); });
    inputs[4].addEventListener("input", () => { node.latitude = Number(inputs[4].value); renderMap(); });
    inputs[5].addEventListener("input", () => { node.longitude = Number(inputs[5].value); renderMap(); });
    row.querySelector(".nearest-edge-button")?.addEventListener("click", () => {
      addEdgeToNearestNode(node.id);
    });
    row.querySelector(".delete-node-button").addEventListener("click", () => {
      nodes.splice(index, 1);
      edges = edges.filter((edge) => edge.from !== node.id && edge.to !== node.id);
      selectedNodeId = nodes[0]?.id || "";
      render();
    });
    row.addEventListener("click", (event) => {
      if (event.target.tagName !== "BUTTON") selectNode(node.id, false);
    });
    tbody.appendChild(row);
  });
  el.nodeTable.appendChild(tbody);
}

function renderEdgeTable() {
  el.edgeTable.innerHTML = `<thead><tr><th>from</th><th>to</th><th>分</th><th>秒</th><th>totalSeconds</th><th>操作</th></tr></thead>`;
  const ids = nodes.map((node) => node.id);
  const tbody = document.createElement("tbody");
  edges.forEach((edge, index) => {
    const row = document.createElement("tr");
    if (index === selectedEdgeIndex) row.classList.add("selected");
    const minutes = Math.floor(Number(edge.travelTimeSeconds || 0) / 60);
    const seconds = Number(edge.travelTimeSeconds || 0) % 60;
    row.innerHTML = `
      <td>${selectHtml(ids, edge.from)}</td>
      <td>${selectHtml(ids, edge.to)}</td>
      <td><input class="narrow" type="number" min="0" value="${minutes}"></td>
      <td><input class="narrow" type="number" min="0" max="59" value="${seconds}"></td>
      <td>${edge.travelTimeSeconds || 0}</td>
      <td><button type="button">削除</button></td>`;
    const controls = row.querySelectorAll("select,input");
    controls[0].addEventListener("change", () => { edge.from = controls[0].value; render(); });
    controls[1].addEventListener("change", () => { edge.to = controls[1].value; render(); });
    controls[2].addEventListener("input", () => updateEdgeTime(edge, controls[2], controls[3]));
    controls[3].addEventListener("input", () => updateEdgeTime(edge, controls[2], controls[3]));
    row.querySelector("button").addEventListener("click", () => {
      edges.splice(index, 1);
      selectedEdgeIndex = -1;
      render();
    });
    row.addEventListener("click", (event) => {
      if (event.target.tagName !== "BUTTON") selectEdge(index, false);
    });
    tbody.appendChild(row);
  });
  el.edgeTable.appendChild(tbody);
}

function updateEdgeTime(edge, minutesInput, secondsInput) {
  const minutes = Math.max(0, Number(minutesInput.value || 0));
  const seconds = Math.max(0, Number(secondsInput.value || 0));
  edge.travelTimeSeconds = Math.floor(minutes * 60 + seconds);
  render();
}

function renderSummary(summary) {
  const labels = {
    nodeCount: "ノード数",
    edgeCount: "エッジ数",
    isolatedNodeCount: "孤立ノード数",
    oneConnectionNodeCount: "接続数1以下",
    duplicateEdgeCount: "重複エッジ数",
    oneWayEdgeCount: "片方向エッジ数",
    invalidReferenceEdgeCount: "未定義参照",
    invalidTravelTimeEdgeCount: "時間不正",
  };
  el.summary.innerHTML = Object.entries(labels).map(([key, label]) => `<dt>${label}</dt><dd>${summary?.[key] ?? 0}</dd>`).join("");
}

function renderValidation(result) {
  const rows = [];
  (result.errors || []).forEach((message) => rows.push(`<div class="issue error">${html(message)}</div>`));
  (result.warnings || []).forEach((message) => rows.push(`<div class="issue">${html(message)}</div>`));
  el.validation.innerHTML = rows.length ? rows.join("") : `<div class="issue">重大エラーはありません</div>`;
}

function validate() {
  const errors = [];
  const warnings = [];
  const ids = new Map();
  nodes.forEach((node, index) => {
    const id = String(node.id || "").trim();
    ids.set(id, (ids.get(id) || 0) + 1);
    if (!id) errors.push(`node[${index}].id が空です`);
    if (!String(node.name || "").trim()) errors.push(`${id || `node[${index}]`} の name が空です`);
    if (!nodeTypes.includes(node.type)) errors.push(`${id || `node[${index}]`} の type が不正です`);
    if (!Number.isFinite(Number(node.latitude))) errors.push(`${id || `node[${index}]`} の latitude が数値ではありません`);
    if (!Number.isFinite(Number(node.longitude))) errors.push(`${id || `node[${index}]`} の longitude が数値ではありません`);
  });
  ids.forEach((count, id) => { if (id && count > 1) errors.push(`node.id が重複しています: ${id}`); });
  const nodeIds = new Set(nodes.map((node) => node.id));
  const degree = new Map(nodes.map((node) => [node.id, 0]));
  const edgeKeys = new Map();
  edges.forEach((edge, index) => {
    if (!nodeIds.has(edge.from)) errors.push(`edge[${index}].from が未定義です: ${edge.from}`);
    if (!nodeIds.has(edge.to)) errors.push(`edge[${index}].to が未定義です: ${edge.to}`);
    if (edge.from === edge.to) errors.push(`edge[${index}] の from/to が同一です`);
    if (!Number.isInteger(Number(edge.travelTimeSeconds)) || Number(edge.travelTimeSeconds) < 1) errors.push(`edge[${index}].travelTimeSeconds が不正です`);
    if (nodeIds.has(edge.from)) degree.set(edge.from, (degree.get(edge.from) || 0) + 1);
    if (nodeIds.has(edge.to)) degree.set(edge.to, (degree.get(edge.to) || 0) + 1);
    const key = `${edge.from}->${edge.to}`;
    edgeKeys.set(key, (edgeKeys.get(key) || 0) + 1);
  });
  edgeKeys.forEach((count, key) => { if (count > 1) warnings.push(`重複エッジ: ${key}`); });
  edges.forEach((edge) => { if (!edge.bidirectional && !edgeKeys.has(`${edge.to}->${edge.from}`)) warnings.push(`片方向エッジ: ${edge.from} -> ${edge.to}`); });
  degree.forEach((count, id) => {
    if (count === 0) warnings.push(`孤立ノード: ${id}`);
    else if (count <= 1) warnings.push(`接続数が1以下のノード: ${id}`);
  });
  return {
    errors,
    warnings,
    summary: {
      nodeCount: nodes.length,
      edgeCount: edges.length,
      isolatedNodeCount: Array.from(degree.values()).filter((count) => count === 0).length,
      oneConnectionNodeCount: Array.from(degree.values()).filter((count) => count <= 1).length,
      duplicateEdgeCount: Array.from(edgeKeys.values()).reduce((sum, count) => sum + Math.max(0, count - 1), 0),
      oneWayEdgeCount: edges.filter((edge) => !edge.bidirectional && !edgeKeys.has(`${edge.to}->${edge.from}`)).length,
      invalidReferenceEdgeCount: edges.filter((edge) => !nodeIds.has(edge.from) || !nodeIds.has(edge.to)).length,
      invalidTravelTimeEdgeCount: edges.filter((edge) => !Number.isInteger(Number(edge.travelTimeSeconds)) || Number(edge.travelTimeSeconds) <= 0).length,
    },
  };
}

function addNode() {
  addNodeAtPixel(ORIGINAL_WIDTH / 2, ORIGINAL_HEIGHT / 2);
  setStatus("地図中央にノードを追加しました");
}

function addNodeAtPixel(x, y) {
  const gps = pixelToLatLon(x, y);
  const id = nextCustomNodeId();
  nodes.push({
    id,
    name: "新規ノード",
    type: nodeTypes.includes("STANDARD") ? "STANDARD" : nodeTypes[0] || "STANDARD",
    visible: true,
    latitude: roundCoordinate(gps.latitude),
    longitude: roundCoordinate(gps.longitude),
  });
  selectedNodeId = id;
  selectedEdgeIndex = -1;
  render();
}

function addEdge(from, to) {
  const fromId = from || nodes[0]?.id || "";
  const toId = to || nodes[1]?.id || nodes[0]?.id || "";
  const edge = {
    id: "",
    from: fromId,
    to: toId,
    travelTimeSeconds: defaultTravelTimeSeconds(fromId, toId),
    bidirectional: false,
    sourceType: "STANDARD",
    selectableForUserEdit: true,
  };
  edges.push(edge);
  selectedEdgeIndex = edges.length - 1;
  render();
}

function addEdgeToNearestNode(fromId) {
  const nearest = nearestNodeByCoordinate(fromId);
  if (!nearest) {
    setStatus("接続できる最寄りノードが見つかりません");
    return;
  }
  addEdge(fromId, nearest.id);
  selectedNodeId = nearest.id;
  render();
  setStatus(`${fromId} から最寄りノード ${nearest.id} へのエッジを追加しました`);
}

function selectNode(id, rerender = true) {
  selectedNodeId = id;
  selectedEdgeIndex = -1;
  if (mode === "edge") {
    if (!pendingFrom) {
      pendingFrom = id;
      setStatus(`${id} からの接続先を選択してください`);
    } else if (pendingFrom !== id) {
      addEdge(pendingFrom, id);
      pendingFrom = "";
      setStatus("エッジを追加しました。分・秒を調整してください");
    }
  }
  if (rerender) render();
}

function selectEdge(index, rerender = true, event = null) {
  if (event?.shiftKey) {
    selectShiftEdge(index);
    return;
  }
  selectedEdgeIndex = index;
  selectedNodeId = "";
  shiftEdgeIndexes = [];
  if (rerender) render();
}

function selectShiftEdge(index) {
  selectedEdgeIndex = -1;
  selectedNodeId = "";
  if (shiftEdgeIndexes.length === 1 && shiftEdgeIndexes[0] === index) {
    shiftEdgeIndexes = [];
    setStatus("Shiftエッジ選択を解除しました");
    render();
    return;
  }
  if (!shiftEdgeIndexes.includes(index)) shiftEdgeIndexes.push(index);
  if (shiftEdgeIndexes.length < 2) {
    setStatus("Shiftを押したまま、もう1本のエッジを選択してください");
    render();
    return;
  }
  const point = nodePointBetweenEdges(shiftEdgeIndexes[0], shiftEdgeIndexes[1]);
  shiftEdgeIndexes = [];
  addNodeAtPixel(point.x, point.y);
  setStatus("選択した2本のエッジ間にノードを追加しました");
}

function latLonToPixel(lat, lon) {
  if (!Number.isFinite(lat) || !Number.isFinite(lon) || Math.abs(TRANSFORM.DET) < 1e-18) return { x: 0, y: 0 };
  return {
    x: (TRANSFORM.E * (lat - TRANSFORM.C) - TRANSFORM.B * (lon - TRANSFORM.F)) / TRANSFORM.DET,
    y: (-TRANSFORM.D * (lat - TRANSFORM.C) + TRANSFORM.A * (lon - TRANSFORM.F)) / TRANSFORM.DET,
  };
}

function pixelToLatLon(x, y) {
  return {
    latitude: TRANSFORM.A * x + TRANSFORM.B * y + TRANSFORM.C,
    longitude: TRANSFORM.D * x + TRANSFORM.E * y + TRANSFORM.F,
  };
}

function eventToImagePixel(event) {
  const rect = el.overlay.getBoundingClientRect();
  const x = clamp(((event.clientX - rect.left) * ORIGINAL_WIDTH) / rect.width, 0, ORIGINAL_WIDTH);
  const y = clamp(((event.clientY - rect.top) * ORIGINAL_HEIGHT) / rect.height, 0, ORIGINAL_HEIGHT);
  return { x, y };
}

function nodePointBetweenEdges(firstIndex, secondIndex) {
  const first = edgeSegment(firstIndex);
  const second = edgeSegment(secondIndex);
  if (!first || !second) return { x: ORIGINAL_WIDTH / 2, y: ORIGINAL_HEIGHT / 2 };
  const intersection = segmentIntersection(first.a, first.b, second.a, second.b);
  if (intersection) return intersection;
  return {
    x: (midpoint(first.a, first.b).x + midpoint(second.a, second.b).x) / 2,
    y: (midpoint(first.a, first.b).y + midpoint(second.a, second.b).y) / 2,
  };
}

function edgeSegment(index) {
  const edge = edges[index];
  if (!edge) return null;
  const from = nodes.find((node) => node.id === edge.from);
  const to = nodes.find((node) => node.id === edge.to);
  if (!from || !to) return null;
  return {
    a: latLonToPixel(Number(from.latitude), Number(from.longitude)),
    b: latLonToPixel(Number(to.latitude), Number(to.longitude)),
  };
}

function segmentIntersection(a, b, c, d) {
  const denominator = (a.x - b.x) * (c.y - d.y) - (a.y - b.y) * (c.x - d.x);
  if (Math.abs(denominator) < 1e-9) return null;
  const px = ((a.x * b.y - a.y * b.x) * (c.x - d.x) - (a.x - b.x) * (c.x * d.y - c.y * d.x)) / denominator;
  const py = ((a.x * b.y - a.y * b.x) * (c.y - d.y) - (a.y - b.y) * (c.x * d.y - c.y * d.x)) / denominator;
  if (!pointOnSegment(px, py, a, b) || !pointOnSegment(px, py, c, d)) return null;
  return { x: px, y: py };
}

function pointOnSegment(x, y, a, b) {
  const tolerance = 1e-6;
  return x >= Math.min(a.x, b.x) - tolerance &&
    x <= Math.max(a.x, b.x) + tolerance &&
    y >= Math.min(a.y, b.y) - tolerance &&
    y <= Math.max(a.y, b.y) + tolerance;
}

function midpoint(a, b) {
  return { x: (a.x + b.x) / 2, y: (a.y + b.y) / 2 };
}

function nearestNodeByCoordinate(fromId) {
  const from = nodes.find((node) => node.id === fromId);
  if (!from || !hasNodeCoordinates(from)) return null;
  let nearest = null;
  let nearestDistance = Infinity;
  nodes.forEach((node) => {
    if (node.id === from.id || !hasNodeCoordinates(node)) return;
    const distance = distanceBetweenNodesMeters(from, node);
    if (!Number.isFinite(distance)) return;
    if (distance < nearestDistance) {
      nearest = node;
      nearestDistance = distance;
    }
  });
  return nearest;
}

function hasNodeCoordinates(node) {
  return Number.isFinite(Number(node?.latitude)) && Number.isFinite(Number(node?.longitude));
}

function defaultTravelTimeSeconds(fromId, toId) {
  const from = nodes.find((node) => node.id === fromId);
  const to = nodes.find((node) => node.id === toId);
  if (!from || !to || from.id === to.id) return 60;
  const distanceMeters = distanceBetweenNodesMeters(from, to);
  if (!Number.isFinite(distanceMeters) || distanceMeters <= 0) return 60;
  return Math.ceil(distanceMeters / DEFAULT_WALKING_SPEED_METERS_PER_SECOND);
}

function distanceBetweenNodesMeters(from, to) {
  const lat1 = degreesToRadians(Number(from.latitude));
  const lat2 = degreesToRadians(Number(to.latitude));
  const deltaLat = degreesToRadians(Number(to.latitude) - Number(from.latitude));
  const deltaLon = degreesToRadians(Number(to.longitude) - Number(from.longitude));
  if (![lat1, lat2, deltaLat, deltaLon].every(Number.isFinite)) return NaN;
  const a = Math.sin(deltaLat / 2) ** 2 +
    Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) ** 2;
  return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function degreesToRadians(value) {
  return value * Math.PI / 180;
}

function syncMapImageSize() {
  const width = el.mapImage.naturalWidth || ORIGINAL_WIDTH;
  const height = el.mapImage.naturalHeight || ORIGINAL_HEIGHT;
  el.mapFrame.style.aspectRatio = `${width} / ${height}`;
}

function setMode(nextMode, shouldRender = true) {
  mode = nextMode;
  pendingFrom = "";
  if (nextMode !== "select") selectedEdgeIndex = -1;
  if (nextMode !== "edge") pendingFrom = "";
  el.selectModeButton.classList.toggle("active", mode === "select");
  el.nodeModeButton.classList.toggle("active", mode === "add-node");
  el.edgeModeButton.classList.toggle("active", mode === "edge");
  el.mapStage.classList.toggle("add-node-mode", mode === "add-node");
  if (mode === "select") setStatus("選択モード");
  if (mode === "add-node") setStatus("地図上をクリックしてノードを追加できます");
  if (mode === "edge") setStatus("1つ目のノードを選択してください");
  if (shouldRender) render();
}

function clearTransientState() {
  selectedEdgeIndex = -1;
  shiftEdgeIndexes = [];
  pendingFrom = "";
}

function formatTime(totalSeconds) {
  const value = Math.max(0, Number(totalSeconds || 0));
  const minutes = Math.floor(value / 60);
  const seconds = value % 60;
  if (minutes <= 0) return `${seconds}秒`;
  if (seconds === 0) return `${minutes}分`;
  return `${minutes}分${seconds}秒`;
}

function uniqueId(base) {
  let index = 1;
  let id = base;
  const ids = new Set(nodes.map((node) => node.id));
  while (ids.has(id)) id = `${base}_${index++}`;
  return id;
}

function nextCustomNodeId() {
  const ids = new Set(nodes.map((node) => node.id));
  for (let index = 1; index < 10000; index++) {
    const id = `custom_node_${String(index).padStart(3, "0")}`;
    if (!ids.has(id)) return id;
  }
  return uniqueId("custom_node");
}

function roundCoordinate(value) {
  return Math.round(value * 1000000000000) / 1000000000000;
}

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value));
}

function selectHtml(options, selected) {
  return `<select>${options.map((option) => `<option value="${html(option)}" ${option === selected ? "selected" : ""}>${html(option)}</option>`).join("")}</select>`;
}

function svg(name, attrs, text) {
  const node = document.createElementNS("http://www.w3.org/2000/svg", name);
  Object.entries(attrs).forEach(([key, value]) => node.setAttribute(key, value));
  if (text != null) node.textContent = text;
  return node;
}

function html(value) {
  return String(value ?? "").replace(/[&<>"']/g, (ch) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;" }[ch]));
}

function setStatus(message) {
  el.status.textContent = message;
}
