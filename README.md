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

Stage     Image                            Purpose
----------------------------------------------------------------------
Stage 1  maven:3.9.6-eclipse-temurin-17    compiles code, builds JAR
----------------------------------------------------------------------
Stage 2  eclipse-temurin:17-jre-alpine     runs JAR only
----------------------------------------------------------------------

Final image contains only the JRE and the JAR — no Maven, no JDK, no source code. This keeps the image small and secure.

# docker-compose.yml
version: '3.8'
services:
  app:
    image: manojgurjar22/devops_project_2:v1
    container_name: society_shops
    restart: always
    ports:
      - "80:8080"
    environment:
      spring.datasource.url: jdbc:mysql://mysql:3306/society_shops?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      spring.datasource.username: manoj
      spring.datasource.password: Manoj@123
    depends_on:
      - mysql

  mysql:
    image: mysql:8.0
    container_name: mysql_db
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: society_shops
      MYSQL_USER: manoj
      MYSQL_PASSWORD: Manoj@123
    ports:
      - "3307:3306"
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:

# Jenkinsfile
pipeline {
    agent any
    stages {
        stage('Copy files to workspace') {
            steps {
                sh 'rsync -avz /home/manoj/devops_projects/devops_project_2/Dockerfile .'
                sh 'rsync -avz /home/manoj/devops_projects/devops_project_2/pom.xml .'
                sh 'rsync -avz /home/manoj/devops_projects/devops_project_2/src ./'
                sh 'rsync -avz /home/manoj/devops_projects/devops_project_2/docker-compose.yml .'
            }
        }
        stage('Build Docker image') {
            steps {
                sh 'docker build -t devops_project_2 .'
            }
        }
        stage('Tag and push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    sh 'docker tag devops_project_2 manojgurjar22/devops_project_2:v1'
                    sh 'docker push manojgurjar22/devops_project_2:v1'
                }
            }
        }
        stage('Deploy with Docker Compose') {
            steps {
                sh 'docker compose down'
                sh 'docker compose up -d'
            }
        }
    }
}

# Setup Guide
## Prerequisites

-> Jenkins installed and running on port 8080
-> Docker installed with jenkins user in docker group
-> Docker Compose installed
-> Docker Hub credentials added to Jenkins as dockerhub-creds
  
# Run locally without Jenkins

# Clone the repo
git clone https://github.com/manojgurjar22/devops_project_2
cd devops_project_2

# Build the image
docker build -t devops_project_2:v1 .

# Start all services
docker compose up -d

# Verify both containers are running
docker compose ps

Open http://localhost in your browser.

# Verify everything is working

# Check containers
docker container ls

# Check HTTP response
curl -s -o /dev/null -w "%{http_code}" http://localhost/shops
# expected: 200

# Check MySQL
docker exec -it mysql_db mysql -u manoj -p
# enter password: Manoj@123
show databases;
# should show society_shops

# How Docker Compose Works Here

Feature          How it's used
--------------------------------------------------------------------------------------------------
Service          Spring Boot uses mysql as hostname — Docker resolves it to the MySQL container IP
discovery
--------------------------------------------------------------------------------------------------
depends_on       MySQL starts before Spring Boot
--------------------------------------------------------------------------------------------------
restart: always  If Spring Boot starts before MySQL is ready it auto-restarts
--------------------------------------------------------------------------------------------------
volumes          MySQL data persists even if container is stopped or removed
--------------------------------------------------------------------------------------------------
networks         Both containers share the same default network automatically
--------------------------------------------------------------------------------------------------

# Key Learnings
-> Multi-stage Dockerfile keeps final image small — JRE only, no build tools
-> mvn dependency:go-offline -B caches Maven dependencies as a Docker layer — speeds up rebuilds
-> Never use localhost in docker-compose — use the service name instead
-> depends_on controls startup order but not readiness — restart: always handles the gap
302 redirect is normal Spring Security behaviour — not an error
-> Docker volumes persist database data across container restarts
-> Port mapping 3307:3306 exposes MySQL on host port 3307, keeping 3306 free

# Next Steps

Add health check to docker-compose so app waits for MySQL to be truly ready
Move to AWS EC2 with GitHub webhook for automatic deployments
Add environment-specific configs using Spring profiles (dev, prod)
Store credentials in Jenkins secrets instead of docker-compose environment variables

# Author
Manoj Gurjar — Built as part of a DevOps + AI learning project series.
