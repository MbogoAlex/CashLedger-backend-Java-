# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the Docker image
FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/CashLedger-0.0.1-SNAPSHOT.jar CashLedger.jar
COPY --from=build /src/main/resources/templates /templates
COPY --from=build /src/main/resources/cashledger-logo.png /resources/cashledger-logo.png
EXPOSE 8080
ENTRYPOINT ["java","-jar","CashLedger.jar"]
