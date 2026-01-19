package com.routing.engine.service.impl;

import com.routing.engine.model.*;
import com.routing.engine.service.PathFinder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DuanLiteService implements PathFinder {

    @Override
    public String getAlgorithmName() {
        return "Duan Lite (Bucket Queue / Delta-Stepping)";
    }

    @Override
    public List<Node> findShortestPath(Graph graph, int sourceId, int targetId) {
        graph.resetGraph();

        Node start = graph.getNode(sourceId);
        Node end = graph.getNode(targetId);

        if (start == null || end == null) return Collections.emptyList();

        start.setMinDistance(0);

        // DELTA SELECTION is crucial.
        // In the paper, this is calculated dynamically.
        // For this implementation, we pick a reasonable constant (e.g., 2.0).
        BucketQueue bucketQueue = new BucketQueue(2.0);
        bucketQueue.add(start);

        while (!bucketQueue.isEmpty()) {
            Node current = bucketQueue.poll();

            // Optimization: Stop if we processed the target bucket
            if (current.getId() == end.getId()) {
                // We don't break immediately in bucket algorithms usually,
                // but for this simple version, we can check if the bucket index is far past the target.
                // For now, let it run to be safe.
            }

            for (Edge edge : current.getNeighbors()) {
                Node neighbor = edge.getTarget();
                double newDist = current.getMinDistance() + edge.getWeight();

                if (newDist < neighbor.getMinDistance()) {
                    neighbor.setMinDistance(newDist);
                    neighbor.setPreviousNode(current);

                    // Add to our O(1) bucket queue
                    bucketQueue.add(neighbor);
                }
            }
        }

        return buildPath(end);
    }

    private List<Node> buildPath(Node target) {
        List<Node> path = new ArrayList<>();
        if (target.getMinDistance() == Double.MAX_VALUE) return path; // No path found

        for (Node node = target; node != null; node = node.getPreviousNode()) {
            path.add(node);
        }
        Collections.reverse(path);
        return path;
    }
}