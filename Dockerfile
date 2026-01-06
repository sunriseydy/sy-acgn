FROM azul/zulu-openjdk:25-jre

WORKDIR /opt/sy-acgn

# Copy executable jar
COPY ./build/tasks/_server_executableJarJvm/server-jvm-executable.jar ./server-jvm-executable.jar

# Expose the server port
EXPOSE 9390

# Run the server
ENTRYPOINT ["java", "-jar", "server-jvm-executable.jar"]
