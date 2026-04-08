# Dockerized Microservices — Spring Boot + MySQL
A fully containerized Java Spring Boot application with MySQL database, orchestrated using Docker Compose and deployed via Jenkins CI/CD pipeline.

# Architecture
Browser
    │
    │  port 80
    ▼
Spring Boot container (society_shops)
    │
    │  JDBC — mysql:3306
    ▼
MySQL container (mysql_db)
    │
    │  persists data
    ▼
Docker volume (mysql_data)

Both containers run on the same Docker network — Spring Boot finds MySQL using the service name mysql, not localhost.

# Tech Stack
 Tool               Purpose 
----------------------------------------------------
 Spring Boot     |   REST API backend 
----------------------------------------------------
 MySQL 8.0       |  Database 
----------------------------------------------------
Docker           |  Containerization
----------------------------------------------------
Docker Compose   |  Multi-container orchestration
----------------------------------------------------
Jenkins          |  CI/CD pipeline
----------------------------------------------------
Maven            |  Build tool
----------------------------------------------------
Docker Hub       |  Image registry
----------------------------------------------------
eclipse-temurin:17  |  JRE for running the app
----------------------------------------------------

# Project Structure
devops_project_2/
├── src/                    ← Spring Boot source code
├── pom.xml                 ← Maven dependencies
├── Dockerfile              ← Multi-stage build
├── docker-compose.yml      ← Orchestrates app + MySQL
└── Jenkinsfile             ← CI/CD pipeline

# Dockerfile — Multi-stage Build
# Stage 1 — Build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /devops_project_2
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2 — Run
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl wget
WORKDIR /devops_project_2
COPY --from=builder /devops_project_2/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Why multi-stage?

Stage     Image     Purpose
-------------------------------
Stage 1  maven:3.9.6-eclipse-temurin-17   
