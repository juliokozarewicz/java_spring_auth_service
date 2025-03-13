FROM openjdk:17-jdk-slim as build
WORKDIR /app
COPY build/libs/application.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]