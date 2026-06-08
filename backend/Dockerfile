# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:21-jdk AS builder

ARG SERVICE_NAME
RUN test -n "$SERVICE_NAME" || (echo "ERROR: SERVICE_NAME build-arg is required" && exit 1)

WORKDIR /build

# common-utils из локального .m2 (подготовлено prepare-build).
COPY .m2-common-utils /root/.m2/repository/com/casualgames

# Gradle wrapper и build-файлы отдельным слоем — для кеша зависимостей.
COPY ${SERVICE_NAME}/gradlew ./
COPY ${SERVICE_NAME}/gradle ./gradle
COPY ${SERVICE_NAME}/build.gradle ${SERVICE_NAME}/settings.gradle ./

RUN sed -i 's/\r$//' ./gradlew && \
    chmod +x ./gradlew && \
    ./gradlew dependencies --no-daemon || true

# Исходники и сборка.
COPY ${SERVICE_NAME}/src ./src

RUN ./gradlew bootJar --no-daemon -x test && \
    cp build/libs/*.jar /build/app.jar

# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder --chown=app:app /build/app.jar app.jar
USER app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
