FROM gradle:8.14.3-jdk21 AS build

WORKDIR /app

COPY gradle gradle
COPY gradlew gradlew
COPY build.gradle settings.gradle gradle.properties ./
COPY src src

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8098

ENTRYPOINT ["java", "-jar", "app.jar"]
