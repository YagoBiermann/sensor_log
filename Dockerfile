FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY src/ src/

RUN chmod +x ./gradlew && ./gradlew clean build -x test

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
