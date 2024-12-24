# Use OpenJDK lightweight base image
FROM openjdk:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy files in correct order
COPY lib/ ./lib/
COPY src/ ./src/
COPY web/ ./web/
COPY comandi/compile.sh ./comandi/compile.sh
COPY comandi/run.sh ./comandi/run.sh

# Make scripts executable and run compilation
RUN chmod +x ./comandi/compile.sh ./comandi/run.sh && \
    ./comandi/compile.sh

# Expose default port
EXPOSE 8080

# Set container startup command
CMD ["./comandi/run.sh"]