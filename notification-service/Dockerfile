FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY build/libs/notification-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"] 