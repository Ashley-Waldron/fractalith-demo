#!/usr/bin/env bash
# Sends continuous checkout requests through the gateway and reports failures.
url="${1:-http://localhost:8080/shop/checkout}"
delay="${2:-0.1}"

ok=0
failed=0
n=0

echo "Sending checkouts to $url (Ctrl+C to stop)"
while true; do
  n=$((n + 1))
  resp=$(curl -s -m 5 -D - -X POST "$url" -H "Content-Type: application/json" \
    -d "{\"customerId\":\"c-$n\",\"sku\":\"sku-1\",\"quantity\":1}")
  code=$(printf '%s' "$resp" | head -1 | awk '{print $2}')
  instance=$(printf '%s' "$resp" | tr -d '\r' | grep -i '^x-instance-id:' | awk '{print $2}')
  body=$(printf '%s' "$resp" | tr -d '\r' | tail -1)

  if [ "$code" = "200" ] && [[ "$body" == *"checked out"* ]]; then
    ok=$((ok + 1))
  else
    failed=$((failed + 1))
    echo "#$n FAILED (${instance:-?}) HTTP ${code:-none}: $body"
  fi

  if [ $((n % 10)) -eq 0 ]; then
    echo "sent=$n ok=$ok failed=$failed last_instance=${instance:-?}"
  fi
  sleep "$delay"
done
