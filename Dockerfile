# Stage 1: Build the application
FROM eclipse-temurin:21.0.7_6-jdk AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
COPY gradle ./gradle
COPY gradlew ./
RUN ./gradlew clean build --no-daemon

# Stage 2: Create the final, smaller image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copy the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]