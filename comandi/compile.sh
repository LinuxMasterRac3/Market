#!/bin/bash

# Get the absolute path of the project root directory
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT" || exit 1

# Create necessary directories
echo "Creating directories..."
mkdir -p target/classes

# Download dependencies if lib is empty
if [ ! -d "lib" ] || [ -z "$(ls -A lib)" ]; then
    echo "Dependencies not found. Downloading..."
    mkdir -p lib
    JETTY_VERSION="9.4.44.v20210927"
    MAVEN_CENTRAL="https://repo1.maven.org/maven2"
    
    # Download dependencies with retry and error checking
    download_with_retry() {
        local url=$1
        local output=$2
        local max_attempts=3
        local attempt=1
        
        while [ $attempt -le $max_attempts ]; do
            if wget -q "$url" -O "$output"; then
                return 0
            fi
            echo "Download attempt $attempt failed, retrying..."
            attempt=$((attempt + 1))
            sleep 1
        done
        return 1
    }
    
    # Array of required dependencies
    DEPS=(
        "org/eclipse/jetty/jetty-server/$JETTY_VERSION/jetty-server-$JETTY_VERSION.jar"
        "org/eclipse/jetty/jetty-servlet/$JETTY_VERSION/jetty-servlet-$JETTY_VERSION.jar"
        "org/eclipse/jetty/jetty-util/$JETTY_VERSION/jetty-util-$JETTY_VERSION.jar"
        "org/eclipse/jetty/jetty-http/$JETTY_VERSION/jetty-http-$JETTY_VERSION.jar"
        "org/eclipse/jetty/jetty-io/$JETTY_VERSION/jetty-io-$JETTY_VERSION.jar"
        "org/eclipse/jetty/jetty-security/$JETTY_VERSION/jetty-security-$JETTY_VERSION.jar"
        "org/eclipse/jetty/jetty-webapp/$JETTY_VERSION/jetty-webapp-$JETTY_VERSION.jar"
        "org/eclipse/jetty/jetty-xml/$JETTY_VERSION/jetty-xml-$JETTY_VERSION.jar"
        "javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar"
        "com/fasterxml/jackson/core/jackson-databind/2.13.0/jackson-databind-2.13.0.jar"
        "com/fasterxml/jackson/core/jackson-core/2.13.0/jackson-core-2.13.0.jar"
        "com/fasterxml/jackson/core/jackson-annotations/2.13.0/jackson-annotations-2.13.0.jar"
        "org/json/json/20231013/json-20231013.jar"
        "com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar"
        "com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar"
        "com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar"
        "org/postgresql/postgresql/42.6.0/postgresql-42.6.0.jar"
    )
    
    # Download dependencies
    for dep in "${DEPS[@]}"; do
        filename=$(basename "$dep")
        echo "Downloading $filename..."
        if ! download_with_retry "$MAVEN_CENTRAL/$dep" "lib/$filename"; then
            echo "Failed to download $filename"
            exit 1
        fi
    done
fi

# Clean and compile
echo "Compiling Java files..."
find src -name "*.java" | xargs javac -cp "lib/*:target/classes" -d target/classes

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
else
    echo "Compilation failed!"
    exit 1
fi

# No hardcoded INSTANCE_CONNECTION_NAME found; compilation focuses on building the project

