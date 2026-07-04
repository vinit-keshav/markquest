$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$composeFile = Join-Path $root "backend\docker-compose.yml"
$pidDir = Join-Path $root "tmp\dev-pids"

if (Test-Path $pidDir) {
    Get-ChildItem $pidDir -Filter "*.pid" | ForEach-Object {
        $name = $_.BaseName
        $pidValue = Get-Content $_.FullName -ErrorAction SilentlyContinue

        if ($pidValue -and (Get-Process -Id $pidValue -ErrorAction SilentlyContinue)) {
            Stop-Process -Id $pidValue -Force
            Write-Host "Stopped $name with PID $pidValue"
        } else {
            Write-Host "$name was not running"
        }

        Remove-Item -LiteralPath $_.FullName -Force
    }
}

Write-Host "Stopping Kafka and Zookeeper..."
docker compose -f $composeFile down

Write-Host "Dev services stopped."
