# Etapp 1: Buildime Springi rakenduse
FROM maven:3.9-eclipse-temurin-17 as builder

WORKDIR /build

# Kopeerime kogu projekti
COPY . .

# Laeme sõltuvused ja ehitame JAR-i
RUN mvn clean package -DskipTests

# Etapp 2: Päris jooksutamise etapp koos Chrome'iga
FROM selenium/standalone-chrome:124.0

# Kasutame õigesti kasutajat, et vältida permission erroreid
USER seluser

WORKDIR /home/seluser/app

# Kopeerime valminud Spring Boot JAR-faili
COPY --from=builder --chown=seluser:seluser /build/backend/spring/target/spring-1.0.0.jar ./spring.jar

# Avame pordi
EXPOSE 8080

# Käivitame Spring rakenduse
ENTRYPOINT ["java", "-jar", "spring.jar"]
