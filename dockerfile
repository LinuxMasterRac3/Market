# Usa un'immagine di base leggera con OpenJDK
FROM openjdk:17-jdk-alpine

# Imposta la directory di lavoro all'interno del container
WORKDIR /app

# Copia le librerie e i file di sorgente
COPY lib/ lib/
COPY src/ src/
COPY comandi/run.sh comandi/run.sh
COPY comandi/compile.sh comandi/compile.sh

# Copia la directory web/ nel container
COPY web/ web/

# Imposta i permessi di esecuzione per compile.sh e eseguilo
RUN chmod +x comandi/compile.sh && sh comandi/compile.sh

# Assicurati che run.sh abbia i permessi di esecuzione
RUN chmod +x comandi/run.sh

# Esponi la porta specificata dall'ambiente o la porta predefinita
EXPOSE 8080

# Imposta il comando di avvio del container
CMD ["sh", "comandi/run.sh"]