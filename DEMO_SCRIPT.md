# Demo script: deploying the inventory module separately with zero downtime

**Story:** the app starts as a monolith (3 instances behind a gateway). Without
touching the code, we stand up the inventory module as its own service and
roll the monolith instances over to use it, while traffic keeps flowing and
nothing fails.

## What's in the stack

| Service | Purpose |
|---|---|
| `gateway` (Traefik) | Load balances `:8080` across the app instances, health-checks them, retries refused connections. Dashboard on `:9000` |
| `app-1`, `app-2`, `app-3` | The fractalith app. Same image for every instance and for inventory; behaviour is controlled only by environment variables |
| `kafka`, `kafka-init` | Kafka and the topics `orders.placed`, `payments.processed` (started in step 2) |
| `inventory` | Standalone inventory service on `:8081` (started in step 3) |

Files: `docker-compose.demo.yml` (base), `docker-compose.demo.split.yml`
(the "deployment": env overrides applied during the rolling restart),
`scripts/traffic.*` (traffic generator), `scripts/rolling-restart.*`.

## Prerequisites

- Docker Desktop with Compose v2 (`docker compose version`)
- Ports `8080` and `9000` free. Stop any other fractalith compose stack first
  (e.g. `docker compose -f docker-compose.inventory-separate.yml down`)
- Three terminals in the repo root:
  - **Terminal A**: commands and the rolling restart
  - **Terminal B**: traffic generator
  - **Terminal C**: logs
- Optional: browser tab on http://localhost:9000/dashboard/ (HTTP > Services > `app@file`
  shows each instance as UP/DOWN)

PowerShell users: run the `.ps1` scripts with `-ExecutionPolicy Bypass`
(shown below) if your machine blocks scripts.

Build the image up front so the demo isn't waiting on Maven (a few minutes the first time):

```
docker compose -f docker-compose.demo.yml build
```

## Step 1: Run the monolith, three instances

**Terminal A**

```
docker compose -f docker-compose.demo.yml up -d --wait
docker compose -f docker-compose.demo.yml ps
```

Say: every module (shop, order, payment, inventory, search) runs inside each
instance. Modules talk through in-process Spring events. There is no Kafka and
no inventory service yet.

Start traffic and leave it running for the rest of the demo.

**Terminal B** (PowerShell)

```
powershell -ExecutionPolicy Bypass -File scripts\traffic.ps1
```

**Terminal B** (bash)

```
./scripts/traffic.sh
```

Expected: a summary line every 10 requests, e.g.
`sent=100 ok=100 failed=0 | app-1=33 app-2=34 app-3=33`. The gateway spreads
requests across all three instances. A failure prints a red/explicit
`#N FAILED` line.

Show where inventory runs today. **Terminal C**

```
docker compose -f docker-compose.demo.yml logs -f app-1 app-2 app-3
```

Look for `InventoryService - Processing inventory hold request` and
`PaymentProcessedEventConsumer - decreasing inventory by 1`. The `[app-N]` tag
shows which instance handled it: inventory is in-process in the monolith.

(Filter if noisy: `... | Select-String InventoryService` or `... | grep InventoryService`.)

## Step 2: Create Kafka and the topics

**Terminal A**

```
docker compose -f docker-compose.demo.yml up -d --wait kafka kafka-init
docker compose -f docker-compose.demo.yml logs kafka-init
```

Expected: `Created topic orders.placed.` and `Created topic payments.processed.`.
Traffic keeps passing; the monolith doesn't use Kafka yet.

## Step 3: Run inventory as a standalone service

**Terminal A**

```
docker compose -f docker-compose.demo.yml up -d --wait inventory
```

Say: this is the same image started with `SPRING_PROFILES_ACTIVE=only-inventory`
and the other modules disabled. It exposes `POST /inventory/hold` over HTTP and
consumes `payments.processed` from Kafka. Nothing is calling it yet: the
monolith instances are still using their in-process inventory.

Optional proof it is idle but alive:

```
docker compose -f docker-compose.demo.yml logs --tail 20 inventory
```

## Step 4: Rolling restart of the monolith with inventory switched off

Show what changes. This override file is the entire "deployment":

```
type docker-compose.demo.split.yml     # PowerShell / cmd
cat docker-compose.demo.split.yml      # bash
```

- `MODULE_INVENTORY_ENABLED=false`: the inventory module isn't loaded in the monolith
- `MODULE_SHOP_APIS_INVENTORY_TYPE=http` and `..._URL=http://inventory:8081`: the shop module calls inventory over HTTP instead of in-process
- `MODULE_PAYMENT_PUBLISHERS_PAYMENT_PROCESSED=kafka`: payment events go to Kafka instead of in-process events

Then run the restart. It recreates one instance at a time and waits for each
to become healthy before moving to the next.

**Terminal A** (PowerShell)

```
powershell -ExecutionPolicy Bypass -File scripts\rolling-restart.ps1
```

**Terminal A** (bash)

```
./scripts/rolling-restart.sh
```

What to point at while it runs:

- **Terminal B**: `failed=0` keeps holding, and the per-instance counts keep rising
- **Dashboard**: an instance goes DOWN while it restarts and comes back UP
- **Terminal C**: lines from the instance being restarted stop and restart

Why it's safe: apps use graceful shutdown. On `SIGTERM` the readiness probe
flips so the gateway stops sending new requests, in-flight requests finish,
and the gateway retries any refused connection on another instance. Mixed
versions during the rollout are fine because the monolith instances that
haven't been restarted yet still handle inventory locally, while restarted
instances use the standalone service.

## Step 5: Prove everything still works

Traffic is still running with no failures. Now show the new request path.

Standalone inventory is receiving the work:

```
docker compose -f docker-compose.demo.yml logs --tail 30 inventory
```

Look for `InventoryController - Received request to hold inventory` (HTTP from
the monolith) and `PaymentProcessedKafkaEventConsumer - Received Kafka payment
processed event` (events over Kafka).

The monolith no longer runs inventory:

```
docker compose -f docker-compose.demo.yml logs --tail 400 app-1
```

Look for `HttpInventoryApi - Calling inventory service over HTTP` and
`KafkaEventPublisher - Published event to Kafka topic 'payments.processed'`, and no
`InventoryService` lines after the restart.

Check the totals from Terminal B and stop the generator with Ctrl+C. The
`failed` count should be `0`.

Optional extra: stop the inventory container and show that checkouts now fail
(`docker compose -f docker-compose.demo.yml stop inventory`), then start it
again. That's the new trade-off of running it as a separate service.

## Reset / cleanup

```
docker compose -f docker-compose.demo.yml --profile kafka --profile inventory down -v
```

To re-run the demo from the start, run that and begin again at Step 1. The
rolling restart changes the running containers' environment; `down` clears it.

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `port is already allocated` on 8080 | Another compose stack is running. `docker ps`, then bring that stack down |
| Script blocked: "running scripts is disabled" | Use `powershell -ExecutionPolicy Bypass -File ...` |
| `#N FAILED` right at the start of traffic | Instances are still starting. Wait for `up -d --wait` to finish |
| Code or config changes aren't picked up | The image is stale. Rebuild with `docker compose -f docker-compose.demo.yml build` (or `up -d --build`) |
| Gateway shows all instances DOWN | Check `docker compose -f docker-compose.demo.yml logs gateway`; recreate it with `up -d --force-recreate gateway` |
