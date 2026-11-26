# ---------- Etapa 1: compilación ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# ---------- Etapa 2: imagen de ejecución liviana ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S bloom && adduser -S bloom -G bloom
COPY --from=build /app/target/bloom-beauty-api-*.jar app.jar
USER bloom
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
