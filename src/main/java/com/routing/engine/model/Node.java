package com.routing.engine.model;

import java.util.ArrayList;
import java.util.List;

public class Node implements Comparable<Node> {
    private int id;
    private List<Edge> neighbors;

    // Fields for the algorithm
    private double minDistance = Double.MAX_VALUE;
    private Node previousNode; // To reconstruct the path later

    public Node(int id) {
        this.id = id;
        this.neighbors = new ArrayList<>();
    }

    public void addNeighbor(Edge edge) {
        this.neighbors.add(edge);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Edge> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<Edge> neighbors) {
        this.neighbors = neighbors;
    }

    public double getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(double minDistance) {
        this.minDistance = minDistance;
    }

    public Node getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(Node previousNode) {
        this.previousNode = previousNode;
    }

    // Standard Getters, Setters, and verify compareTo for PriorityQueue
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.minDistance, other.minDistance);
    }

    // Reset state for a new run
    public void reset() {
        this.minDistance = Double.MAX_VALUE;
        this.previousNode = null;
    }
}
