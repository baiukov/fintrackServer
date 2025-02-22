FROM openjdk:23-jdk-slim

WORKDIR /app

COPY fintrackServer-0.0.1.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
