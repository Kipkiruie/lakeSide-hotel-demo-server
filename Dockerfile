# =========================
# STAGE 1: BUILD
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only pom first (cache dependencies)
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copy source
COPY src src

# Build jar
RUN mvn clean package -DskipTests


# =========================
# STAGE 2: RUN
# =========================
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000

ENV PORT=10000

ENTRYPOINT ["java", "-jar", "app.jar"]