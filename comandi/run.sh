#!/bin/bash
cd /app
# Add current directory to classpath and use absolute paths
java -cp "/app/lib/json-20240303.jar:/app/classes" com.example.Server
