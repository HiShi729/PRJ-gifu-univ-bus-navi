const fs = require("fs");
const http = require("http");
const path = require("path");
const { URL } = require("url");

const PORT = Number(process.env.PORT || 3000);
const ROOT = path.resolve(__dirname, "../..");
const GRAPH_FILE = path.join(ROOT, "app/src/main/java/com/example/prj_gifu_univ_bus_navi/data/LocalCampusGraphData.java");
const NODE_TYPE_FILE = path.join(ROOT, "app/src/main/java/com/example/prj_gifu_univ_bus_navi/model/NodeType.java");
const MAP_IMAGE_CANDIDATES = [
  path.join(ROOT, "app/src/main/res/mipmap-hdpi/tatemono_no_number.png"),
  path.join(ROOT, "app/src/main/res/drawable-nodpi/tatemono_no_number.png"),
];
const PUBLIC_DIR = path.join(__dirname, "public");

function readGraph() {
  const source = fs.readFileSync(GRAPH_FILE, "utf8");
  return {
    nodes: parseNodes(source),
    edges: parseEdges(source),
    nodeTypes: readNodeTypes(),
  };
}

function readNodeTypes() {
  const source = fs.readFileSync(NODE_TYPE_FILE, "utf8");
  const match = source.match(/enum\s+NodeType\s*\{([\s\S]*?)\}/);
  if (!match) return [];
  return match[1].split(",").map((value) => value.trim()).filter(Boolean);
}

function parseNodes(source) {
  const body = listBody(source, "NODES");
  const nodes = [];
  const re = /new\s+CampusGraphNode\s*\(\s*"([^"]*)"\s*,\s*"([^"]*)"\s*,\s*NodeType\.([A-Z_]+)\s*,\s*(true|false)\s*,\s*([^,\s)]+)\s*,\s*([^,\s)]+)\s*\)/g;
  let match;
  while ((match = re.exec(body)) !== null) {
    nodes.push({
      id: unescapeJava(match[1]),
      name: unescapeJava(match[2]),
      type: match[3],
      visible: match[4] === "true",
      latitude: numberOrNull(match[5]),
      longitude: numberOrNull(match[6]),
    });
  }
  return nodes;
}

function parseEdges(source) {
  const body = listBody(source, "EDGES");
  const edges = [];
  const re = /new\s+CampusGraphEdge\s*\(\s*"([^"]*)"\s*,\s*"([^"]*)"\s*,\s*"([^"]*)"\s*,\s*(-?\d+)\s*,\s*(true|false)\s*,\s*EdgeSourceType\.([A-Z_]+)\s*,\s*(true|false)\s*\)/g;
  let match;
  while ((match = re.exec(body)) !== null) {
    edges.push({
      id: unescapeJava(match[1]),
      from: unescapeJava(match[2]),
      to: unescapeJava(match[3]),
      travelTimeSeconds: Number(match[4]),
      bidirectional: match[5] === "true",
      sourceType: match[6],
      selectableForUserEdit: match[7] === "true",
    });
  }
  return edges;
}

function listBody(source, name) {
  const marker = `List<${name === "NODES" ? "CampusGraphNode" : "CampusGraphEdge"}> ${name}`;
  const markerIndex = source.indexOf(marker);
  if (markerIndex < 0) throw new Error(`${name} list was not found`);
  const start = source.indexOf("Arrays.asList(", markerIndex);
  if (start < 0) throw new Error(`${name} Arrays.asList was not found`);
  const bodyStart = start + "Arrays.asList(".length;
  const bodyEnd = matchingParen(source, start + "Arrays.asList".length);
  return source.slice(bodyStart, bodyEnd);
}

function replaceListBody(source, name, replacement) {
  const marker = `List<${name === "NODES" ? "CampusGraphNode" : "CampusGraphEdge"}> ${name}`;
  const markerIndex = source.indexOf(marker);
  if (markerIndex < 0) throw new Error(`${name} list was not found`);
  const start = source.indexOf("Arrays.asList(", markerIndex);
  if (start < 0) throw new Error(`${name} Arrays.asList was not found`);
  const bodyStart = start + "Arrays.asList(".length;
  const bodyEnd = matchingParen(source, start + "Arrays.asList".length);
  return source.slice(0, bodyStart) + "\n" + replacement + "\n" + source.slice(bodyEnd);
}

function matchingParen(source, openIndex) {
  let depth = 0;
  let inString = false;
  let escaped = false;
  for (let i = openIndex; i < source.length; i++) {
    const ch = source[i];
    if (inString) {
      if (escaped) {
        escaped = false;
      } else if (ch === "\\") {
        escaped = true;
      } else if (ch === "\"") {
        inString = false;
      }
      continue;
    }
    if (ch === "\"") inString = true;
    if (ch === "(") depth++;
    if (ch === ")") {
      depth--;
      if (depth === 0) return i;
    }
  }
  throw new Error("matching parenthesis was not found");
}

