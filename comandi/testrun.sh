#!/bin/bash

# Get the absolute path of the project root directory
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT" || exit 1

# Check if classes exist, if not compile first
if [ ! -d "target/classes" ]; then
    echo "Classes not found. Running compile.sh first..."
    ./comandi/compile.sh
fi

echo "Starting server..."
java -cp "lib/*:target/classes" com.example.WebServer

