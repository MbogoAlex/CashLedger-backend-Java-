# Stage 1: Build
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:17.0.1-jdk-slim
# Install required libraries for JasperReports
RUN apt-get update && apt-get install -y \
    libfreetype6 \
    libfontconfig1 \
    libxrender1 \
    libxext6 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /target/CashLedger-0.0.1-SNAPSHOT.jar CashLedger.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","CashLedger.jar"]
