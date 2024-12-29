#!/bin/bash

# Exit on any error
set -e

# Get the absolute path of the project root directory
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

# Check if PostgreSQL client is installed
if ! command -v psql &> /dev/null; then
    echo "Installing PostgreSQL client..."
    sudo apt-get update && sudo apt-get install -y postgresql-client
fi

# Source environment variables from .env
if [ -f "$PROJECT_ROOT/.env" ]; then
    echo "Loading environment variables from .env..."
    set -a  # automatically export all variables
    . "$PROJECT_ROOT/.env"
    set +a
else
    echo "Error: .env file not found in $PROJECT_ROOT"
    exit 1
fi

echo "Testing Supabase connection..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Host: $SUPABASE_DB_HOST"
echo "Database: $SUPABASE_DB_NAME"
echo "User: $SUPABASE_DB_USER"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Test the connection using PGPASSWORD with updated connection string (removed pool_mode)
PGPASSWORD=$SUPABASE_DB_PASSWORD psql \
    "postgresql://$SUPABASE_DB_USER@$SUPABASE_DB_HOST:$SUPABASE_DB_PORT/$SUPABASE_DB_NAME?sslmode=require" \
    --set=sslmode=require \
    -c "\conninfo" \
    -c "SELECT version();" \
    -c "SELECT NOW();"

# Check if the connection was successful
if [ $? -eq 0 ]; then
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "✅ Connection successful!"
else
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "❌ Connection failed!"
    exit 1
fi