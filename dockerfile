FROM openjdk:17-jdk-slim

WORKDIR /app

COPY comandi/ comandi/
COPY src/ src/
COPY lib/ lib/

RUN chmod +x comandi/compile.sh && comandi/compile.sh

EXPOSE 8080
CMD ["comandi/run.sh"]