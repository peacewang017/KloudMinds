# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# Set environment variables for UTF-8 encoding
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven build output (the jar file) from the target directory
COPY target/springboot-0.0.1-SNAPSHOT.jar /app/springboot.jar

# Create /config directory and copy the application.yaml file into it
RUN mkdir /config
COPY config/application.yaml /config/application.yaml

# Make port 9090 available to the world outside this container
EXPOSE 9090

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/springboot.jar", "--spring.config.location=file:/config/application.yaml"]