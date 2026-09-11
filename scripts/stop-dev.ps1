$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$composeFile = Join-Path $root "backend\docker-compose.yml"
$pidDir = Join-Path $root "tmp\dev-pids"

if (Test-Path $pidDir) {
    Get-ChildItem $pidDir -Filter "*.pid" | ForEach-Object {
        $name = $_.BaseName
        $pidValue = Get-Content $_.FullName -ErrorAction SilentlyContinue

        if ($pidValue -and (Get-Process -Id $pidValue -ErrorAction SilentlyContinue)) {
            # Terminate Maven and Java children as well as the launcher shell.
            $launcher = Get-Process -Id $pidValue -ErrorAction SilentlyContinue
            if ($launcher.ProcessName -ne 'powershell') {
                throw "PID $pidValue no longer belongs to a PowerShell launcher; refusing to stop it."
            }
            taskkill /PID $pidValue /T /F
            if ($LASTEXITCODE -ne 0) { throw "Could not stop process tree for $name" }
            Write-Host "Stopped $name with PID $pidValue"
        } else {
            Write-Host "$name was not running"
        }

        Remove-Item -LiteralPath $_.FullName -Force
    }
}

Write-Host "Stopping Kafka and Zookeeper..."
docker compose -f $composeFile down
if ($LASTEXITCODE -ne 0) { throw 'Docker Compose could not stop the infrastructure.' }

Write-Host "Dev services stopped."
