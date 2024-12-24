#!/bin/bash
# Rimuovi tutti i file .class
find ../src/com/example/ -name "*.class" -delete

# Compila tutti i file Java includendo json-20240303.jar nel classpath
javac -cp ../lib/json-20240303.jar:../src $(find ../src/com/example -name "*.java")