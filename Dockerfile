FROM gradle:8-jdk21-alpine AS builder

FROM eclipse-temurin:21-jre-alpine AS runtime

FROM builder AS build
WORKDIR /app
ADD . .
RUN gradle --no-daemon assemble

FROM builder AS development
RUN apk add --no-cache curl
WORKDIR /app

FROM runtime AS production
LABEL maintainer="HMPPS Digital Studio <info@digital.justice.gov.uk>"
ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}
RUN apk add --no-cache tzdata curl
ENV TZ=Europe/London
RUN cp "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone
RUN addgroup --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --ingroup appgroup
WORKDIR /app
COPY --from=build --chown=appuser:appgroup /app/build/libs/hmpps-assess-risks-and-needs*.jar /app/app.jar
COPY --from=build --chown=appuser:appgroup /app/build/libs/applicationinsights-agent*.jar /app/agent.jar
COPY --from=build --chown=appuser:appgroup /app/applicationinsights.json /app
USER 2000
ENTRYPOINT ["java", "-javaagent:/app/agent.jar", "-jar", "/app/app.jar"]