function validateGraph(nodes, edges, nodeTypes) {
  const errors = [];
  const warnings = [];
  const idCounts = new Map();
  nodes.forEach((node, index) => {
    const id = String(node.id || "").trim();
    idCounts.set(id, (idCounts.get(id) || 0) + 1);
    if (!id) errors.push(`node[${index}].id が空です`);
    if (!String(node.name || "").trim()) errors.push(`${id || `node[${index}]`} の name が空です`);
    if (!nodeTypes.includes(node.type)) errors.push(`${id || `node[${index}]`} の type が不正です`);
    if (!Number.isFinite(Number(node.latitude))) errors.push(`${id || `node[${index}]`} の latitude が数値ではありません`);
    if (!Number.isFinite(Number(node.longitude))) errors.push(`${id || `node[${index}]`} の longitude が数値ではありません`);
  });
  for (const [id, count] of idCounts.entries()) {
    if (id && count > 1) errors.push(`node.id が重複しています: ${id}`);
  }
  const nodeIds = new Set(nodes.map((node) => String(node.id || "").trim()));
  const edgeKeys = new Map();
  edges.forEach((edge, index) => {
    if (!nodeIds.has(edge.from)) errors.push(`edge[${index}].from が未定義です: ${edge.from}`);
    if (!nodeIds.has(edge.to)) errors.push(`edge[${index}].to が未定義です: ${edge.to}`);
    if (edge.from === edge.to) errors.push(`edge[${index}] の from/to が同一です: ${edge.from}`);
    if (!Number.isInteger(Number(edge.travelTimeSeconds)) || Number(edge.travelTimeSeconds) < 1) {
      errors.push(`edge[${index}].travelTimeSeconds は1以上の整数が必要です`);
    }
    const key = `${edge.from}->${edge.to}`;
    edgeKeys.set(key, (edgeKeys.get(key) || 0) + 1);
  });
  for (const [key, count] of edgeKeys.entries()) {
    if (count > 1) warnings.push(`重複エッジがあります: ${key}`);
  }
  const summary = summarize(nodes, edges);
  summary.issues.forEach((issue) => warnings.push(issue));
  return { errors, warnings, summary };
}

function summarize(nodes, edges) {
  const nodeIds = new Set(nodes.map((node) => node.id));
  const degree = new Map(nodes.map((node) => [node.id, 0]));
  const directedKeys = new Map();
  let invalidReferenceEdgeCount = 0;
  let invalidTravelTimeEdgeCount = 0;
  edges.forEach((edge) => {
    if (!nodeIds.has(edge.from) || !nodeIds.has(edge.to)) invalidReferenceEdgeCount++;
    if (!Number.isInteger(Number(edge.travelTimeSeconds)) || Number(edge.travelTimeSeconds) <= 0) invalidTravelTimeEdgeCount++;
    if (nodeIds.has(edge.from)) degree.set(edge.from, (degree.get(edge.from) || 0) + 1);
    if (nodeIds.has(edge.to)) degree.set(edge.to, (degree.get(edge.to) || 0) + 1);
    const key = `${edge.from}->${edge.to}`;
    directedKeys.set(key, (directedKeys.get(key) || 0) + 1);
  });
  const duplicateEdgeCount = Array.from(directedKeys.values()).reduce((sum, count) => sum + Math.max(0, count - 1), 0);
  let oneWayEdgeCount = 0;
  edges.forEach((edge) => {
    if (!edge.bidirectional && !directedKeys.has(`${edge.to}->${edge.from}`)) oneWayEdgeCount++;
  });
  const isolatedNodeCount = Array.from(degree.values()).filter((count) => count === 0).length;
  const oneConnectionNodeCount = Array.from(degree.values()).filter((count) => count <= 1).length;
  const issues = [];
  nodes.forEach((node) => {
    const count = degree.get(node.id) || 0;
    if (count === 0) issues.push(`孤立ノード: ${node.id}`);
    else if (count <= 1) issues.push(`接続数が1以下のノード: ${node.id}`);
  });
  edges.forEach((edge) => {
    if (!edge.bidirectional && !directedKeys.has(`${edge.to}->${edge.from}`)) issues.push(`片方向エッジ: ${edge.from} -> ${edge.to}`);
  });
  return {
    nodeCount: nodes.length,
    edgeCount: edges.length,
    isolatedNodeCount,
    oneConnectionNodeCount,
    duplicateEdgeCount,
    oneWayEdgeCount,
    invalidReferenceEdgeCount,
    invalidTravelTimeEdgeCount,
    issues,
  };
}

function saveGraph(nodes, edges) {
  const nodeTypes = readNodeTypes();
  const validation = validateGraph(nodes, edges, nodeTypes);
  if (validation.errors.length > 0) {
    return { ok: false, status: 400, body: { errors: validation.errors, warnings: validation.warnings, summary: validation.summary } };
  }
  let source = fs.readFileSync(GRAPH_FILE, "utf8");
  source = replaceListBody(source, "NODES", renderNodes(nodes));
  source = replaceListBody(source, "EDGES", renderEdges(edges));
  fs.writeFileSync(GRAPH_FILE, source, "utf8");
  const saved = readGraph();
  const summary = summarize(saved.nodes, saved.edges);
  return { ok: true, status: 200, body: { nodes: saved.nodes, edges: saved.edges, nodeTypes, summary, warnings: validation.warnings } };
}

