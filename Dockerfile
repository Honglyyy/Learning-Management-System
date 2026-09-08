# ==========================================
# Stage 1: Build the application
# ==========================================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy Maven wrapper and POM first for Docker layer caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Ensure wrapper script is executable and cache dependencies
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy project source code (includes application.properties)
COPY src ./src

# Build production JAR skipping tests
RUN ./mvnw clean package -DskipTests

# ==========================================
# Stage 2: Runtime stage for Render
# ==========================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create uploads folder
RUN mkdir -p /app/uploads

# Copy the packaged JAR from the builder stage
COPY --from=builder /app/target/LMS.jar app.jar

# Render dynamically sets the $PORT environment variable (typically 10000)
# Fallback to 8080 for local testing
EXPOSE 8080

# JVM options optimized for Render container memory limits (e.g. 512MB RAM)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Bind server port dynamically to Render's $PORT
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
