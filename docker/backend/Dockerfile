# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Create upload directory
RUN mkdir -p /app/upload

# Environment variables for production (can be overridden)
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_URL=jdbc:mysql://mysql:3306/kecheng_campus_market
ENV DB_USERNAME=market_user
ENV DB_PASSWORD=market_password
ENV JWT_SECRET=your-production-jwt-secret-key-2025

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]