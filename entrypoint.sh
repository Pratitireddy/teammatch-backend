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

    # Ensure aiven-ca.pem is available (file on disk OR decoded from env var)
    if [ ! -f "aiven-ca.pem" ]; then
        if [ -z "$AIVEN_CA_PEM_B64" ]; then
            echo "ERROR: aiven-ca.pem not found and AIVEN_CA_PEM_B64 env var is not set." >&2
            exit 1
        fi
        echo "Decoding AIVEN_CA_PEM_B64 -> aiven-ca.pem ..."
        echo "$AIVEN_CA_PEM_B64" | base64 -d > aiven-ca.pem

        # Validate that the decoded file looks like a PEM certificate
        if ! head -n1 aiven-ca.pem | grep -q "^-----BEGIN CERTIFICATE-----"; then
            echo "ERROR: Decoded aiven-ca.pem does not appear to be a valid PEM certificate." >&2
            rm -f aiven-ca.pem
            exit 1
        fi
        echo "aiven-ca.pem decoded and validated ($(wc -c < aiven-ca.pem) bytes)."
    else
        echo "Using existing aiven-ca.pem from build context."
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

# 3. Non-sensitive TCP Connectivity Diagnostic
if [ -n "$DB_URL" ]; then
    echo "=== DB Connectivity Diagnostic ==="
    # Extract host and port from JDBC URL without exposing credentials or params
    CLEAN_TARGET=$(echo "$DB_URL" | sed -e 's|^jdbc:[^/]*//||' -e 's|/.*||')
    DIAG_HOST=$(echo "$CLEAN_TARGET" | cut -d: -f1)
    DIAG_PORT=$(echo "$CLEAN_TARGET" | cut -s -d: -f2)
    [ -z "$DIAG_PORT" ] && DIAG_PORT=3306

    if [ -n "$DIAG_HOST" ]; then
        echo "Testing TCP connectivity to $DIAG_HOST:$DIAG_PORT (timeout 5s)..."
        if nc -w 5 -z "$DIAG_HOST" "$DIAG_PORT" 2>/dev/null; then
            echo "DIAGNOSTIC RESULT: TCP connection to $DIAG_HOST:$DIAG_PORT SUCCEEDED."
        else
            echo "DIAGNOSTIC RESULT: TCP connection to $DIAG_HOST:$DIAG_PORT TIMED OUT / FAILED."
            echo "WARNING: Outbound TCP to $DIAG_HOST:$DIAG_PORT is unreachable from this environment."
        fi
    fi
    echo "==================================="
fi

# 4. Launch Spring Boot application
echo "Starting Spring Boot application on port $APP_PORT..."
exec java -Dserver.port="$APP_PORT" -jar app.jar
