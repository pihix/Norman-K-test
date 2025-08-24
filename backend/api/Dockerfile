# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copie ciblée pour tirer les deps en cache rapidement
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw
COPY pom.xml pom.xml

# (Optionnel) pré-résoudre plugins/déps, mais on ignore l'échec
RUN ./mvnw -B -ntp dependency:resolve-plugins dependency:resolve || true
# Copie du code + ressources
COPY src src
COPY db db

RUN for i in 1 2 3; do \
    ./mvnw -B -ntp -DskipTests \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.http.pool=false \
    -Dmaven.wagon.http.connectionTimeout=60000 \
    -Dmaven.wagon.http.readTimeout=60000 \
    package && break || { \
    echo "retry $i..."; \
    rm -rf ~/.m2/repository/org/mock-server || true; \
    sleep 5; \
    }; \
    done

# ---------- Runtime stage ----------
FROM eclipse-temurin:21.0.7_6-jre-alpine
ENV USER_NAME=api-user \
    APP_HOME=/opt/api-user/app \
    SPRING_PROFILES_ACTIVE=default \
    JVM_HEAP_SETTINGS="-Xmx256m" \
    JAVA_RUN_OPTS="-Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -XX:+UseParallelGC -XshowSettings:all -XX:+PrintFlagsFinal" \
    JAVA_OPTS=""

# User non-root + dossier appli
RUN adduser -D --shell /bin/bash --home /opt/${USER_NAME} ${USER_NAME} \
    && mkdir -p ${APP_HOME}

# Jar depuis le stage builder
COPY --from=builder /app/target/*.jar ${APP_HOME}/application.jar
RUN chown -R ${USER_NAME}: ${APP_HOME}

USER ${USER_NAME}
WORKDIR ${APP_HOME}
EXPOSE 8080

CMD java -Djava.security.egd=file:/dev/./urandom $JVM_HEAP_SETTINGS $JAVA_RUN_OPTS $JAVA_OPTS -jar application.jar
