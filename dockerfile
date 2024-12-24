FROM openjdk:17-jdk-slim


RUN  comandi/compile.sh
RUN  comandi/run.sh
EXPOSE 8080
