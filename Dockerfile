# Stage 1: Build the WAR artifact
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and pre-fetch dependencies for better build caching
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build production WAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production runtime with Tomcat 10.1 (Jakarta EE 10 compatible)
FROM tomcat:10.1-jdk17-temurin-alpine
WORKDIR /usr/local/tomcat

# Clean up default Tomcat sample applications
RUN rm -rf webapps/*

# Deploy built WAR as ROOT.war (accessible at / without context path)
COPY --from=build /app/target/*.war webapps/ROOT.war

# Expose standard web port
EXPOSE 8080

# Start Tomcat server
CMD ["catalina.sh", "run"]
