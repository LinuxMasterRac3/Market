FROM openjdk:17-jdk-slim
WORKDIR /app
COPY comandi/ comandi/
RUN chmod +x comandi/compile.sh comandi/run.sh
RUN comandi/compile.sh
RUN comandi/run.sh
EXPOSE 8080
