# Multi-stage build for batch-job-service
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
RUN chmod +x mvnw && ./mvnw dependency:go-offline -pl batch-job-service -q

COPY batch-job-service/src batch-job-service/src
RUN ./mvnw clean package -DskipTests -pl batch-job-service -q

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/batch-job-service/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
