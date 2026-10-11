# TeamMatch - Backend

This is the backend component of TeamMatch, built with Spring Boot and Java 17.

## Setup and Requirements

- Java 17
- Maven
- MySQL Database (e.g. Aiven)

## Environment Variables

The application requires the following environment variables to run:

- `DB_URL`: The JDBC connection URL (e.g., `jdbc:mysql://HOST:PORT/teammatch_db?sslMode=REQUIRED`)
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `JWT_SECRET`: A secure random string used for signing JWT tokens

See `.env.example` in the project root for placeholders. For local development, create a `.env` file in the root of the project with your actual local secrets. **Never commit the `.env` file to version control.**

## Local Development

You can start the backend using the provided startup script which automatically loads the `.env` file:

```powershell
.\start.ps1
```

Or manually using Maven (after setting the required environment variables in your terminal):
```bash
./mvnw spring-boot:run
```

## Database Migrations

This project uses Flyway for database migrations. Flyway will automatically run on startup.
Hibernate `ddl-auto` is set to `validate` to ensure schema integrity without causing destructive changes.

## Deployment (Render Web Service)

The backend is configured for containerized deployment on Render using Docker:

1. **Service Type:** Web Service (Environment: Docker)
2. **Root Directory:** `backend`
3. **Dockerfile Path:** `./Dockerfile`
4. **Build & Start:** Handled automatically by the multi-stage `Dockerfile` and `entrypoint.sh`. The container starts with non-root security and dynamically binds to Render's injected `$PORT`.

### Required Environment Variables on Render

Configure the following in the Render Dashboard (**Environment** tab). Never commit these to version control:

- `DB_URL`: `jdbc:mysql://<AIVEN_HOST>:<AIVEN_PORT>/defaultdb?sslMode=VERIFY_IDENTITY&trustCertificateKeyStoreType=JKS&trustCertificateKeyStoreUrl=file:aiven-truststore.jks&connectTimeout=30000&socketTimeout=60000`
- `DB_USERNAME`: `avnadmin`
- `DB_PASSWORD`: `<your-aiven-mysql-password>`
- `TRUSTSTORE_PASSWORD`: `<strong-random-password-for-runtime-keystore>`
- `JWT_SECRET`: `<base64-encoded-256-bit-jwt-secret>`
- `ALLOWED_ORIGINS`: `https://<your-project>.vercel.app`

> **Note on TLS:** `entrypoint.sh` automatically reads `aiven-ca.pem` at container startup, generates `aiven-truststore.jks` in memory/ephemeral container storage using `$TRUSTSTORE_PASSWORD`, and augments `DB_URL` with `&trustCertificateKeyStorePassword=...`. No passwords or `.jks` files are baked into image layers or committed to Git.

