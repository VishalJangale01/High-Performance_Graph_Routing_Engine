package com.routing.engine.model;

import java.util.HashMap;
import java.util.Map;

public class Graph {
    // Map ID -> Node Object
    private Map<Integer, Node> nodes = new HashMap<>();

    public void addEdge(int sourceId, int targetId, double weight) {
        Node source = nodes.computeIfAbsent(sourceId, Node::new);
        Node target = nodes.computeIfAbsent(targetId, Node::new);
        source.addNeighbor(new Edge(target, weight));
    }

    public Node getNode(int id) {
        return nodes.get(id);
    }

    public Map<Integer, Node> getNodes() {
        return nodes;
    }

    // Helper to reset all nodes before a new algorithm run
    public void resetGraph() {
        for (Node node : nodes.values()) {
            node.reset();
        }
    }
}