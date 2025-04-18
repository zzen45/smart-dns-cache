# ---- build stage ----
# use a tag that supports both amd64 + arm64
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- runtime stage ----
FROM eclipse-temurin:17-jre
ARG JAR=/app/target/dnscache-0.0.1-SNAPSHOT.jar
COPY --from=builder ${JAR} /dnscache.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/dnscache.jar"]
