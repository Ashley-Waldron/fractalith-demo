#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

compose=(docker compose -f docker-compose.demo.yml -f docker-compose.demo.split.yml)

for app in app-1 app-2 app-3; do
  echo "==> Restarting $app with inventory switched off"
  "${compose[@]}" up -d --no-deps --force-recreate --wait "$app"
  sleep 3
done

echo "Rolling restart complete"
