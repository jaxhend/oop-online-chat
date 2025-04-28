# Etapp 1: Buildime projekti Maveniga
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

# Etapp 2: Väiksem lõpp-image koos Selenium Chrome'iga
FROM selenium/standalone-chrome:124.0

USER seluser
WORKDIR /home/seluser/app

COPY --chown=seluser:seluser --from=builder /app/backend/spring/target/spring-1.0.0.jar ./spring.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "spring.jar"]