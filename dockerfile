# Use the official OpenJDK 17 runtime as the base image
FROM openjdk:17-jdk-alpine

# Install required tools: bash and dos2unix
RUN apk add --no-cache bash dos2unix

# Set the working directory to /app
WORKDIR /app

# Create directory structure
RUN mkdir -p src/com/example lib web classes comandi

# Copy files maintaining structure
COPY lib/* lib/
COPY src/com/example/* src/com/example/
COPY web/ web/
COPY comandi/*.sh comandi/

# Fix scripts and compile
RUN dos2unix comandi/*.sh && \
    chmod +x comandi/*.sh && \
    /bin/bash comandi/compile.sh

# Expose port 8080 for the application
EXPOSE 8080

# Define the default command to run the run.sh script
CMD ["/bin/bash", "comandi/run.sh"]