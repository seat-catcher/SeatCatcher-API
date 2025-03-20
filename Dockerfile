# Build stage

FROM bellsoft/liberica-openjdk-alpine:17 AS builder

WORKDIR /app

COPY . .

RUN ./gradlew clean build -x test


# Run stage

FROM bellsoft/liberica-openjdk-alpine:17

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

COPY src/main/resources/prod.properties /config/application.properties

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar", "--spring.config.location=file:/config/application.properties"]