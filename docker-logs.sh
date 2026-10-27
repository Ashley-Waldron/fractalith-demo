#!/usr/bin/env bash
set -euo pipefail

mode="${1:-split-search}"

case "$mode" in
  monolith)
    docker compose -f docker-compose.monolith.yml logs -f --tail=200
    ;;
  split-search)
    docker compose -f docker-compose.split-search.yml logs -f --tail=200 fractalith-app fractalith-search
    ;;
  *)
    echo "Usage: $0 [monolith|split-search]"
    echo "Default: split-search"
    exit 1
    ;;
esac
