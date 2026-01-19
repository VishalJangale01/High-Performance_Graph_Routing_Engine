package com.routing.engine.controller;

import com.routing.engine.model.Edge;
import com.routing.engine.model.Graph;
import com.routing.engine.model.Node;
import com.routing.engine.service.PathFinder;
import com.routing.engine.service.impl.DijkstraService;
import com.routing.engine.service.impl.DuanLiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.http.HttpHeaders;

import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/graph")
public class GraphController {

    private final DijkstraService dijkstraService;
    private final DuanLiteService duanLiteService;
    private static final int MAX_NODES = 1_000_000;
    private static final int MAX_EDGES = 2_000_000;

    private Graph memoryGraph;

    @Autowired
    public GraphController(DijkstraService dijkstraService, DuanLiteService duanLiteService) {
        this.dijkstraService = dijkstraService;
        this.duanLiteService = duanLiteService;
        this.memoryGraph = new Graph();
        initializeDummyGraph(); // Pre-load small test data on startup
    }

    /**
     * Calculate Shortest Path
     * Usage: http://localhost:8080/api/graph/shortest-path?from=1&to=5&algo=DUAN
     */
    @GetMapping("/shortest-path")
    public String getShortestPath(@RequestParam int from,
                                  @RequestParam int to,
                                  @RequestParam(defaultValue = "DIJKSTRA") String algo) {

        //  Select the Algorithm
        PathFinder selectedAlgo;
        if ("DUAN".equalsIgnoreCase(algo)) {
            selectedAlgo = duanLiteService;
        } else {
            selectedAlgo = dijkstraService;
        }

        //  Execute and Time it
        long startTime = System.nanoTime();
        List<Node> path = selectedAlgo.findShortestPath(memoryGraph, from, to);
        long endTime = System.nanoTime();

        //  Format Response
        if (path.isEmpty()) {
            return "No path found between Node " + from + " and " + to;
        }

        String pathString = path.stream()
                .map(node -> String.valueOf(node.getId()))
                .collect(Collectors.joining(" -> "));

        double totalDistance = path.get(path.size() - 1).getMinDistance();

        return String.format(
                "Algorithm Used: %s\n" +
                        "Path Found: %s\n" +
                        "Total Cost: %.2f\n" +
                        "Execution Time: %d ns",
                selectedAlgo.getAlgorithmName(),
                pathString,
                totalDistance,
                (endTime - startTime)
        );
    }

    /**
     * BENCHMARK
     * Usage: http://localhost:8080/api/graph/benchmark?nodes=20000&edges=100000
     */
    @GetMapping("/benchmark")
    public String runBenchmark(@RequestParam(defaultValue = "20000") int nodes,
                               @RequestParam(defaultValue = "100000") int edges) {
        //Prevent server crash
        if (nodes > MAX_NODES || edges > MAX_EDGES) {
            return String.format(
                    "ERROR: Graph size too large for this demo server!\n" +
                            "Limit: %d Nodes, %d Edges.\n" +
                            "You requested: %d Nodes, %d Edges.\n" +
                            "Please reduce the size to avoid crashing the application.",
                    MAX_NODES, MAX_EDGES, nodes, edges
            );
        }

        // Get Runtime instance
        Runtime runtime = Runtime.getRuntime();

        //Clean up memory before starting (to get an accurate baseline)
        System.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        //  Generate Graph
        generateRandomGraph(nodes, edges);

        // Measure Memory After Graph Generation
        long memoryAfterGraph = runtime.totalMemory() - runtime.freeMemory();
        long graphMemory = (memoryAfterGraph - memoryBefore) / (1024 * 1024); // Convert to MB

        // Generate Massive Data
        generateRandomGraph(nodes, edges);

        // Pick fixed source/target to ensure fair comparison
        int source = 0;
        int target = nodes - 1;

        //  Run Dijkstra
        long startDijkstra = System.currentTimeMillis();
        dijkstraService.findShortestPath(memoryGraph, source, target);
        long endDijkstra = System.currentTimeMillis();
        long dijkstraTime = endDijkstra - startDijkstra;

        //  Run Duan Lite (Bucket Queue)
        long startDuan = System.currentTimeMillis();
        duanLiteService.findShortestPath(memoryGraph, source, target);
        long endDuan = System.currentTimeMillis();
        long duanTime = endDuan - startDuan;

        //  Calculate Improvement
        String winner;
        if (duanTime < dijkstraTime) {
            double percent = ((double)(dijkstraTime - duanTime) / dijkstraTime) * 100;
            winner = String.format("Duan Lite is FASTER by %.2f%%", percent);
        } else {
            winner = "Dijkstra is faster (Graph might be too small or too dense)";
        }

        return String.format(
                "=== BENCHMARK RESULT ===\n" +
                        "Graph Config: %d Nodes, %d Edges\n" +
                        "----------------------------\n" +
                        "Dijkstra Time: %d ms\n" +
                        "Duan Lite Time: %d ms\n" +
                        "RAM Used by Graph: %d MB\n" +
                "----------------------------\n" +
                        "Result: %s",
                nodes, edges,
                dijkstraTime,
                duanTime,
                graphMemory,
                winner
        );
    }

