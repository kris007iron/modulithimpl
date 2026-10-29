FROM eclipse-temurin:27-jdk AS build
WORKDIR /app
LABEL authors="kris007iron"

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:27-jre
WORKDIR /app

RUN useradd --system --uid 10001 appuser
COPY --from=build /app/target/modulithimpl-0.0.1-SNAPSHOT.jar app.jar

USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]