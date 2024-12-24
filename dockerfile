FROM openjdk:17-jdk-slim

WORKDIR /app

COPY lib/json-20240303.jar ./lib/
COPY comandi/compile.sh comandi/run.sh ./comandi/
COPY src/ ./src/

RUN chmod +x ./comandi/compile.sh && ./comandi/compile.sh

EXPOSE 8080
CMD ["./comandi/run.sh"]