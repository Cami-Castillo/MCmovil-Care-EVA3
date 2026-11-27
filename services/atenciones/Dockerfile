FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN mvn -B -pl services/atenciones -am package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/services/atenciones/target/atenciones-4.0.0.jar app.jar
EXPOSE 8082
VOLUME /app/datos
ENTRYPOINT ["java","-jar","app.jar"]
