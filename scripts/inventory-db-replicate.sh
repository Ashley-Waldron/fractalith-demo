#!/usr/bin/env bash
# Part 2, step A: create the inventory tables in the new database and start
# replicating the inventory schema from db-main into db-inventory.
set -euo pipefail
cd "$(dirname "$0")/.."

compose=(docker compose -f docker-compose.demo.yml)
admin=http://localhost:8081/inventory/admin/datasource

psql_run() { "${compose[@]}" exec -T -e "PGOPTIONS=-c client_min_messages=warning" "$1" psql -v ON_ERROR_STOP=1 -U fractalith -d fractalith -tAc "$2"; }

echo "==> Creating inventory tables in the new database (Flyway, via the inventory service)"
curl -fsS -X POST "$admin/prepare"; echo

echo "==> Publishing inventory tables on db-main"
psql_run db-main "DROP PUBLICATION IF EXISTS inventory_pub"
psql_run db-main "CREATE PUBLICATION inventory_pub FOR TABLE inventory.stock, inventory.holds"

echo "==> Subscribing db-inventory (copies existing rows, then streams changes)"
psql_run db-inventory "CREATE SUBSCRIPTION inventory_sub CONNECTION 'host=db-main port=5432 dbname=fractalith user=fractalith password=fractalith' PUBLICATION inventory_pub"

echo "==> Waiting for the initial copy to finish"
pending=x
for _ in $(seq 60); do
  pending=$(psql_run db-inventory "SELECT count(*) FROM pg_subscription_rel WHERE srsubstate <> 'r'" | tr -d '[:space:]')
  [ "$pending" = "0" ] && break
  sleep 1
done
[ "$pending" = "0" ] || { echo "Initial copy did not finish"; exit 1; }

echo "Replication is running. Next: scripts/inventory-db-cutover.sh"