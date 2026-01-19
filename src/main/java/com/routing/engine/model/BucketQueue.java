package com.routing.engine.model;

import java.util.*;

public class BucketQueue {
    private final double delta;

    // OPTIMIZATION: Use an Array instead of HashMap for speed (Direct Memory Access)
    // We assume a max reasonable path length for the demo to avoid resizing logic
    private final List<Node>[] buckets;
    private int currentBucketIndex;
    private int size;
    private final int CAPACITY = 100000; // Enough slots for our demo

    @SuppressWarnings("unchecked")
    public BucketQueue(double delta) {
        this.delta = delta;
        // Pre-allocate array to avoid "hashing" overhead
        this.buckets = new ArrayList[CAPACITY];
        this.currentBucketIndex = 0;
        this.size = 0;
    }

    public void add(Node node) {
        int bucketIndex = (int) (node.getMinDistance() / delta);

        // Safety check for demo (in production, we'd use a circular buffer)
        if (bucketIndex >= CAPACITY) bucketIndex = CAPACITY - 1;

        if (buckets[bucketIndex] == null) {
            buckets[bucketIndex] = new ArrayList<>();
        }

        buckets[bucketIndex].add(node);
        size++;

        // If we inserted behind the current pointer, reset pointer (rare in Dijkstra, but possible)
        if (bucketIndex < currentBucketIndex) {
            currentBucketIndex = bucketIndex;
        }
    }

    public Node poll() {
        if (size == 0) return null;

        // Fast-scan for next non-empty bucket
        // Array access is much faster than Map.containsKey()
        while (currentBucketIndex < CAPACITY && (buckets[currentBucketIndex] == null || buckets[currentBucketIndex].isEmpty())) {
            currentBucketIndex++;
        }

        if (currentBucketIndex >= CAPACITY) return null;

        List<Node> currentBucket = buckets[currentBucketIndex];

        // Remove last element (O(1) operation)
        Node node = currentBucket.remove(currentBucket.size() - 1);
        size--;

        return node;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}