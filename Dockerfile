# syntax=docker/dockerfile:1.7

FROM gradle:8.14.3-jdk21 AS build

WORKDIR /app

COPY gradle gradle
COPY gradlew gradlew
COPY build.gradle settings.gradle gradle.properties ./

RUN chmod +x gradlew

RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew dependencies --no-daemon

COPY src src

RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8098

ENTRYPOINT ["java", "-jar", "app.jar"]
