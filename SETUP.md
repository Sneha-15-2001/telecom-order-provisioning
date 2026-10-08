# Setup Guide — From Zero to Running

Follow in order. Nothing is skipped; every step can be copy-pasted.

## 1. Install what you need

| Tool | Check with | Get it |
|---|---|---|
| Java 17 | `java -version` → `17.x` | `brew install openjdk@17` (macOS) |
| Maven 3.9+ | `mvn -version` | `brew install maven` |
| Node 20+ | `node --version` | `brew install node` |
| PostgreSQL 14+ | `psql --version` | `brew install postgresql` or official installer |

## 2. Start PostgreSQL and create the five databases

```bash
# start (adjust to YOUR postgres installation)
pg_ctl -D <your-data-dir> -l pg.log start
# OR, on the original machine: /Library/PostgreSQL/18/bin/pg_ctl -D ~/.local/share/telecom-pg -l ~/.local/share/telecom-pg.log start

# create databases (change user/password flags to match your setup)
psql -h localhost -U postgres -c "CREATE DATABASE telecom_customer;"
psql -h localhost -U postgres -c "CREATE DATABASE telecom_order;"
psql -h localhost -U postgres -c "CREATE DATABASE telecom_inventory;"
psql -h localhost -U postgres -c "CREATE DATABASE telecom_provisioning;"
psql -h localhost -U postgres -c "CREATE DATABASE telecom_notification;"
```

Tell the apps how to connect (defaults: host `localhost`, port `5432`,
user `postgres`). Only export what differs from the defaults:

```bash
export POSTGRES_HOST=localhost
export POSTGRES_PORT=5433        # ← only if YOUR postgres runs elsewhere
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=postgres
```

## 3. Run the five backends (one terminal each, from the repo root)

```bash
cd telecom-order-provisioning
export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo /opt/homebrew/opt/openjdk@17)

cd customer-service && mvn spring-boot:run        # → :8081
cd order-service && mvn spring-boot:run           # → :8082
cd inventory-service && mvn spring-boot:run       # → :8083
cd provisioning-service && mvn spring-boot:run   # → :8084
cd notification-service && mvn spring-boot:run    # → :8085
```

Faster alternative (prebuilt jars, same env vars exported):

```bash
java -jar customer-service/target/customer-service-0.0.1-SNAPSHOT.jar
# … repeat per service
```

Wait for `Started *Application` in each terminal.

## 4. Run the website

```bash
cd frontend/telecom-order-ui
npm install   # first time only
npm start     # → http://localhost:4200
```

## 5. Check everything works

```bash
# all backends UP with database connected?
for p in 8081 8082 8083 8084 8085; do curl -s http://localhost:$p/actuator/health | head -c 16; echo " <- $p"; done

# website loads? open http://localhost:4200 (dashboard shows live numbers)

# interactive API docs (open in browser):
# http://localhost:8081/swagger-ui/index.html  (also 8082–8085)
```

## 6. Build everything (jars + UI bundle)

```bash
# backends: compile + test + package each service
export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo /opt/homebrew/opt/openjdk@17)
for s in customer-service order-service inventory-service provisioning-service notification-service; do
  mvn -q clean package -f $s/pom.xml && echo "$s built: $s/target/*.jar"
done

# website: production bundle
cd frontend/telecom-order-ui && npm run build   # → dist/telecom-order-ui
```

## 7. Run the tests

```bash
# needs the env vars from step 2 + Java 17
for s in customer-service order-service inventory-service provisioning-service notification-service; do
  mvn test -f $s/pom.xml 2>&1 | grep -E "Tests run: [0-9]+, F|BUILD" | tail -1
done
cd frontend/telecom-order-ui && CI=true npx ng test
```

## 8. Try an incident scenario (optional)

```bash
bash incident-scenarios/scenario-01-stuck-payment/setup.sh
# prints IDs + the curl commands that show the broken state
```

## If something breaks

| Symptom | Fix |
|---|---|
| `connection refused` on a port | that service isn't running — check its terminal for errors |
| DB `password authentication failed` | your user/password differ — set the `POSTGRES_*` vars correctly |
| `relation does not exist` | tables auto-create on boot (`ddl-auto: update`); if a DB is empty, restart that service once |
| Port already in use | `lsof -i :8082` then `kill <pid>`, or change `*_SERVICE_PORT` env |
| UI shows "Backend unreachable" | backends must run before/while the UI runs; refresh after starting them |
