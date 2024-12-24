#!/bin/bash
cd /app
# Remove all .class files
find src/com/example/ -name "*.class" -delete

# Compile all Java files with json jar in classpath
javac -cp lib/json-20240303.jar:src $(find src/com/example -name "*.java")