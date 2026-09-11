$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$composeFile = Join-Path $root "backend\docker-compose.yml"
$logDir = Join-Path $root "tmp\dev-logs"
$pidDir = Join-Path $root "tmp\dev-pids"

New-Item -ItemType Directory -Force -Path $logDir | Out-Null
New-Item -ItemType Directory -Force -Path $pidDir | Out-Null

foreach ($commandName in @('java', 'mvn', 'docker')) {
    if (!(Get-Command $commandName -ErrorAction SilentlyContinue)) {
        throw "$commandName is missing from PATH. Install it and reopen your terminal."
    }
}
docker info *> $null
if ($LASTEXITCODE -ne 0) { throw 'Docker engine is unavailable. Open Docker Desktop and wait until it is running.' }

$envFile = Join-Path $root '.env'
if (!(Test-Path -LiteralPath $envFile)) {
    throw 'Create .env from .env.example and configure JWT_SECRET, MySQL and SMTP before starting.'
}
$jwtLine = Get-Content -LiteralPath $envFile | Where-Object { $_ -match '^JWT_SECRET=.' } | Select-Object -First 1
if (!$env:JWT_SECRET -and (!$jwtLine -or $jwtLine -match 'replace-with-')) {
    throw 'Set a private random JWT_SECRET (at least 32 bytes) in .env. See README.md.'
}

function Start-SpringService {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Name,

        [Parameter(Mandatory = $true)]
        [string] $ServicePath
    )

    $logFile = Join-Path $logDir "$Name.log"
    $pidFile = Join-Path $pidDir "$Name.pid"
    $escapedServicePath = $ServicePath.Replace("'", "''")
    $escapedLogFile = $logFile.Replace("'", "''")
    $runner = if (Test-Path (Join-Path $ServicePath "mvnw.cmd")) { ".\mvnw.cmd" } else { "mvn" }
    $command = "Set-Location -LiteralPath '$escapedServicePath'; $runner spring-boot:run *> '$escapedLogFile'"

    if (Test-Path $pidFile) {
        $existingPid = Get-Content $pidFile -ErrorAction SilentlyContinue
        if ($existingPid -and (Get-Process -Id $existingPid -ErrorAction SilentlyContinue)) {
            Write-Host "$Name is already running with PID $existingPid"
            return
        }
    }

    $process = Start-Process `
        -FilePath "powershell.exe" `
        -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-EncodedCommand", [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($command))) `
        -WindowStyle Hidden `
        -PassThru

    Set-Content -Path $pidFile -Value $process.Id
    Write-Host "Started $Name with PID $($process.Id)"
    Write-Host "Log: $logFile"
}

Write-Host "Starting Kafka and Zookeeper..."
docker compose -f $composeFile up -d
if ($LASTEXITCODE -ne 0) { throw 'Docker Compose failed. No Spring services were launched.' }

Write-Host 'Waiting for Kafka to accept requests...'
$kafkaReady = $false
for ($attempt = 0; $attempt -lt 30; $attempt++) {
    docker compose -f $composeFile exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list *> $null
    if ($LASTEXITCODE -eq 0) { $kafkaReady = $true; break }
    Start-Sleep -Seconds 2
}
if (!$kafkaReady) { throw 'Kafka did not become ready. Inspect docker compose logs before retrying.' }

Start-SpringService `
    -Name "notification-service" `
    -ServicePath (Join-Path $root "backend\notification-service")

Start-SpringService `
    -Name "auth-service" `
    -ServicePath (Join-Path $root "backend\auth-service")

Start-SpringService `
    -Name "watchlist-service" `
    -ServicePath (Join-Path $root "backend\watchlist-service")

Start-SpringService `
    -Name "portfolio-service" `
    -ServicePath (Join-Path $root "backend\portfolio-service")

Start-SpringService `
    -Name "trading-service" `
    -ServicePath (Join-Path $root "backend\trading-service")

Write-Host ""
Write-Host "Five implemented backend services launched. Check each log for successful Spring startup."
Write-Host "MySQL must be running and configured separately. Frontend: cd frontendr; npm run dev"
Write-Host "Watch logs with:"
Write-Host "  Get-Content -Wait tmp\dev-logs\notification-service.log"
Write-Host "  Get-Content -Wait tmp\dev-logs\auth-service.log"
Write-Host "  Get-Content -Wait tmp\dev-logs\watchlist-service.log"
Write-Host "  Get-Content -Wait tmp\dev-logs\portfolio-service.log"
Write-Host "  Get-Content -Wait tmp\dev-logs\trading-service.log"
