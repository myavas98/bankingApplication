FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY build/libs/transaction-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"] 