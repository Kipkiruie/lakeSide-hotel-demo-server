# =========================
# Stage 1: Build
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src src

RUN ./mvnw clean package -DskipTests


# =========================
# Stage 2: Run
# =========================
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy EXACT jar (based on your build output)
COPY --from=build /app/target/lakeSide-hotel-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 10000

ENV PORT=10000

ENTRYPOINT ["java", "-jar", "app.jar"]