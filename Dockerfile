# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app
# Copy pom.xml and download dependencies (caches them for faster rebuilds)
COPY pom.xml .
RUN mvn dependency:go-offline
# Copy source code and package the app
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application using a lightweight JRE
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copy the compiled JAR from the build stage
COPY --from=build /app/target/urlShortner-0.0.1-SNAPSHOT.jar app.jar

# JVM Tuning for 512MB Render free tier
# Restricts max heap size to 256MB to leave room for OS and Metaspace
ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:+UseZGC"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]