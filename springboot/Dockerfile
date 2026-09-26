FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 9999
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseContainerSupport", "-jar", "app.jar"]
