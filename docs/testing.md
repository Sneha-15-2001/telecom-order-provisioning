# Testing Guide (Phase 11)

## Backend (JUnit 5 + Mockito + Spring slice tests)

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17 PATH="$JAVA_HOME/bin:$PATH"
export POSTGRES_HOST=localhost POSTGRES_PORT=5433 POSTGRES_USER=postgres POSTGRES_PASSWORD=postgres

# one service
mvn test -f customer-service/pom.xml

# all five (94 tests total as of Phase 11)
for s in customer-service order-service inventory-service provisioning-service notification-service; do
  mvn test -f $s/pom.xml 2>&1 | grep -E "Tests run: [0-9]+, F|BUILD" | tail -1
done
```

Conventions: service unit tests use Mockito (`@ExtendWith(MockitoExtension)`,
`ReflectionTestUtils` for generated IDs); repository tests use
`@DataJpaTest + @AutoConfigureTestDatabase(replace = NONE)` against real
PostgreSQL (no embedded DB on the classpath by design); controller tests use
`@WebMvcTest` with mocked services; cross-service REST clients are tested
against a JDK `HttpServer` stub (`IntegrationClientTest`) that also asserts
`X-Correlation-ID` propagation.

## Frontend (Vitest)

```bash
cd frontend/telecom-order-ui
CI=true npx ng test   # 2/2 component specs
npm run build          # production bundle must compile
```

## Manual verification

- Health: `curl localhost:808{1..5}/actuator/health` → all `UP` with PostgreSQL `UP`.
- Swagger: `localhost:808{1..5}/swagger-ui/index.html` (Try it out on any endpoint).
- Paged endpoints take `?page=0&size=5&sort=field,asc` (expanded via `@ParameterObject`).
- Trace a full saga: create order → validate → submit → pay → fulfill; then check
  the reservation (8083), provisioning request (8084) and notification (8085)
  for the same order ID — the same flow the AI investigator will trace in Phase 14.
- Failure drills: `fulfill?failAt=ACTIVATE`, short payments, suspended-customer
  validation, broadband-without-resource activation, `fail-*` recipients.
