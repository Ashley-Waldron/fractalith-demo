#!/usr/bin/env bash
# Part 2, step B: switch the running inventory service from db-main to db-inventory
# without dropping requests. Requests wait briefly while the pool is swapped.
set -euo pipefail
cd "$(dirname "$0")/.."

compose=(docker compose -f docker-compose.demo.yml)
admin=http://localhost:8081/inventory/admin/datasource

fingerprint_sql="SELECT (SELECT count(*) FROM inventory.holds) || '|' ||
  (SELECT coalesce(sum(quantity), 0) || ',' || coalesce(sum(reserved), 0) FROM inventory.stock) || '|' ||
  (SELECT coalesce(string_agg(status || ':' || c, ',' ORDER BY status), '')
     FROM (SELECT status, count(*) AS c FROM inventory.holds GROUP BY status) s)"

psql_run() { "${compose[@]}" exec -T -e "PGOPTIONS=-c client_min_messages=warning" "$1" psql -v ON_ERROR_STOP=1 -U fractalith -d fractalith -tAc "$2" | tr -d '\r'; }

echo "==> Freezing inventory database access (new requests wait, in-flight ones finish)"
curl -fsS -X POST "$admin/freeze?autoResumeSeconds=30"; echo

abort() { echo "Cutover failed, resuming on the original database" >&2; curl -fsS -X POST "$admin/abort" >/dev/null; }
trap abort ERR

echo "==> Waiting for db-inventory to catch up with db-main"
caught_up=false
for _ in $(seq 30); do
  old=$(psql_run db-main "$fingerprint_sql")
  new=$(psql_run db-inventory "$fingerprint_sql")
  if [ "$old" = "$new" ]; then caught_up=true; break; fi
  sleep 1
done
$caught_up || { echo "Databases did not converge: main=$old new=$new" >&2; false; }
echo "    identical: $old"

echo "==> Switching the connection pool to db-inventory"
curl -fsS -X POST "$admin/cutover"; echo
trap - ERR

echo "==> Stopping replication (db-main is no longer used by inventory)"
psql_run db-inventory "DROP SUBSCRIPTION inventory_sub" >/dev/null
psql_run db-main "DROP PUBLICATION IF EXISTS inventory_pub" >/dev/null

echo "Cutover complete. Inventory now runs on db-inventory."