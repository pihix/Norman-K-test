FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests
RUN ls -la target # 🔍 debug visible dans logs GitHub

FROM eclipse-temurin:21-jre
ENV APP_HOME=/opt/api-user/app
WORKDIR ${APP_HOME}
COPY --from=builder /app/target/*.jar ${APP_HOME}/application.jar
RUN chown -R 1000:1000 ${APP_HOME}
USER 1000
CMD ["java", "-jar", "application.jar"]
