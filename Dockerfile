# Use official Java 17 base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy source code
COPY src src

# Make Maven wrapper executable
RUN chmod +x mvnw

# Build the Spring Boot app
RUN ./mvnw package -DskipTests

# Expose port 8080
EXPOSE 8080

# Run the jar (adjust the JAR name if needed)
CMD ["java", "-jar", "target/lakeSide-hotel-demo-server-0.0.1-SNAPSHOT.jar"]

