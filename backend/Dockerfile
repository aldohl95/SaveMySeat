# ---- Stage 1: build the jar ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Dependencies first: this layer is cached until pom.xml changes
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Source second: changes on every edit, so it comes after
COPY src src
RUN ./mvnw -B package -DskipTests

# ---- Stage 2: run it ----
FROM eclipse-temurin:25-jre
WORKDIR /app

# Don't run as root
RUN useradd --system --no-create-home appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "app.jar"]