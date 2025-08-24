# ---------- Build stage ----------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

RUN apk add --no-cache nodejs npm


COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml pom.xml


RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src

RUN ./mvnw clean package -DskipTests -Dgit.commit.id.skip=true -Dspotless.check.skip=true -B -X

# ---------- Runtime stage ----------
FROM eclipse-temurin:21.0.7_6-jre-alpine

ENV USER_NAME=api-user \
    APP_HOME=/opt/api-user/app \
    SPRING_PROFILES_ACTIVE=default \
    JVM_HEAP_SETTINGS="-Xmx256m" \
    JAVA_RUN_OPTS="-Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -XX:+UseParallelGC -XshowSettings:all -XX:+PrintFlagsFinal" \
    JAVA_OPTS=""

RUN adduser -D --shell /bin/bash --home /opt/${USER_NAME} ${USER_NAME} \
    && mkdir -p ${APP_HOME}

COPY --from=builder /app/target/*.jar ${APP_HOME}/application.jar
RUN chown -R ${USER_NAME}: ${APP_HOME}

USER ${USER_NAME}
WORKDIR ${APP_HOME}
EXPOSE 8080

CMD java -Djava.security.egd=file:/dev/./urandom $JVM_HEAP_SETTINGS $JAVA_RUN_OPTS $JAVA_OPTS -jar application.jar