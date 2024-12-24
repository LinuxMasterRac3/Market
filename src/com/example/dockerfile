FROM openjdk:17-jdk-slim

WORKDIR /app

# Copia le dipendenze
COPY lib/json-20240303.jar ./lib/

# Copia gli script di compilazione e esecuzione
COPY comandi/compile.sh ./comandi/compile.sh
COPY comandi/run.sh ./comandi/run.sh

# Copia il codice sorgente
COPY src/ ./src/

# Compila il codice
RUN chmod +x ./comandi/compile.sh && ./comandi/compile.sh

EXPOSE 8080

CMD ["./comandi/run.sh"]
