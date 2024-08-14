# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the Docker image
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/CashLedger-0.0.1-SNAPSHOT.jar CashLedger.jar

# Copy the additional JAR file into the image
COPY lib/jdt-compiler-3.1.1.jar /app/lib/jdt-compiler-3.1.1.jar

# Copy static resources
COPY --from=build /app/src/main/resources/templates /app/templates
COPY --from=build /app/src/main/resources/cashledger-logo.png /app/resources/cashledger-logo.png

# Set CLASSPATH to include the additional JAR
ENV CLASSPATH="/app/lib/jdt-compiler-3.1.1.jar:${CLASSPATH}"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "CashLedger.jar"]
