FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY backend/build/libs/backend.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
