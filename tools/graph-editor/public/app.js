const ORIGINAL_WIDTH = 1632;
const ORIGINAL_HEIGHT = 1904;
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
let edgeMode = false;
let pendingFrom = "";

const el = {
  overlay: document.getElementById("overlay"),
  nodeTable: document.getElementById("nodeTable"),
  edgeTable: document.getElementById("edgeTable"),
  summary: document.getElementById("summary"),
  validation: document.getElementById("validation"),
  status: document.getElementById("status"),
  labelToggle: document.getElementById("labelToggle"),
  edgeToggle: document.getElementById("edgeToggle"),
  edgeModeButton: document.getElementById("edgeModeButton"),
};

document.getElementById("reloadButton").addEventListener("click", loadGraph);
document.getElementById("saveButton").addEventListener("click", saveGraph);
document.getElementById("addNodeButton").addEventListener("click", addNode);
document.getElementById("addEdgeButton").addEventListener("click", () => addEdge("", ""));
el.labelToggle.addEventListener("change", render);
el.edgeToggle.addEventListener("change", render);
el.edgeModeButton.addEventListener("click", () => {
  edgeMode = !edgeMode;
  pendingFrom = "";
  el.edgeModeButton.classList.toggle("active", edgeMode);
  setStatus(edgeMode ? "1つ目のノードを選択してください" : "");
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
  render(graph.summary);
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
  const showLabels = el.labelToggle.checked;
  const showEdges = el.edgeToggle.checked;
  if (showEdges) {
    edges.forEach((edge, index) => {
      const from = nodes.find((node) => node.id === edge.from);
      const to = nodes.find((node) => node.id === edge.to);
      if (!from || !to) return;
      const a = latLonToPixel(Number(from.latitude), Number(from.longitude));
      const b = latLonToPixel(Number(to.latitude), Number(to.longitude));
      const line = svg("line", { x1: a.x, y1: a.y, x2: b.x, y2: b.y, class: `edge-line${index === selectedEdgeIndex ? " selected" : ""}` });
      line.addEventListener("click", () => selectEdge(index));
      el.overlay.appendChild(line);
      if (showLabels) {
        el.overlay.appendChild(svg("text", { x: (a.x + b.x) / 2, y: (a.y + b.y) / 2, class: "edge-label" }, formatTime(edge.travelTimeSeconds)));
      }
    });
  }
  nodes.forEach((node) => {
    const point = latLonToPixel(Number(node.latitude), Number(node.longitude));
    const dot = svg("circle", {
      cx: point.x,
      cy: point.y,
      r: node.type === "BUS_STOP" ? 18 : 14,
      class: `node-dot${node.type === "BUS_STOP" ? " bus" : ""}${node.id === selectedNodeId ? " selected" : ""}`,
    });
    dot.addEventListener("click", () => selectNode(node.id));
    el.overlay.appendChild(dot);
    if (showLabels) {
      el.overlay.appendChild(svg("text", { x: point.x + 18, y: point.y - 14, class: "node-label" }, node.name || node.id));
    }
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
      <td><button type="button">削除</button></td>`;
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
    inputs[3].addEventListener("change", () => { node.visible = inputs[3].checked; });
    inputs[4].addEventListener("input", () => { node.latitude = Number(inputs[4].value); renderMap(); });
    inputs[5].addEventListener("input", () => { node.longitude = Number(inputs[5].value); renderMap(); });
    row.querySelector("button").addEventListener("click", () => {
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
  const id = uniqueId("new_node");
  nodes.push({ id, name: "新規ノード", type: nodeTypes[0] || "STANDARD", visible: true, latitude: 35.464, longitude: 136.737 });
  selectedNodeId = id;
  render();
}

function addEdge(from, to) {
  const edge = {
    id: "",
    from: from || nodes[0]?.id || "",
    to: to || nodes[1]?.id || nodes[0]?.id || "",
    travelTimeSeconds: 60,
    bidirectional: false,
    sourceType: "STANDARD",
    selectableForUserEdit: true,
  };
  edges.push(edge);
  selectedEdgeIndex = edges.length - 1;
  render();
}

function selectNode(id, rerender = true) {
  selectedNodeId = id;
  selectedEdgeIndex = -1;
  if (edgeMode) {
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

function selectEdge(index, rerender = true) {
  selectedEdgeIndex = index;
  selectedNodeId = "";
  if (rerender) render();
}

function latLonToPixel(lat, lon) {
  if (!Number.isFinite(lat) || !Number.isFinite(lon) || Math.abs(TRANSFORM.DET) < 1e-18) return { x: 0, y: 0 };
  return {
    x: (TRANSFORM.E * (lat - TRANSFORM.C) - TRANSFORM.B * (lon - TRANSFORM.F)) / TRANSFORM.DET,
    y: (-TRANSFORM.D * (lat - TRANSFORM.C) + TRANSFORM.A * (lon - TRANSFORM.F)) / TRANSFORM.DET,
  };
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
