FROM maven:3.9-eclipse-temurin-17 as builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src /app/src

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app

RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

COPY --from=builder --chown=appuser:appgroup /app/target/online-chat-1.0.0.jar /app/online-chat.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/online-chat.jar"]
