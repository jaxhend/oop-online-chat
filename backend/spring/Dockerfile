FROM selenium/standalone-chrome:124.0

# Kasutaja ja töökaust
USER seluser
WORKDIR /home/seluser/app

# Kopeerime valmis ehitatud Spring Boot JAR-i
COPY --chown=seluser:seluser backend/spring/target/spring-1.0.0.jar ./spring.jar

# Ava port
EXPOSE 8080

# Käivitame rakenduse
ENTRYPOINT ["java", "-jar", "spring.jar"]