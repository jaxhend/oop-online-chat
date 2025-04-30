# Esimene etapp: ehitame rakenduse
 FROM maven:3.9-eclipse-temurin-17 as builder

 WORKDIR /app

 COPY . .

 RUN mvn dependency:go-offline

 RUN mvn clean package -DskipTests

 # Teine etapp: väiksem image
 FROM openjdk:17-jdk-slim

 WORKDIR /app

 ENV TZ=Europe/Tallinn

 RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

 # Kopeerime ehitatud JAR-faili
  COPY --from=builder --chown=appuser:appgroup /app/backend/spring/target/spring-1.0.0.jar /app/spring.jar

  USER appuser

  EXPOSE 8080

  ENTRYPOINT ["java", "-jar", "/app/spring.jar"]