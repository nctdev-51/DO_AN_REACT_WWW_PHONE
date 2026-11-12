FROM eclipse-temurin:17-jre

WORKDIR /app
COPY target/WWW_PHONE_VER1-0.0.1-SNAPSHOT.war app.war

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.war"]
