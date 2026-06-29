# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:21-jdk AS builder

ARG SERVICE_NAME
ARG COMMON_UTILS_VERSION
ARG GPR_USER
ARG GPR_TOKEN

RUN test -n "$SERVICE_NAME" || (echo "ERROR: SERVICE_NAME build-arg is required" && exit 1)
RUN test -n "$COMMON_UTILS_VERSION" || (echo "ERROR: COMMON_UTILS_VERSION build-arg is required" && exit 1)
RUN test -n "$GPR_USER" || (echo "ERROR: GPR_USER build-arg is required" && exit 1)
RUN test -n "$GPR_TOKEN" || (echo "ERROR: GPR_TOKEN build-arg is required" && exit 1)

WORKDIR /build

# Gradle wrapper и build-файлы отдельным слоем — для кеша зависимостей.
COPY ${SERVICE_NAME}/gradlew ./
COPY ${SERVICE_NAME}/gradle ./gradle
COPY ${SERVICE_NAME}/build.gradle ${SERVICE_NAME}/settings.gradle ./

RUN sed -i 's/\r$//' ./gradlew && \
    chmod +x ./gradlew && \
    ./gradlew dependencies \
      -PcommonUtilsVersion=${COMMON_UTILS_VERSION} \
      -PgprUser=${GPR_USER} \
      -PgprToken=${GPR_TOKEN} \
      --no-daemon || true

# Исходники и сборка.
COPY ${SERVICE_NAME}/src ./src

RUN ./gradlew bootJar \
      -PcommonUtilsVersion=${COMMON_UTILS_VERSION} \
      -PgprUser=${GPR_USER} \
      -PgprToken=${GPR_TOKEN} \
      --no-daemon -x test && \
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
