# Use OpenJDK lightweight base image
FROM openjdk:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy libraries and source files
COPY lib/ lib/
COPY src/ src/
COPY comandi/run.sh comandi/run.sh
COPY comandi/compile.sh comandi/compile.sh
COPY web/ web/

# Update compile.sh to use correct paths and execute it
RUN chmod +x comandi/compile.sh && \
    sh comandi/compile.sh

# Set run.sh executable
RUN chmod +x comandi/run.sh

# Expose default port
EXPOSE 8080

# Set container startup command
CMD ["sh", "comandi/run.sh"]