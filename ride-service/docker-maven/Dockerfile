FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /project
COPY . /project/
RUN mvn clean package
EXPOSE 8080
CMD ["mvn", "exec:java", "-Dexec.mainClass=sap.ass02.vertxrideservice.infrastructure.Main" ]