    /**
     * DATA EXPORT (The "Proof")
     * Downloads the current graph structure as a .txt file.
     * Uses Streaming to avoid crashing server RAM with large strings.
     */
    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadGraph() {

        // Define the file stream
        StreamingResponseBody stream = out -> {
            PrintWriter writer = new PrintWriter(out);
            writer.println("=== GRAPH DATA DUMP ===");
            writer.printf("Total Nodes: %d%n", memoryGraph.getNodes().size());
            writer.println("Format: [Source Node] -> [Target Node] (Weight)");
            writer.println("------------------------------------------------");

            // Stream data line-by-line
            for (Node node : memoryGraph.getNodes().values()) {
                if (node.getNeighbors().isEmpty()) continue;

                for (Edge edge : node.getNeighbors()) {
                    writer.printf("%d -> %d (%.2f)%n",
                            node.getId(),
                            edge.getTarget().getId(),
                            edge.getWeight());
                }

                // Flush every 1000 lines to keep memory low
                if (node.getId() % 1000 == 0) writer.flush();
            }
            writer.println("--- End of Export ---");
            writer.flush();
            writer.close(); // Close explicitly
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"graph_data.txt\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                .body(stream);
    }

    // --- Helper Methods ---

    // 1. Small Dummy Graph for functional testing
    private void initializeDummyGraph() {
        memoryGraph = new Graph(); // Reset
        memoryGraph.addEdge(1, 2, 10.0);
        memoryGraph.addEdge(1, 3, 5.0);
        memoryGraph.addEdge(2, 5, 15.0);
        memoryGraph.addEdge(3, 2, 8.0);
        memoryGraph.addEdge(3, 5, 20.0);
        memoryGraph.addEdge(3, 4, 2.0);
        memoryGraph.addEdge(4, 5, 10.0);
        System.out.println("Dummy Graph Initialized.");
    }

    // Massive Random Graph for Benchmarking
    private void generateRandomGraph(int numNodes, int numEdges) {
        System.out.println("Generating graph with " + numNodes + " nodes...");
        memoryGraph = new Graph();
        Random random = new Random();

        // Create Nodes first
        for (int i = 0; i < numNodes; i++) {
            memoryGraph.getNodes().computeIfAbsent(i, Node::new);
        }

        // Create Edges
        for (int i = 0; i < numEdges; i++) {
            int source = random.nextInt(numNodes);
            int target = random.nextInt(numNodes);

            // Random weight between 1 and 100
            double weight = 1.0 + random.nextInt(10);

            if (source != target) {
                memoryGraph.addEdge(source, target, weight);
            }
        }
        System.out.println("Graph Generation Complete.");
    }
}