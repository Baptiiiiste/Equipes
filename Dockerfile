# ── Stage 1: Build ───────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Cache dependencies first (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build fat JAR
COPY src ./src
RUN mvn package -B -DskipTests

# ── Stage 2: Runtime ─────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build /app/target/Equipes-1.0-SNAPSHOT.jar app.jar

RUN chown -R appuser:appgroup /app
USER appuser

# Default environment variables (override in compose / portainer)
ENV APP_SERVER_PORT=8080
ENV APP_AUDIO_UDP_PORT=8081
ENV APP_DB_URL=jdbc:postgresql://postgres:5432/equipes
ENV APP_DB_USER=equipes
ENV APP_DB_PASSWORD=equipes

# TCP server port + UDP audio port
EXPOSE 8080
EXPOSE 8081/udp

ENTRYPOINT ["java", "-jar", "app.jar", "server"]
