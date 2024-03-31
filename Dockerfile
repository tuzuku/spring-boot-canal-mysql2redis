# Use OpenJDK for JDK 21
FROM openjdk:21

# Add Maintainer Info
LABEL maintainer="your-email@example.com"

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file
ARG JAR_FILE=target/app.jar

# Copy the application's jar to the container
COPY ${JAR_FILE} app.jar

# JVM Options
ENV JAVA_OPTS="-Xms125m -Xmx256m"

# Run the jar file
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]