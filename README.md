# Spring Boot Microservices with Kubernetes (E-Commerce Demo)

This project is a **demonstration of using Spring Boot + Microservices + Kubernetes** to build a production-style backend system.

It implements a **workable backend for a small e-commerce platform** consisting of independent microservices for **Order, Product, and Payment** modules, following real-world microservice architecture principles.

---

### Purpose of This Project
- **Demonstrate real-world microservice architecture**
- **Practice Kubernetes orchestration**
- **Apply resilience patterns (circuit breaker, rate limiting)**
- **Implement distributed tracing and secure service communication**
- **Showcase production-ready Spring Boot practices**

---

## Architecture Overview

The system is built using **Spring Boot microservices**, orchestrated with **Kubernetes**, and containerized using **Docker**.

### Core Microservices
- **API Gateway (Spring Cloud Gateway)**
- **Service Registry (Eureka)**
- **Config Server**
- **Order Service**
- **Product Service**
- **Payment Service**

### Supporting Services
- **MySQL** – primary database
- **Redis** – circuit breaker & rate limiting
- **Zipkin** – distributed tracing

---

## E-Commerce Features
- Order creation and management
- Product catalog handling
- Payment processing
- Inter-service communication using Feign Clients
- Secure token propagation across services

---

## Database
- **MySQL** is used as the main relational database.
- Deployed in Kubernetes as a **StatefulSet** to ensure:
  - Stable network identity
  - Persistent storage
- **PersistentVolumeClaims (PVCs)** are used so that:
  - Database data survives pod restarts
  - Storage is decoupled from pod lifecycle

---

## Distributed Tracing & Communication
- **Zipkin** is used for distributed log tracing across microservices.
- **Feign Client** is used for internal REST API communication.
- **Token Relay** is implemented to forward authentication tokens from the API Gateway to downstream services securely.

---

## Resilience & Security
- **Redis** is used to implement:
  - Circuit Breaker logic (to prevent cascading failures)
  - Rate Limiting (to protect REST APIs from possible DDoS attacks)
- These mechanisms help improve system stability and fault tolerance.

---

## Testing Strategy
- **Unit Testing** using:
  - **Mockito** to mock databases and internal service dependencies
  - **WireMock** to mock external REST endpoints
- Ensures services can be tested independently and reliably.

---

## Kubernetes Setup (Local with Minikube)

Kubernetes is run locally using **Minikube on Docker**.

### Kubernetes Resources Created
- **Deployments** for all stateless Spring Boot microservices
- **StatefulSet** for MySQL
- **Services**:
  - ClusterIP for internal service communication
  - NodePort for API Gateway access
- **PersistentVolumeClaims (PVCs)** for MySQL storage
- **ConfigMaps & environment variables** for configuration management

This setup closely mirrors a real production Kubernetes environment while remaining lightweight for local development.

---

## How to Run the Project (Minikube)

### 1️. Install Required Tools
- Docker
- Minikube
- kubectl

### 2️. Start Minikube
```bash
minikube start
```

### 3. Apply Kubernetes Manifests
```bash
kubectl apply -f k8s/
```

### 4. Check Pod Status
```bash
kubectl get pods
```

### 5. Access the API Gateway
```bash
minikube service cloud-gateway-app
```

## Docker Images

All services are already containerized and pushed to Docker Hub.

If you want to try this project yourself, please email me at:

rapheo.abdullah20@gmail.com

I will provide you access to my Docker Hub repository so you can pull the images and run the project locally.

