#!/bin/bash
set -e

cd /app || exit 1

# Ensure PORT is set and exported
export PORT=${PORT:-8080}
echo "PORT environment variable is set to: $PORT"

# Use environment variables
DATA_DIR=${DATA_DIR:-"/data"}
INSTANCE_CONNECTION_NAME=${INSTANCE_CONNECTION_NAME:-""}
DB_HOST=${DB_HOST:-"nltoknxotsigtyrceuyt.supabase.co"}
DB_NAME=${DB_NAME:-"LinuxMasterRac3's Project"}
DB_USER=${DB_USER:-"your_supabase_user"}
DB_PASSWORD=${DB_PASSWORD:-"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5sdG9rbnhvdHNpZ3R5cmNldXl0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzU0OTc4MjIsImV4cCI6MjA1MTA3MzgyMn0.zg_LXjgxG0pZfbddQmlDIS7jHcl2_9uQdP3IrPh6flM"}

echo "DATA_DIR is set to $DATA_DIR"
echo "INSTANCE_CONNECTION_NAME is set to $INSTANCE_CONNECTION_NAME"
echo "DB_HOST is set to $DB_HOST"
echo "DB_NAME is set to $DB_NAME"
echo "DB_USER is set to $DB_USER"
echo "DB_PASSWORD is set to $DB_PASSWORD"

# Verify directories
if [ ! -d "$DATA_DIR" ]; then
    echo "Creating data directory..."
    mkdir -p "$DATA_DIR"
fi

# Set the classpath to include all JARs in lib and the compiled classes
export CLASSPATH="lib/*:target/classes"

echo "Starting server on port $PORT..."
# Redirect Java logs to stdout and stderr and bind to all network interfaces
exec java -cp "$CLASSPATH" \
    -Dserver.address=0.0.0.0 \
    -Djetty.http.port=$PORT \
    -Djetty.http.host=0.0.0.0 \
    -DPORT=$PORT \
    -DDATA_DIR="$DATA_DIR" \
    -DDB_HOST="$DB_HOST" \
    -DDB_NAME="$DB_NAME" \
    -DDB_USER="$DB_USER" \
    -DDB_PASSWORD="$DB_PASSWORD" \
    com.example.WebServer 1> /dev/stdout 2> /dev/stderr
