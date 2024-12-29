#!/bin/bash

JETTY_VERSION="9.4.44.v20210927"
MAVEN_CENTRAL="https://repo1.maven.org/maven2"
LIB_DIR="../lib"

mkdir -p $LIB_DIR

# Download Jetty dependencies
JETTY_DEPS=(
    "org/eclipse/jetty/jetty-server/$JETTY_VERSION/jetty-server-$JETTY_VERSION.jar"
    "org/eclipse/jetty/jetty-servlet/$JETTY_VERSION/jetty-servlet-$JETTY_VERSION.jar"
    "org/eclipse/jetty/jetty-util/$JETTY_VERSION/jetty-util-$JETTY_VERSION.jar"
    "org/eclipse/jetty/jetty-http/$JETTY_VERSION/jetty-http-$JETTY_VERSION.jar"
    "org/eclipse/jetty/jetty-io/$JETTY_VERSION/jetty-io-$JETTY_VERSION.jar"
    "org/eclipse/jetty/jetty-security/$JETTY_VERSION/jetty-security-$JETTY_VERSION.jar"
    "org/eclipse/jetty/jetty-webapp/$JETTY_VERSION/jetty-webapp-$JETTY_VERSION.jar"
)

# Download Servlet API
wget -P $LIB_DIR "$MAVEN_CENTRAL/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar"

# Download JSON library
wget -P $LIB_DIR "$MAVEN_CENTRAL/org/json/json/20210307/json-20210307.jar"

# Download Jetty dependencies
for DEP in "${JETTY_DEPS[@]}"; do
    wget -P $LIB_DIR "$MAVEN_CENTRAL/$DEP"
done

echo "All dependencies downloaded successfully!"
