# Quick start — copy and paste

Full detail in [SETUP.md](SETUP.md).

```bash
# 1. tools
brew install openjdk@17 maven node postgresql@18

# 2. database
export PATH=/Library/PostgreSQL/18/bin:$PATH
pg_ctl -D ~/.local/share/telecom-pg -l ~/.local/share/telecom-pg.log start
for db in customer order inventory provisioning notification; do
  psql -h localhost -p 5433 -U postgres -c "CREATE DATABASE telecom_$db;"
done

# 3. schema + sample data
cd database
for db in customer order inventory provisioning notification; do
  for f in 01_create_tables.sql 02_indexes.sql 03_sample_data.sql; do
    psql -h localhost -p 5433 -U postgres -d "telecom_$db" -f "$db/$f"
  done
done
cd ..

# 4. build
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"
for svc in customer order inventory provisioning notification; do
  (cd "$svc-service" && mvn -q package -DskipTests)
done

# 5. run all five FROM THE REPO ROOT (see SETUP.md — this matters)
export POSTGRES_HOST=localhost POSTGRES_PORT=5433 POSTGRES_USER=postgres POSTGRES_PASSWORD=postgres
export LOG_PATH=$PWD/logs
for svc in customer-service order-service inventory-service provisioning-service notification-service; do
  nohup java -jar "$svc/target/$svc-0.0.1-SNAPSHOT.jar" > "/tmp/$svc.log" 2>&1 &
done

# 6. UI
cd frontend/telecom-order-ui && npm install && npm start    # :4200
```

Check it worked:

```bash
for p in 8081 8082 8083 8084 8085; do curl -s http://localhost:$p/actuator/health; echo; done
curl -s http://localhost:4200
```
