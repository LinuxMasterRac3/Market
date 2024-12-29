FROM openjdk:17-jdk-alpine

# Aggiorna i pacchetti e installa le dipendenze necessarie
RUN apk update && \
    apk upgrade && \
    apk add --no-cache bash dos2unix wget postgresql-client

WORKDIR /app

# Crea la struttura delle directory
RUN mkdir -p src/com/example/servlets lib web target/classes comandi data

# Copia i file di origine e gli script
COPY src/com/example/*.java src/com/example/
COPY src/com/example/servlets/*.java src/com/example/servlets/
COPY web/ web/
COPY comandi/ comandi/

# Assicurati che non vi siano COPY o RUN comandi che gestiscono file JSON

# Fix delle permessi e line endings
RUN dos2unix comandi/*.sh && \
    chmod +x comandi/*.sh && \
    chmod +x comandi/run.sh

# Compila il progetto
RUN ./comandi/compile.sh

# Set the working directory and data directory
WORKDIR /app
ENV DATA_DIR=/data

# Definisce un volume per i dati persistenti
ENV DATA_DIR=/data

# Cloud Run specific configurations
EXPOSE ${PORT}

# Make web directory
RUN mkdir -p /app/web

# Copy web content
COPY web/ /app/web/

# Set correct permissions
RUN chmod -R 755 /app

# Set Supabase-specific environment variables (excluding sensitive data)
ENV SUPABASE_DB_HOST=nltoknxotsigtyrceuyt.supabase.co
ENV SUPABASE_DB_USER=postgres
ENV SUPABASE_DB_NAME=postgres
# Do NOT set SUPABASE_DB_PASSWORD here - it will be provided at runtime

VOLUME ["/data"]

# Simplify the CMD to ensure proper startup
CMD exec java -cp "lib/*:target/classes" \
    -Dserver.address=0.0.0.0 \
    -Djetty.http.port=${PORT} \
    -Djetty.http.host=0.0.0.0 \
    com.example.WebServer