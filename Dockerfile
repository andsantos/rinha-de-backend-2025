#FROM eclipse-temurin:17-jre-alpine
FROM ghcr.io/graalvm/jdk-community:24

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:ZUncommitDelay=10", "-jar", "app.jar"]
