# Multi-stage build for auth-service
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
RUN chmod +x mvnw && ./mvnw dependency:go-offline -pl auth-service -q

COPY auth-service/src auth-service/src
RUN ./mvnw clean package -DskipTests -pl auth-service -q

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/auth-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
