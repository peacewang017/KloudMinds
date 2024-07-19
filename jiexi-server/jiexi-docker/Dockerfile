# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# Set environment variables for UTF-8 encoding
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven build output (the jar file) from the target directory
COPY target/jiexi-0.0.1-SNAPSHOT.jar /app/jiexi.jar

# Create /config directory
RUN mkdir /config

# Make port 9898 available to the world outside this container
EXPOSE 9898

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/jiexi.jar", "--spring.config.location=file:/config/application.yaml"]