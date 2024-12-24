# Use the official OpenJDK 17 runtime as the base image
FROM openjdk:17-jdk-alpine

# Install required tools: bash and dos2unix
RUN apk add --no-cache bash dos2unix

# Set the working directory to /app
WORKDIR /app

# Create all necessary directories including output
RUN mkdir -p comandi src/com/example lib web classes/com/example

# Copy the lib directory to the container
COPY lib/ lib/

# Copy the src/com/example directory to the container
COPY src/com/example/ src/com/example/

# Copy the web directory to the container
COPY web/ web/

# Copy all .sh files from comandi to the container's comandi directory
COPY comandi/*.sh comandi/

# Fix line endings of .sh files and set execute permissions
RUN dos2unix comandi/*.sh && \
    chmod +x comandi/*.sh

# Compile the application using the compile.sh script
RUN /bin/bash comandi/compile.sh

# Expose port 8080 for the application
EXPOSE 8080

# Define the default command to run the run.sh script
CMD ["/bin/bash", "comandi/run.sh"]