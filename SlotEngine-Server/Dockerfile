# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests package && \
    cp target/*.jar /workspace/app.jar

FROM eclipse-temurin:21-jre-jammy AS runtime

ARG APP_UID=10001
ARG APP_USER=slotengine

RUN groupadd --gid "${APP_UID}" "${APP_USER}" && \
    useradd --uid "${APP_UID}" \
            --gid "${APP_UID}" \
            --no-create-home \
            --shell /usr/sbin/nologin \
            "${APP_USER}"

WORKDIR /app

COPY --from=build --chown=${APP_UID}:${APP_UID} /workspace/app.jar ./app.jar

USER ${APP_UID}:${APP_UID}

EXPOSE 8080
STOPSIGNAL SIGTERM

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
