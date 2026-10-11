# Navigate to the script's directory (backend)
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

# Load .env from root if it exists
$EnvFile = Join-Path $ScriptDir "..\.env"
if (Test-Path $EnvFile) {
    Write-Host "Loading environment variables from $EnvFile"
    Get-Content $EnvFile | Where-Object { $_ -match '^\s*[^#]' -and $_ -match '=' } | ForEach-Object {
        $name, $value = $_.Split('=', 2)
        $name = $name.Trim()
        $value = $value.Trim()
        if ($value -match '^"(.*)"$') { $value = $matches[1] }
        [Environment]::SetEnvironmentVariable($name, $value, [EnvironmentVariableTarget]::Process)
    }
} else {
    Write-Host "No .env file found at $EnvFile"
}

# Validate required variables
$required = @("DB_URL", "DB_USERNAME", "DB_PASSWORD", "JWT_SECRET")
$missing = @()
foreach ($var in $required) {
    if (-not [Environment]::GetEnvironmentVariable($var)) {
        $missing += $var
    }
}

if ($missing.Count -gt 0) {
    Write-Error "Missing required environment variables: $($missing -join ', ')"
    exit 1
}

# ── Aiven truststore generation (idempotent) ──────────────────────────
$DbUrl = [Environment]::GetEnvironmentVariable("DB_URL")
if ($DbUrl -and $DbUrl.Contains("aiven-truststore.jks")) {

    $TruststorePath = Join-Path $ScriptDir "aiven-truststore.jks"
    $CertPath       = Join-Path $ScriptDir "aiven-ca.pem"
    $StorePass      = [Environment]::GetEnvironmentVariable("TRUSTSTORE_PASSWORD")

    # Validate prerequisites
    if (-not $StorePass) {
        Write-Error "TRUSTSTORE_PASSWORD is not set in .env. Cannot generate truststore."
        exit 1
    }

    if (-not (Get-Command keytool -ErrorAction SilentlyContinue)) {
        if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\keytool.exe")) {
            $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
        } else {
            $foundKeytool = Get-ChildItem -Path "C:\Program Files\Java","C:\Program Files\Eclipse Adoptium","C:\Program Files\Microsoft" -Filter "keytool.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
            if ($foundKeytool) {
                $jdkBin = Split-Path -Parent $foundKeytool.FullName
                $env:PATH = "$jdkBin;$env:PATH"
                if (-not $env:JAVA_HOME) {
                    $env:JAVA_HOME = Split-Path -Parent $jdkBin
                }
            }
        }
    }

    if (-not (Get-Command keytool -ErrorAction SilentlyContinue)) {
        Write-Error "keytool not found on PATH. Install a JDK and ensure JAVA_HOME/bin is on PATH."
        exit 1
    }

    if (-not (Test-Path $CertPath)) {
        Write-Error "Aiven CA certificate not found at $CertPath"
        exit 1
    }

    # Generate JKS truststore if it does not already exist
    if (-not (Test-Path $TruststorePath)) {
        Write-Host "Generating JKS truststore from aiven-ca.pem ..."
        keytool -importcert -trustcacerts -noprompt `
            -alias aiven-ca `
            -file $CertPath `
            -keystore $TruststorePath `
            -storepass $StorePass 2>&1 | Write-Host

        if ($LASTEXITCODE -ne 0) {
            Write-Error "keytool import failed (exit code $LASTEXITCODE). Check the certificate file."
            exit 1
        }

        # Verify the certificate was imported correctly
        $listOutput = (keytool -list -keystore $TruststorePath -storepass $StorePass -alias aiven-ca 2>&1) | Out-String
        if ($LASTEXITCODE -ne 0 -or $listOutput -notmatch "aiven-ca") {
            Write-Error "Truststore verification failed - alias 'aiven-ca' not found after import."
            exit 1
        }

        Write-Host "Truststore created and verified: $TruststorePath"
    } else {
        Write-Host "Truststore already exists: $TruststorePath (skipping generation)"
    }

    # Append trustCertificateKeyStorePassword to DB_URL at runtime
    # This avoids nested ${} expansion issues in Spring Boot
    $AugmentedUrl = $DbUrl + "&trustCertificateKeyStorePassword=" + $StorePass
    [Environment]::SetEnvironmentVariable("DB_URL", $AugmentedUrl, [EnvironmentVariableTarget]::Process)
    Write-Host "DB_URL augmented with truststore password at runtime."
}

Write-Host "Starting Spring Boot application..."
if (Test-Path "mvnw") {
    ./mvnw spring-boot:run
} elseif (Test-Path "mvnw.cmd") {
    ./mvnw.cmd spring-boot:run
} else {
    mvn spring-boot:run
}
