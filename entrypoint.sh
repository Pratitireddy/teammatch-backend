#!/bin/sh
set -e

echo "=== TeamMatch Backend Linux Startup ==="

# 1. Determine Port (Render injects PORT, default to 8081 if not provided)
APP_PORT="${PORT:-8081}"
echo "Configured listening port: $APP_PORT"

# 2. Check if Aiven JKS truststore is requested in DB_URL
if echo "$DB_URL" | grep -q "aiven-truststore.jks"; then
    echo "Aiven JKS truststore required by DB_URL."

    if [ -z "$TRUSTSTORE_PASSWORD" ]; then
        echo "ERROR: TRUSTSTORE_PASSWORD environment variable is not set." >&2
        exit 1
    fi

    if [ ! -f "aiven-ca.pem" ]; then
        echo "ERROR: aiven-ca.pem certificate file not found in $(pwd)." >&2
        exit 1
    fi

    # Generate truststore dynamically at runtime if not present
    if [ ! -f "aiven-truststore.jks" ]; then
        echo "Generating JKS truststore from aiven-ca.pem at runtime..."
        keytool -importcert -trustcacerts -noprompt \
            -alias aiven-ca \
            -file aiven-ca.pem \
            -keystore aiven-truststore.jks \
            -storepass "$TRUSTSTORE_PASSWORD"

        # Verify alias
        if ! keytool -list -keystore aiven-truststore.jks -storepass "$TRUSTSTORE_PASSWORD" -alias aiven-ca > /dev/null 2>&1; then
            echo "ERROR: Truststore verification failed for alias 'aiven-ca'." >&2
            exit 1
        fi
        echo "Truststore successfully created and verified."
    else
        echo "Truststore aiven-truststore.jks already exists."
    fi

    # Append trustCertificateKeyStorePassword to DB_URL dynamically if not already present
    if ! echo "$DB_URL" | grep -q "trustCertificateKeyStorePassword="; then
        export DB_URL="${DB_URL}&trustCertificateKeyStorePassword=${TRUSTSTORE_PASSWORD}"
        echo "DB_URL dynamically augmented with trustCertificateKeyStorePassword at runtime."
    fi
fi

# 3. Launch Spring Boot application
echo "Starting Spring Boot application on port $APP_PORT..."
exec java -Dserver.port="$APP_PORT" -jar app.jar