function renderNodes(nodes) {
  return nodes.map((node, index) => {
    const comma = index === nodes.length - 1 ? "" : ",";
    const spacer = index > 0 && nodes[index - 1].type !== "BUS_STOP" && node.type === "BUS_STOP" ? "\n" : "";
    return `${spacer}    new CampusGraphNode("${escapeJava(node.id)}", "${escapeJava(node.name)}", NodeType.${node.type}, ${Boolean(node.visible)}, ${Number(node.latitude)}, ${Number(node.longitude)})${comma}`;
  }).join("\n");
}

function renderEdges(edges) {
  return edges.map((edge, index) => {
    const id = edge.id && String(edge.id).trim() ? edge.id : makeEdgeId(edge.from, edge.to, index);
    const sourceType = edge.sourceType || "STANDARD";
    const bidirectional = Boolean(edge.bidirectional);
    const selectable = edge.selectableForUserEdit !== false;
    const comma = index === edges.length - 1 ? "" : ",";
    const currentBusEdge = String(edge.from).startsWith("bus_stop_") || String(edge.to).startsWith("bus_stop_");
    const previous = edges[index - 1];
    const previousBusEdge = previous && (String(previous.from).startsWith("bus_stop_") || String(previous.to).startsWith("bus_stop_"));
    const spacer = index > 0 && !previousBusEdge && currentBusEdge ? "\n" : "";
    return `${spacer}    new CampusGraphEdge("${escapeJava(id)}", "${escapeJava(edge.from)}", "${escapeJava(edge.to)}", ${Number(edge.travelTimeSeconds)}, ${bidirectional}, EdgeSourceType.${sourceType}, ${selectable})${comma}`;
  }).join("\n");
}

function makeEdgeId(from, to, index) {
  return `edge_${String(from || "from").replace(/[^a-zA-Z0-9_]/g, "_")}_${String(to || "to").replace(/[^a-zA-Z0-9_]/g, "_")}_${index}`;
}

function escapeJava(value) {
  return String(value || "").replace(/\\/g, "\\\\").replace(/"/g, "\\\"");
}

function unescapeJava(value) {
  return String(value).replace(/\\"/g, "\"").replace(/\\\\/g, "\\");
}

function numberOrNull(value) {
  if (value === "null") return null;
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

function sendJson(res, status, body) {
  const text = JSON.stringify(body);
  res.writeHead(status, { "Content-Type": "application/json; charset=utf-8" });
  res.end(text);
}

function readRequestBody(req) {
  return new Promise((resolve, reject) => {
    let body = "";
    req.on("data", (chunk) => {
      body += chunk;
      if (body.length > 5_000_000) reject(new Error("request body is too large"));
    });
    req.on("end", () => resolve(body));
    req.on("error", reject);
  });
}

function serveStatic(req, res, pathname) {
  const relative = pathname === "/" ? "index.html" : pathname.replace(/^\/+/, "");
  const filePath = path.normalize(path.join(PUBLIC_DIR, relative));
  if (!filePath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403);
    res.end("Forbidden");
    return;
  }
  if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
    res.writeHead(404);
    res.end("Not found");
    return;
  }
  const ext = path.extname(filePath);
  const type = ext === ".html" ? "text/html; charset=utf-8" : ext === ".css" ? "text/css; charset=utf-8" : ext === ".js" ? "application/javascript; charset=utf-8" : "application/octet-stream";
  res.writeHead(200, { "Content-Type": type });
  fs.createReadStream(filePath).pipe(res);
}

const server = http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url, `http://${req.headers.host || "localhost"}`);
    if ((req.method === "GET" || req.method === "HEAD") && url.pathname === "/favicon.ico") {
      res.writeHead(204);
      res.end();
      return;
    }
    if (req.method === "GET" && url.pathname === "/api/graph") {
      const graph = readGraph();
      sendJson(res, 200, { ...graph, summary: summarize(graph.nodes, graph.edges) });
      return;
    }
    if (req.method === "POST" && url.pathname === "/api/graph") {
      const body = JSON.parse(await readRequestBody(req));
      const result = saveGraph(Array.isArray(body.nodes) ? body.nodes : [], Array.isArray(body.edges) ? body.edges : []);
      sendJson(res, result.status, result.body);
      return;
    }
    if (req.method === "GET" && url.pathname === "/api/map-image") {
      const imagePath = MAP_IMAGE_CANDIDATES.find((candidate) => fs.existsSync(candidate));
      if (!imagePath) {
        res.writeHead(404);
        res.end("map image not found");
        return;
      }
      res.writeHead(200, { "Content-Type": "image/png" });
      fs.createReadStream(imagePath).pipe(res);
      return;
    }
    if (req.method === "GET") {
      serveStatic(req, res, url.pathname);
      return;
    }
    res.writeHead(405);
    res.end("Method not allowed");
  } catch (error) {
    sendJson(res, 500, { errors: [error.message] });
  }
});

server.listen(PORT, () => {
  console.log(`Graph editor: http://localhost:${PORT}`);
});
