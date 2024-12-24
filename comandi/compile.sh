#!/bin/bash
cd /app

# Clean and create output directory
rm -rf classes
mkdir -p classes

# Compile Java files preserving package structure
javac -d classes -cp "lib/*:src" $(find src -name "*.java")

# Verify compilation and class files
echo "Verifying compilation..."
find classes -name "*.class"