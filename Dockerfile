# ---- BUILD STAGE ----
FROM gradle:8.3-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# ---- RUNTIME STAGE ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built JAR
COPY --from=builder /app/build/libs/EmployeeService-1.0-SNAPSHOT.jar app.jar

# Expose port 8080 (Cloud Run default)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
