FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd

# Prime Maven cache (dependencies only)
RUN mvn -q -DskipTests dependency:go-offline

COPY src src
RUN mvn -q -DskipTests package

# Use Alpine JRE 17 — much lighter (~90MB) to fit in Koyeb's 512MB free tier
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

# Hugging Face Spaces uses port 7860 by default; fall back to 8080 for local use
ENV PORT=7860
EXPOSE 7860

# Limit JVM heap to stay within free tier RAM limits
ENTRYPOINT ["sh", "-c", "java -Xmx256m -Xms128m -jar /app/app.jar --server.port=${PORT}"]

