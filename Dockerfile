# Esimene etapp: ehitame rakenduse
FROM maven:3.9-eclipse-temurin-17 as builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src /app/src

RUN mvn clean package -DskipTests

# Teine etapp: väiksem image
FROM openjdk:17-jdk-slim

WORKDIR /app

RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

# Kopeerime ehitatud JAR-faili
COPY --from=builder --chown=appuser:appgroup /app/target/online-chat-1.0.0.jar /app/online-chat.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/online-chat.jar"]
