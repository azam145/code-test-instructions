# --- Stage 1: Build ---
# Use a Maven image with JDK 21 to build the artifact
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 1. Copy only the pom.xml first to cache dependencies
# This speeds up subsequent builds if dependencies haven't changed
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copy the source code and build the application
# We skip tests here because they were already run in the CI pipeline/local terminal
COPY src ./src
RUN mvn package -DskipTests

# --- Stage 2: Runtime ---
# Use a slim JRE (Java Runtime Environment) for the final image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 3. Security: Create a non-root user to run the application
# Running as root in a container is a major security risk (Lead-level best practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 4. Copy the compiled jar from the build stage
# Note: Ensure the JAR name matches your pom.xml artifactId/version
COPY --from=build /app/target/*.jar app.jar

# 5. Expose the application port
EXPOSE 8080

# 6. Optimized Entrypoint
# We use 'exec' form and tune the JVM for container resource limits
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]