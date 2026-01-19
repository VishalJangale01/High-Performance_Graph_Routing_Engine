# High-Performance Graph Routing Engine

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?style=for-the-badge&logo=spring)
![AWS](https://img.shields.io/badge/Deployed_on-AWS-232F3E?style=for-the-badge&logo=amazon-aws)

> **Project is Live on AWS ElasticBeans:** [http://graphrouting.eu-north-1.elasticbeanstalk.com/]  
> **Research Basis:** "Breaking the Sorting Barrier for Directed Single-Source Shortest Paths" (Duan et al., 2025)

## Project Overview
This project is a high-performance routing microservice engineered to benchmark the theoretical **"Sorting Barrier" breakthrough** against industry-standard algorithms.

It implements a custom **Bucket-Queue architecture ($O(m)$)** to replace the traditional **Binary Heap ($O(m \log n)$)** used in Dijkstra's algorithm. The result is a system capable of calculating shortest paths on massive, sparse graphs with significantly lower latency.

### ⚡ Key Performance Results
| Metric | Standard Dijkstra (PriorityQueue) | Duan Lite (Bucket Queue) | Improvement |
| :--- | :--- | :--- | :--- |
| **Time Complexity** | $O(m \log n)$ | $O(m)$ | **Log-Linear -> Linear** |
| **Execution Time** | ~5,300 ms | ~333 ms | **~15x Faster** |
| **Memory Footprint** | Low (Object Heavy) | Ultra-Low (Array Based) | **130 MB (Stable)** |
*(Benchmark: 100,000 Nodes, 5M Edges on AWS t2.micro)*

---

## 🏗️ System Architecture
The system is designed as a **decoupled architecture**: a static frontend hosted on GitHub Pages communicates with an ephemeral Spring Boot backend on AWS Elastic Beanstalk.

```mermaid
graph TD
    User[Recruiter / User] -->|HTTP Request| FE[Frontend Dashboard\n(GitHub Pages)]
    FE -->|REST API| LB[AWS Load Balancer]
    LB -->|Forward| App[Spring Boot Backend]
    
    subgraph "Core Engine (Java 17)"
        App -->|Dispatch| Service[Routing Service]
        Service -->|Legacy Path| Dijkstra[Standard Dijkstra\n(Binary Heap)]
        Service -->|Optimized Path| Duan[Duan Lite Implementation\n(Bucket Queue)]
        
        Duan -->|O(1) Access| RAM[In-Memory Graph]
        Dijkstra -->|O(log n) Sort| RAM
    end
