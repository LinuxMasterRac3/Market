FROM openjdk:17-jdk-slim




RUN chmod +x ../comandi/compile.sh && ../comandi/compile.sh

EXPOSE 8080
CMD ["./comandi/run.sh"]