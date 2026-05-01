> 🚧 Project in active development.
>
> Core backend functionality is complete (authentication, job management, leveling calculations).
Currently extending the system with new measurement types and export features.

# GeoNexus

GeoNexus is a backend system for processing leveling survey sequences and generating corrected measurement reports.

It enables calculation of misclosure, tolerance verification, and automatic adjustment of height differences for engineering and construction workflows.

---

## Key Highlights

- Domain logic isolated from Spring — pure Java module, no framework coupling
- JWT authentication with stateless Spring Security filter chain
- Integration tests with real PostgreSQL (Testcontainers), coverage ≥80% enforced by JaCoCo
- JSONB for immutable measurement snapshots — no schema changes for new measurement types, optimized for flexibility over relational querying
- Strategy pattern for calculation engine — new measurement types added via new implementations without modifying existing logic

---

## Modules

**`surveying-math`** —  pure Java, zero Spring dependency. Leveling engines, tolerance checks, misclosure calculations. Fully unit-testable in isolation. Can be reused independently as a calculation engine.
**`surveying-app`** — Spring Boot application. REST API, persistence, security, file parsing, orchestration. Depends on `surveying-math` for calculations.

---

## Request Flow

```
HTTP Request → Controller → Domain Service → surveying-math → Repository → Database
```

**Leveling file processing:**

```
File → Parser → Validation → LevelingStrategy → LevelingEngine → Persist (JSONB) → Response
```

Parser auto-detects format (CSV / TXT) by filename. New formats registered as `@Component` — no changes to existing code.

---

## Key Design Decisions

**Domain isolation** — domain model has no JPA/Spring annotations. `surveying-math` is a standalone library. Business logic tested without loading Spring context.

**Extensible calculation engine** — `LevelingStrategy` selects the correct `LevelingEngine` at runtime. Adding a new measurement type = implementing one interface.

**JSONB for measurement snapshots** — immutable station results stored as JSONB. Flexible schema, backward compatible, no per-type migrations.

**Unidirectional relationships** — User holds `company_id`, no bidirectional collections. Avoids N+1 without explicit fetch tuning.

**Clock injection** — `Clock` injected as a Spring bean, no `Instant.now()` in business logic. Tests override it for deterministic timestamp assertions.

**Centralized exception handling** — `GlobalExceptionHandler` maps all domain and security exceptions (`401`, `403`) to HTTP status codes. Every error logged with a unique UUID.

---

## Security

- Stateless JWT (`JwtFilter` → validate token → set `SecurityContext`)
- Role-based access: `SUPER_ADMIN` · `ADMIN` · `SURVEYOR`
- Custom annotations: `@IsSuperAdmin`, `@IsAdmin`, `@IsAdminOrSurveyor`
- Business rule enforced: company must always have at least one active admin

---

## Testing & CI

- **Unit tests** — `surveying-math` in pure Java, no Spring context
- **Integration tests** — Testcontainers (real PostgreSQL), SQL fixtures via `@Sql`
- **Controller tests** — `@WebMvcTest` with mocked services
- **Coverage** — minimum 80% enforced by JaCoCo; excludes DTOs, entities, config, generated code
- **CI** — GitHub Actions on every push and PR to `master`: build, test, `jacocoTestCoverageVerification`; test and coverage reports uploaded as artifacts

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL + JSONB |
| Migrations | Flyway |
| Security | Spring Security + JWT |
| Mapping | MapStruct |
| Boilerplate | Lombok |
| Testing | JUnit 5, Mockito, AssertJ, Testcontainers |
| CI | GitHub Actions |
| API Docs | OpenAPI / Swagger UI |
| Build | Gradle (Kotlin DSL) |
| Containerization | Docker Compose |

---

## Domain

Leveling determines precise height differences between ground points — a critical process in construction and infrastructure projects.

Measurement errors accumulate across stations (misclosure), requiring validation against tolerance limits and adjustment of results to ensure engineering accuracy.

Supported modes:

- **ONE_WAY** — single-direction, one backsight + one foresight per station

- **ONE_WAY_DOUBLE** — double reading per station, enables per-station error detection

Both modes calculate misclosure and verify against tolerance.

Results are returned as structured JSON responses, with planned support for PDF report export.

---

## Running Locally

Requirements: Docker, Java 21

```bash
docker compose up -d
./gradlew :surveying-app:bootRun
./gradlew test
./gradlew jacocoTestReport   # coverage → build/jacocoHtml/
```

Flyway runs migrations automatically on startup — schema and seed data included. The app starts with 3 companies, 9 users and 5 jobs ready to use.

---

## Try it out

Bruno collection available in `/bruno` — import folder into Bruno for ready-to-use requests.

Full API reference and interactive testing available via Swagger UI: `http://localhost:8080/swagger-ui.html`

Sample leveling files are in [`/sample-files`](./sample-files):
- `leveling_one_way.csv` — 5 stations, misclosure 0.001m, passes tolerance
- `leveling_one_way_double.csv` — 4 stations with double readings, passes tolerance
- `leveling_one_way_tolerance_exceeded.csv` — misclosure 0.036m, exceeds allowed 0.022m

The API returns full calculation results including misclosure, tolerance validation, and adjusted station heights.

### 1. Authenticate

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "jan.kowalski@geosurvey.pl", "password": "GeoAdmin1!"}'
```

Copy token from response and replace `<JWT>` in subsequent requests.

### 2. Check existing jobs

```bash
curl "http://localhost:8080/api/v1/jobs?jobIdentifier=JOB-2024-001" \
  -H "Authorization: Bearer <JWT>"
```

### 3. Upload a leveling file

```bash
curl -X POST http://localhost:8080/api/v1/jobs/leveling \
  -H "Authorization: Bearer <JWT>" \
  -F "file=@sample-files/leveling_one_way.csv" \
  -F "jobIdentifier=JOB-2024-001" \
  -F "startH=100.000" \
  -F "endH=101.250" \
  -F "type=ONE_WAY" \
  -F "observationTime=2024-06-15T10:00:00Z"
```

Response includes full calculation result: misclosure, allowedMisclosure, toleranceMet, adjusted heights per station.

### 4. Try tolerance exceeded

Same request with `leveling_one_way_tolerance_exceeded.csv` and `endH=100.500`. Response returns `"toleranceMet": false`.

### 5. Get paginated reports

```bash
curl "http://localhost:8080/api/v1/jobs/leveling?jobIdentifier=JOB-2024-001&page=0&size=10" \
  -H "Authorization: Bearer <JWT>"
```

### 6. Get companies
```bash
# Example of an endpoint accessible only to SUPER_ADMIN role
curl "http://localhost:8080/api/v1/companies" \
-H "Authorization: Bearer <JWT>"
```

### Seed credentials

| Role        | Email | Password |
|-------------|---|---|
| SUPER_ADMIN | super_admin@sp.sp | Super_admin! |
| ADMIN       | jan.kowalski@geosurvey.pl | GeoAdmin1! |
| ADMIN       | katarzyna.lis@geodeta.pl | GeoAdmin1! |
| SURVEYOR    | anna.nowak@geosurvey.pl | GeoSurvey1! |
| SURVEYOR    | monika.krol@geodeta.pl | GeoSurvey1! |

---

## In Progress

- PDF report export