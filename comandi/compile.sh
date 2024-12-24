#!/bin/bash
cd /app
# Remove class files
find src/com/example/ -name "*.class" -delete

# Compile Java files
javac -cp lib/json-20240303.jar:src $(find src/com/example -name "*.java")