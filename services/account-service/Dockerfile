FROM maven:3.9.0-eclipse-temurin-17-alpine  AS build
WORKDIR /app
COPY src ./src
COPY pom.xml .
RUN mvn clean package -D skipTests

FROM amazoncorretto:17.0.6-alpine3.17
COPY --from=build /app/target/*.jar ./app.jar
EXPOSE ${SERVER_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
