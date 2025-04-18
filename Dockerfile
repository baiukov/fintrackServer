FROM openjdk:23-jdk-slim

WORKDIR /app

COPY fintrackServer-0.1.0.jar app.jar
COPY ssl/keystore2.p12 keystore.p12 

EXPOSE 443

ENTRYPOINT ["java", "-jar", "app.jar"]
