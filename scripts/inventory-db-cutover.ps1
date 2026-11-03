# Part 2, step B: switch the running inventory service from db-main to db-inventory
# without dropping requests. Requests wait briefly while the pool is swapped.
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

$compose = @("compose", "-f", "docker-compose.demo.yml")
$admin = "http://localhost:8081/inventory/admin/datasource"

$fingerprintSql = @"
SELECT (SELECT count(*) FROM inventory.holds) || '|' ||
       (SELECT coalesce(sum(quantity), 0) || ',' || coalesce(sum(reserved), 0) FROM inventory.stock) || '|' ||
       (SELECT coalesce(string_agg(status || ':' || c, ',' ORDER BY status), '')
          FROM (SELECT status, count(*) AS c FROM inventory.holds GROUP BY status) s)
"@ -replace "\r?\n", " "

function Psql($service, $sql) {
    $out = docker @compose exec -T -e "PGOPTIONS=-c client_min_messages=warning" $service psql -v ON_ERROR_STOP=1 -U fractalith -d fractalith -tAc $sql
    if ($LASTEXITCODE -ne 0) { throw "psql failed on ${service}: $sql" }
    return ($out | Out-String).Trim()
}

Write-Host "==> Freezing inventory database access (new requests wait, in-flight ones finish)" -ForegroundColor Cyan
Invoke-RestMethod -Method Post "$admin/freeze?autoResumeSeconds=30" | ConvertTo-Json -Compress

try {
    Write-Host "==> Waiting for db-inventory to catch up with db-main" -ForegroundColor Cyan
    $caughtUp = $false
    for ($i = 0; $i -lt 30; $i++) {
        $old = Psql "db-main" $fingerprintSql
        $new = Psql "db-inventory" $fingerprintSql
        if ($old -eq $new) { $caughtUp = $true; break }
        Start-Sleep -Seconds 1
    }
    if (-not $caughtUp) { throw "Databases did not converge: main=$old new=$new" }
    Write-Host "    identical: $old"

    Write-Host "==> Switching the connection pool to db-inventory" -ForegroundColor Cyan
    Invoke-RestMethod -Method Post "$admin/cutover" | ConvertTo-Json -Compress
} catch {
    Write-Host "Cutover failed, resuming on the original database: $_" -ForegroundColor Red
    Invoke-RestMethod -Method Post "$admin/abort" | Out-Null
    throw
}

Write-Host "==> Stopping replication (db-main is no longer used by inventory)" -ForegroundColor Cyan
Psql "db-inventory" "DROP SUBSCRIPTION inventory_sub" | Out-Null
Psql "db-main" "DROP PUBLICATION IF EXISTS inventory_pub" | Out-Null

Write-Host "Cutover complete. Inventory now runs on db-inventory." -ForegroundColor Green