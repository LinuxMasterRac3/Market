#!/bin/bash

# Get the absolute path of the project root directory
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT" || exit 1

# Create necessary directories
echo "Creating directories..."
mkdir -p src/com/example/servlets lib web target/classes comandi data example/db

# Download dependencies if not exists
if [ ! -d "lib" ] || [ -z "$(ls -A lib)" ]; then
    echo "Dependencies not found. Downloading..."
    JETTY_VERSION="9.4.44.v20210927"
    MAVEN_CENTRAL="https://repo1.maven.org/maven2"

    # Replace array with a multi-line variable
    DEPS="org/eclipse/jetty/jetty-server/$JETTY_VERSION/jetty-server-$JETTY_VERSION.jar
    org/eclipse/jetty/jetty-servlet/$JETTY_VERSION/jetty-servlet-$JETTY_VERSION.jar
    org/eclipse/jetty/jetty-util/$JETTY_VERSION/jetty-util-$JETTY_VERSION.jar
    org/eclipse/jetty/jetty-http/$JETTY_VERSION/jetty-http-$JETTY_VERSION.jar
    org/eclipse/jetty/jetty-io/$JETTY_VERSION/jetty-io-$JETTY_VERSION.jar
    org/eclipse/jetty/jetty-webapp/$JETTY_VERSION/jetty-webapp-$JETTY_VERSION.jar
    org/eclipse/jetty/jetty-security/$JETTY_VERSION/jetty-security-$JETTY_VERSION.jar
    org/eclipse/jetty/jetty-xml/$JETTY_VERSION/jetty-xml-$JETTY_VERSION.jar
    org/eclipse/jetty/jetty-continuation/$JETTY_VERSION/jetty-continuation-$JETTY_VERSION.jar
    javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar
    # Rimuovi la seguente dipendenza se non utilizzi org.json
    # org/json/json/20210307/json-20210307.jar"

    # Download each Jetty dependency
    for dep in $DEPS; do
        echo "Downloading $dep..."
        wget -P lib "$MAVEN_CENTRAL/$dep"
    done

    # Rimuovi il download di org.json se non necessario
    # wget -P lib "$MAVEN_CENTRAL/org/json/json/20210307/json-20210307.jar"
fi

# Clean and compile
echo "Removing old class files..."
rm -rf target/classes
mkdir -p target/classes

echo "Compiling Java files..."
find src/com/example -name "*.java" | xargs javac -cp "lib/*:target/classes" -d target/classes

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
else
    echo "Compilation failed!"
    exit 1
fi

