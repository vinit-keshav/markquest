FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /build
ARG SERVICE
COPY ${SERVICE}/pom.xml ./pom.xml
COPY ${SERVICE}/src ./src
RUN mvn -B verify && cp target/*-SNAPSHOT.jar /application.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system marketquest && useradd --system --gid marketquest marketquest \
    && mkdir /app/uploads && chown marketquest:marketquest /app/uploads
COPY --from=build /application.jar /app/application.jar
USER marketquest
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70"
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
