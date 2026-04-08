FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package \
    && JAR_FILE=$(find target -maxdepth 1 -type f -name '*.jar' ! -name 'original-*.jar' | head -n 1) \
    && cp "$JAR_FILE" target/app.jar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/app.jar /app/app.jar

USER spring:spring

ENV JAVA_OPTS=""

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
