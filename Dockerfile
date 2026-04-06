#stage 1
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /devops_project_2

COPY pom.xml .
RUN mvn dependency:go-offline -B
   
COPY src ./src
RUN mvn clean package -DskipTests -B

#stage 2
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl wget

WORKDIR /devops_project_2
COPY --from=builder /devops_project_2/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
 
