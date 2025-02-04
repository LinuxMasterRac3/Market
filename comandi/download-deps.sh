#!/bin/bash

JETTY_VERSION="9.4.44.v20210927"
JACKSON_VERSION="2.15.2"
MAVEN_CENTRAL="https://repo1.maven.org/maven2"
LIB_DIR="lib"

mkdir -p $LIB_DIR

# Download dependencies with retry mechanism
download_with_retry() {
    local url=$1
    local output=$2
    local max_attempts=3
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if wget -q "$url" -O "$output"; then
            echo "Successfully downloaded $(basename "$output")"
            return 0
        fi
        echo "Download attempt $attempt failed, retrying..."
        attempt=$((attempt + 1))
        sleep 1
    done
    echo "Failed to download $url after $max_attempts attempts"
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
    "javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar"
    "com/fasterxml/jackson/core/jackson-databind/$JACKSON_VERSION/jackson-databind-$JACKSON_VERSION.jar"
    "com/fasterxml/jackson/core/jackson-core/$JACKSON_VERSION/jackson-core-$JACKSON_VERSION.jar"
    "com/fasterxml/jackson/core/jackson-annotations/$JACKSON_VERSION/jackson-annotations-$JACKSON_VERSION.jar"
    "org/postgresql/postgresql/42.6.0/postgresql-42.6.0.jar"
    "org/json/json/20231013/json-20231013.jar"
)

# Download dependencies
for dep in "${DEPS[@]}"; do
    filename=$(basename "$dep")
    if [ ! -f "$LIB_DIR/$filename" ]; then
        echo "Downloading $filename..."
        if ! download_with_retry "$MAVEN_CENTRAL/$dep" "$LIB_DIR/$filename"; then
            echo "Failed to download $filename"
            exit 1
        fi
    else
        echo "Skipping $filename - already exists"
    fi
done

echo "All dependencies downloaded successfully!"
