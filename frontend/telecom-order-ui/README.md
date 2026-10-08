# telecom-order-ui (Phase 1)

Angular frontend for the Telecom Order Provisioning System.

Phase 1 ships a static foundation shell: service overview dashboard, backend
URL constants (`src/app/core/api-endpoints.ts`), an `X-Correlation-ID`
HTTP interceptor, HttpClient wiring, and a dev proxy (`proxy.conf.json`).
Live screens and backend calls arrive in Phase 9.

## Run

```bash
npm install
npm start
# open http://localhost:4200
```

With proxy (from Phase 9 onwards):

```bash
npx ng serve --proxy-config proxy.conf.json
```

## Build

```bash
npm run build
```

## Backend links (local)

| Service | Ping | Health | Swagger |
|---|---|---|---|
| customer (8081) | http://localhost:8081/api/system/ping | http://localhost:8081/actuator/health | http://localhost:8081/swagger-ui/index.html |
| order (8082) | http://localhost:8082/api/system/ping | http://localhost:8082/actuator/health | http://localhost:8082/swagger-ui/index.html |
| inventory (8083) | http://localhost:8083/api/system/ping | http://localhost:8083/actuator/health | http://localhost:8083/swagger-ui/index.html |
| provisioning (8084) | http://localhost:8084/api/system/ping | http://localhost:8084/actuator/health | http://localhost:8084/swagger-ui/index.html |
| notification (8085) | http://localhost:8085/api/system/ping | http://localhost:8085/actuator/health | http://localhost:8085/swagger-ui/index.html |
