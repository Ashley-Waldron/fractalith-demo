$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

$compose = @("compose", "-f", "docker-compose.demo.yml", "-f", "docker-compose.demo.split.yml")

foreach ($app in "app-1", "app-2", "app-3") {
    Write-Host "==> Restarting $app with inventory switched off" -ForegroundColor Cyan
    docker @compose up -d --no-deps --force-recreate --wait $app
    if ($LASTEXITCODE -ne 0) { throw "Failed to restart $app" }
    Start-Sleep -Seconds 3
}

Write-Host "Rolling restart complete" -ForegroundColor Green
