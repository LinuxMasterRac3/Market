FROM openjdk:17-jdk-alpine
RUN apk add --no-cache bash dos2unix
WORKDIR /app
RUN mkdir -p comandi src lib web
COPY lib/ ./lib/
COPY src/ ./src/
COPY web/ ./web/
COPY comandi/compile.sh comandi/run.sh ./comandi/
RUN dos2unix ./comandi/.sh &&
chmod +x ./comandi/.sh
RUN /bin/bash ./comandi/compile.sh
EXPOSE 8080
CMD ["/bin/bash", "./comandi/run.sh"]