# Use an OpenJDK base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/cicd-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the application's port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
