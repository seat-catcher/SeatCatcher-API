# Build stage
FROM bellsoft/liberica-openjdk-alpine:17 AS builder
WORKDIR /app
COPY . .
COPY src/main/resources/application.properties src/main/resources/application.properties
RUN ./gradlew clean build -x test

# Run stage
FROM bellsoft/liberica-openjdk-alpine:17
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
COPY src/main/resources/json src/main/resources/json

RUN addgroup -S seat-catcher && \
    adduser -S seat-catcher-user -G seat-catcher && \
    chown -R seat-catcher-user:seat-catcher /app

USER seat-catcher-user

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1

ENTRYPOINT ["java","-jar","app.jar"]