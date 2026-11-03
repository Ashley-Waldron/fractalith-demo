# Part 2, step A: create the inventory tables in the new database and start
# replicating the inventory schema from db-main into db-inventory.
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

$compose = @("compose", "-f", "docker-compose.demo.yml")
$admin = "http://localhost:8081/inventory/admin/datasource"

function Psql($service, $sql) {
    docker @compose exec -T -e "PGOPTIONS=-c client_min_messages=warning" $service psql -v ON_ERROR_STOP=1 -U fractalith -d fractalith -tAc $sql
    if ($LASTEXITCODE -ne 0) { throw "psql failed on ${service}: $sql" }
}

Write-Host "==> Creating inventory tables in the new database (Flyway, via the inventory service)" -ForegroundColor Cyan
Invoke-RestMethod -Method Post "$admin/prepare" | ConvertTo-Json -Compress

Write-Host "==> Publishing inventory tables on db-main" -ForegroundColor Cyan
Psql "db-main" "DROP PUBLICATION IF EXISTS inventory_pub"
Psql "db-main" "CREATE PUBLICATION inventory_pub FOR TABLE inventory.stock, inventory.holds"

Write-Host "==> Subscribing db-inventory (copies existing rows, then streams changes)" -ForegroundColor Cyan
Psql "db-inventory" "CREATE SUBSCRIPTION inventory_sub CONNECTION 'host=db-main port=5432 dbname=fractalith user=fractalith password=fractalith' PUBLICATION inventory_pub"

Write-Host "==> Waiting for the initial copy to finish" -ForegroundColor Cyan
for ($i = 0; $i -lt 60; $i++) {
    $pending = (Psql "db-inventory" "SELECT count(*) FROM pg_subscription_rel WHERE srsubstate <> 'r'").Trim()
    if ($pending -eq "0") { break }
    Start-Sleep -Seconds 1
}
if ($pending -ne "0") { throw "Initial copy did not finish" }

Write-Host "Replication is running. Next: scripts\inventory-db-cutover.ps1" -ForegroundColor Green