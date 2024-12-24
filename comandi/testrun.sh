#!/bin/bash

# Verifica che i file siano compilati
if [ ! -f "src/com/example/Server.class" ]; then
    echo "Files not compiled. Running testcompile.sh first..."
    ./comandi/testcompile.sh
fi

# Esegui il server in locale
echo "Starting local server..."
java -cp "lib/*:src" com.example.Server