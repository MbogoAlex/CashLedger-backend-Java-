# Step 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: Run the application
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app

# Copy the compiled JAR from the build stage
COPY --from=build /app/target/CashLedger-0.0.1-SNAPSHOT.jar CashLedger.jar

# Copy the JasperReports files
COPY --from=build /app/target/classes /app/classes

EXPOSE 8080
ENTRYPOINT ["java","-jar","CashLedger.jar"]
