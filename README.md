# High-Performance Graph Routing Engine 

![Java](https://img.shields.io/badge/Java-17-orange?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?logo=spring&logoColor=white)
![AWS](https://img.shields.io/badge/Deployed_on-AWS-232F3E?logo=amazon-aws&logoColor=white)

> **Live Demo:** [AWS ElasticBeanStalk](http://graphrouting.eu-north-1.elasticbeanstalk.com/)  
> **API Docs:** [Swagger UI](http://graphrouting.eu-north-1.elasticbeanstalk.com/swagger-ui/index.html)  
> **Research Basis:** "Breaking the Sorting Barrier for Directed Single-Source Shortest Paths" (Duan et al., 2025)

## Project Overview
This project is a high-performance routing microservice engineered to benchmark the theoretical **"Sorting Barrier" breakthrough** against industry-standard algorithms. 

It implements a custom **Bucket-Queue architecture** (Linear Time) to replace the traditional **Binary Heap** (Log-Linear Time) used in Dijkstra's algorithm. The result is a system capable of calculating shortest paths on massive, sparse graphs with significantly lower latency.

### ⚡ Key Performance Results
*(Benchmark: 100,000 Nodes, 5M Edges on AWS)*

| Metric | Standard Dijkstra (PriorityQueue) | Duan Lite (Bucket Queue) | Improvement |
| :--- | :--- | :--- | :--- |
| **Time Complexity** | $O(m \log n)$ | $O(m \log n)>$ | **Log-Linear -> Nearly Linear** |
| **Execution Time** | ~5,300 ms | ~1333 ms | **Faster** |
| **Memory Footprint** | Low (Object Heavy) | Ultra-Low (Array Based) | **130 MB (Stable)** |

---

## System Architecture
The system follows a decoupled architecture. The frontend is a static dashboard that consumes the Spring Boot REST API hosted on AWS Elastic Beanstalk.

```mermaid
graph TD
    User([User]) -->|HTTP Request| FE[Frontend Dashboard]
    FE -->|REST API Call| LB[AWS Load Balancer]
    LB -->|Forward| App[Spring Boot Backend]
    
    subgraph Core Engine
        App -->|Dispatch| Service[Routing Service]
        Service -->|Legacy Path| Dijkstra[Standard Dijkstra]
        Service -->|Optimized Path| Duan[Duan Lite Algo]
        
        Duan -->|O-1 Access| RAM[(In-Memory Graph)]
        Dijkstra -->|Sort| RAM
    end
