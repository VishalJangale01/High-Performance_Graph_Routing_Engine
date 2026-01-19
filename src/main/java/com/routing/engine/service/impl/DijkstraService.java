package com.routing.engine.service.impl;

import com.routing.engine.model.*;
import com.routing.engine.service.PathFinder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DijkstraService implements PathFinder {

    @Override
    public String getAlgorithmName() {
        return "Standard Dijkstra (PriorityQueue)";
    }

    @Override
    public List<Node> findShortestPath(Graph graph, int sourceId, int targetId) {
        graph.resetGraph(); // Clear old runs

        Node start = graph.getNode(sourceId);
        Node end = graph.getNode(targetId);

        if (start == null || end == null) return Collections.emptyList();

        start.setMinDistance(0);

        // Java's PriorityQueue is a Binary Heap (The "Sorting" Barrier!)
        PriorityQueue<Node> queue = new PriorityQueue<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            // Optimization: If we reached the target, we can stop
            if (current.getId() == end.getId()) break;

            for (Edge edge : current.getNeighbors()) {
                Node neighbor = edge.getTarget();
                double newDist = current.getMinDistance() + edge.getWeight();

                if (newDist < neighbor.getMinDistance()) {
                    // Remove is O(n) in Java PQ, usually we just add duplicate
                    // But for strict Dijkstra we update logic
                    queue.remove(neighbor);
                    neighbor.setMinDistance(newDist);
                    neighbor.setPreviousNode(current);
                    queue.add(neighbor);
                }
            }
        }

        return buildPath(end);
    }

    private List<Node> buildPath(Node target) {
        List<Node> path = new ArrayList<>();
        for (Node node = target; node != null; node = node.getPreviousNode()) {
            path.add(node);
        }
        Collections.reverse(path);
        return path;
    }
}