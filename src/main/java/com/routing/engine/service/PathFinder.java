package com.routing.engine.service;

import com.routing.engine.model.Graph;
import com.routing.engine.model.Node;
import java.util.List;

public interface PathFinder {
    // Returns the path as a list of Nodes
    List<Node> findShortestPath(Graph graph, int sourceId, int targetId);

    // Returns the name of the algorithm (e.g., "Dijkstra", "Duan2025")
    String getAlgorithmName();
}