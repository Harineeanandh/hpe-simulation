# ---- BUILD STAGE ----
FROM gradle:8.3-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# ---- RUNTIME STAGE ----
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install netcat for wait script
RUN apt-get update && apt-get install -y netcat && rm -rf /var/lib/apt/lists/*

# Copy app and wait script
COPY --from=builder /app/build/libs/EmployeeService-1.0-SNAPSHOT.jar app.jar
COPY wait-for-postgres.sh .

# Make wait script executable
RUN chmod +x wait-for-postgres.sh

EXPOSE 8080

# Run wait script before launching app
ENTRYPOINT ["./wait-for-postgres.sh", "postgres", "5432", "java", "-jar", "app.jar"]
