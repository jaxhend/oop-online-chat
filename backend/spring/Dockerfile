# Etapp 1: ehita Maveniga
FROM maven:3.9-eclipse-temurin-17 as builder

WORKDIR /app
COPY . .

# Lae sõltuvused ja paki JAR
RUN mvn clean package -DskipTests

# Etapp 2: jooksuta Selenium Chrome peal
FROM selenium/standalone-chrome:124.0

USER seluser
WORKDIR /home/seluser/app

# Võta ainult valmis JAR ehitusest
COPY --from=builder /app/backend/spring/target/spring-1.0.0.jar ./spring.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "spring.jar"]

