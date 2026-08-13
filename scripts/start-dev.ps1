$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$composeFile = Join-Path $root "backend\docker-compose.yml"
$logDir = Join-Path $root "tmp\dev-logs"
$pidDir = Join-Path $root "tmp\dev-pids"

New-Item -ItemType Directory -Force -Path $logDir | Out-Null
New-Item -ItemType Directory -Force -Path $pidDir | Out-Null

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
        -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", $command) `
        -WindowStyle Hidden `
        -PassThru

    Set-Content -Path $pidFile -Value $process.Id
    Write-Host "Started $Name with PID $($process.Id)"
    Write-Host "Log: $logFile"
}

Write-Host "Starting Kafka and Zookeeper..."
docker compose -f $composeFile up -d

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
Write-Host "All requested services started."
Write-Host "Watch logs with:"
Write-Host "  Get-Content -Wait tmp\dev-logs\notification-service.log"
Write-Host "  Get-Content -Wait tmp\dev-logs\auth-service.log"
Write-Host "  Get-Content -Wait tmp\dev-logs\watchlist-service.log"
Write-Host "  Get-Content -Wait tmp\dev-logs\portfolio-service.log"
Write-Host "  Get-Content -Wait tmp\dev-logs\trading-service.log"
