FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY --from=build /app/target/CashLedger-0.0.1-SNAPSHOT.jar CashLedger.jar
COPY --from=build /app/src/main/resources/templates/ /app/templates/
EXPOSE 8080
ENTRYPOINT ["java","-jar","CashLedger.jar"]
