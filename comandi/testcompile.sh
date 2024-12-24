#!/bin/bash

# Rimuovi i vecchi file compilati
echo "Removing old class files..."
find src/com/example/ -name "*.class" -delete

# Compila il codice con le dipendenze
echo "Compiling Java files..."
javac -cp "lib/*:src" src/com/example/*.java

# Verifica la compilazione
if [ $? -eq 0 ]; then
    echo "Compilation successful!"
else
    echo "Compilation failed!"
    exit 1
fi