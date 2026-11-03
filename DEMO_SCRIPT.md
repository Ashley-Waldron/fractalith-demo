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
| `db-main` (Postgres) | One database, one schema per module: `shop`, `orders`, `payments`, `search`, `inventory`. Created by Flyway at startup. `wal_level=logical` so schemas can be replicated |
| `db-inventory` (Postgres) | Empty second database used in Part 2 (profile `migration`) |
| `kafka`, `kafka-init` | Kafka and the topics `orders.placed`, `payments.processed` (started in step 2) |
| `inventory` | Standalone inventory service on `:8081` (started in step 3) |

Files: `docker-compose.demo.yml` (base), `docker-compose.demo.split.yml`
(the "deployment": env overrides applied during the rolling restart),
`scripts/traffic.*` (traffic generator), `scripts/rolling-restart.*`.

## Prerequisites

- Docker Desktop with Compose v2 (`docker compose version`)
- Ports `8080`, `8081` and `9000` free. Stop any other fractalith compose stack first
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
`InventoryService - Committed inventory for order ...`. The `[app-N]` tag
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

## Part 2 (longer demo): move the inventory database while serving traffic

Do Steps 1-5 first and leave the traffic generator running. Inventory is now a
standalone service, but its tables still live in the shared `db-main`.

### 6. Show one database, one schema per module

```
docker compose -f docker-compose.demo.yml exec db-main psql -U fractalith -d fractalith -c "\dt *.*" | findstr /v pg_ | findstr /v information_schema
docker compose -f docker-compose.demo.yml exec db-main psql -U fractalith -d fractalith -c "SELECT * FROM inventory.stock"
docker compose -f docker-compose.demo.yml exec db-main psql -U fractalith -d fractalith -c "SELECT status, count(*) FROM inventory.holds GROUP BY 1"
```

(bash: replace the `findstr` pipes with `grep -v pg_ | grep -v information_schema`.)

Say: each module only touches its own schema (no cross-schema joins or foreign
keys), so a schema can move on its own. Inventory's holds/stock counts keep growing
because of the live traffic.

### 7. Start the new database and replicate the inventory schema

```
docker compose -f docker-compose.demo.yml up -d --wait db-inventory
```

PowerShell: `powershell -ExecutionPolicy Bypass -File scripts\inventory-db-replicate.ps1`
bash: `./scripts/inventory-db-replicate.sh`

The script (read it aloud, it's short):

1. Asks the running inventory service to create the empty inventory tables in
   `db-inventory` (`POST /inventory/admin/datasource/prepare`, same Flyway migrations)
2. Creates a Postgres **publication** on `db-main` for `inventory.stock` and `inventory.holds`
3. Creates a **subscription** on `db-inventory`: Postgres copies the existing rows,
   then streams every change

Inventory is still reading and writing `db-main`; nothing user-visible changed.
Optional: show the replica filling up and tracking the original:

```
docker compose -f docker-compose.demo.yml exec db-inventory psql -U fractalith -d fractalith -c "SELECT count(*) FROM inventory.holds"
```

Why replication and not dual writes: the app writes to exactly one database, so there is no
cross-database atomicity problem and no manual rollback. Postgres does the copying and
guarantees ordering.

### 8. Cut over the live service

PowerShell: `powershell -ExecutionPolicy Bypass -File scripts\inventory-db-cutover.ps1`
bash: `./scripts/inventory-db-cutover.sh`

What the script does:

1. **Freeze**: new inventory DB connections wait (up to 30s); in-flight transactions finish.
   Checkout requests just see a short pause. A safety timer lifts the freeze by itself if anything goes wrong.
2. **Verify**: wait until a fingerprint of both databases is identical (hold count, stock totals, holds by status)
3. **Switch**: the inventory service builds a connection pool for `db-inventory` and swaps it in; waiting requests resume on the new database
4. **Clean up**: drops the subscription and publication

Expected output ends with `identical: <count>|<stock>|CONFIRMED:<count>` and
`Inventory now runs on db-inventory`.

### 9. Prove it

- **Terminal B**: `failed=0` the whole time
- Rows now only grow in the new database:

```
docker compose -f docker-compose.demo.yml exec db-main psql -U fractalith -d fractalith -tAc "SELECT count(*) FROM inventory.holds"
docker compose -f docker-compose.demo.yml exec db-inventory psql -U fractalith -d fractalith -tAc "SELECT count(*) FROM inventory.holds"
```

Run each twice a few seconds apart: `db-main` is frozen at the cutover count, `db-inventory` keeps increasing.
- `curl http://localhost:8081/inventory/admin/datasource` shows `currentUrl` pointing at `db-inventory`.

Note: the inventory container's `DB_URL` environment still names `db-main`. In a real
deployment you'd update the config (and redeploy) afterwards so a restart doesn't go back to the old
database. The demo leaves it so the replication story stays visible.

## Reset / cleanup

```
docker compose -f docker-compose.demo.yml --profile kafka --profile inventory --profile migration down -v
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
| `CREATE SUBSCRIPTION` fails or `prepare` returns 409 | `db-inventory` isn't running, or the inventory service isn't the standalone one (the admin API only exists when inbound is HTTP). Do Steps 1-5 first |
| Cutover prints `Databases did not converge` | Replication isn't running. Re-run the replicate script; the freeze lifts itself after 30s and nothing is lost |
| Gateway shows all instances DOWN | Check `docker compose -f docker-compose.demo.yml logs gateway`; recreate it with `up -d --force-recreate gateway` |
