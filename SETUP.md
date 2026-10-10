# Setup — Telecom Order Provisioning

Five Spring Boot services, one database each, one Angular UI.

> Most people should just run `bash ../telecom-ops-automation/scripts/start-all.sh`
> from the other repo. It brings up Postgres, all five services and both UIs.
> This page is for when you want to do it by hand.

---

## 1. Prerequisites

| Tool | Version | Check |
|---|---|---|
| Java | 17 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node | 20+ | `node --version` |
| PostgreSQL | 14+ | `psql --version` |

```bash
brew install openjdk@17 maven node postgresql@18
```

---

## 2. Start PostgreSQL and create five databases

```bash
export PATH=/Library/PostgreSQL/18/bin:$PATH

pg_ctl -D ~/.local/share/telecom-pg -l ~/.local/share/telecom-pg.log start
pg_isready -h localhost -p 5433        # must print "accepting connections"

for db in customer order inventory provisioning notification; do
  psql -h localhost -p 5433 -U postgres -c "CREATE DATABASE telecom_$db;"
done
```

**Note the port: 5433, not the 5432 default.** Every service reads `POSTGRES_PORT`.

---

## 3. Load schema and sample data

```bash
cd database
for db in customer order inventory provisioning notification; do
  for f in 01_create_tables.sql 02_indexes.sql 03_sample_data.sql; do
    psql -h localhost -p 5433 -U postgres -d "telecom_$db" -f "$db/$f"
  done
done
```

All the SQL is `IF NOT EXISTS`, so re-running is safe. The `order` database
includes the product catalogue table — that is what feeds the plan picker in
the UI.

---

## 4. Build the five services

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"

for svc in customer order inventory provisioning notification; do
  (cd "$svc-service" && mvn -q package -DskipTests)
done
```

Expect five jars under `*/target/`. Skip this if you only intend to use
`mvn spring-boot:run`.

---

## 5. Run the five services

**All five must be started from the repository root.** This is not a
preference — see the warning below.

```bash
cd /path/to/telecom-order-provisioning

export POSTGRES_HOST=localhost POSTGRES_PORT=5433
export POSTGRES_USER=postgres POSTGRES_PASSWORD=postgres
export LOG_PATH=$PWD/logs          # absolute, and the same for all five

nohup java -jar customer-service/target/customer-service-0.0.1-SNAPSHOT.jar     > /tmp/customer-service.log 2>&1 &
nohup java -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar           > /tmp/order-service.log 2>&1 &
nohup java -jar inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar   > /tmp/inventory-service.log 2>&1 &
nohup java -jar provisioning-service/target/provisioning-service-0.0.1-SNAPSHOT.jar > /tmp/provisioning-service.log 2>&1 &
nohup java -jar notification-service/target/notification-service-0.0.1-SNAPSHOT.jar > /tmp/notification-service.log 2>&1 &
```

Wait ~45s, then verify:

```bash
for p in 8081 8082 8083 8084 8085; do curl -s http://localhost:$p/actuator/health; echo; done
```

| Port | Service |
|---|---|
| 8081 | customer-service |
| 8082 | order-service |
| 8083 | inventory-service |
| 8084 | provisioning-service |
| 8085 | notification-service |

Swagger UI for each: `http://localhost:8082/swagger-ui/index.html`

---

## ⚠️ Why the working directory matters

`logback-spring.xml` resolves its output as `${LOG_PATH:-logs}/<service>`.
With no `LOG_PATH` set, that is a **relative** path, so a service started
from inside `order-service/` writes to:

```
order-service/logs/order-service/app.log     ← wrong
logs/order-service/app.log                   ← what everything else reads
```

Nothing errors. The service just logs to a place nobody looks, and
cross-service correlation tracing silently breaks — you see an order in
customer-service logs with no matching line in order-service logs.

Either always start from the repo root, or set an absolute `LOG_PATH`. The
`start-all.sh` script does this for you.

---

## 6. Run the order UI

```bash
cd frontend/telecom-order-ui
npm install
npm start          # http://localhost:4200
```

First start takes ~40s. It calls the services directly on :8081–:8085, and each
service allows CORS from `http://localhost:4200`.

---

## 7. Optional — chaos mode

Off by default. Turns it on to run incident scenarios 15, 16 and 21, which
reproduce HTTP 502/503 failures by injecting faults.

```bash
CHAOS_ENABLED=true java -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar
```

```bash
# arm a fault
curl -X POST "http://localhost:8081/api/chaos/fault?path=/api/customers/1/validate&status=500&seconds=30"
# clear everything (do this when finished)
curl -X DELETE "http://localhost:8081/api/chaos/fault?path=*"
```

---

## Troubleshooting

| Symptom | Cause |
|---|---|
| Service exits at boot, `Unable to determine Dialect` | Postgres is not running, or `POSTGRES_PORT` is wrong |
| `orders` page empty, 8082 fine | UI not rebuilt since the API changed — restart `npm start` |
| Correlated traces miss one service | Service logging to an orphaned directory; see the warning above |
| `429` while clicking around | Rate limit is 120 req/min per client. Wait 60s or raise `RATE_LIMIT_MAX_REQUESTS` |
| Port already in use | `lsof -ti:8082 \| xargs kill -9` |
| UI shows "something went wrong on our side" | A dependency is down — check `/actuator/health` on each service |

Reset everything:

```bash
bash ../telecom-ops-automation/scripts/stop-all.sh
```