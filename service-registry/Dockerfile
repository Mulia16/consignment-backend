# Multi-stage build for service-registry
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY service-registry/pom.xml service-registry/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY consignment-service/pom.xml consignment-service/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY email-service/pom.xml email-service/pom.xml
COPY batch-job-service/pom.xml batch-job-service/pom.xml
RUN chmod +x mvnw && ./mvnw dependency:go-offline -pl service-registry -q

COPY service-registry/src service-registry/src
RUN ./mvnw clean package -DskipTests -pl service-registry -q

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/service-registry/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
