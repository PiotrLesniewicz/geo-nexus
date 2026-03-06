# GeoNexus

A backend application for managing geodetic surveying work — starting with leveling calculations,
designed as an extensible platform for further measurement types.

> 🚧 Project in active development. This README reflects the current state and will be updated as the project evolves.

---

## Overview

GeoNexus allows surveying companies to manage field measurement data, perform geodetic calculations,
persist results and generate reports. The initial scope covers leveling surveys (niwelacja),
with the architecture intentionally designed to support additional measurement types in the future.

The project serves as a portfolio piece demonstrating end-to-end Java/Spring Boot development
with a focus on clean architecture, testability and domain-driven design.

---

## Modules

The project is a multi-module Gradle build composed of two modules:

**`surveying-math`** — pure Java library with no Spring dependency. Contains all geodetic calculation logic:
leveling algorithms, tolerance checks, misclosure calculations. Designed to be testable in isolation
and reusable independently of any framework.

**`surveying-app`** — Spring Boot application. Handles persistence, REST API, security, file parsing
and orchestration. Depends on `surveying-math` for calculation logic.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Migrations | Flyway |
| Mapping | MapStruct |
| Boilerplate | Lombok |
| Security | Spring Security (JWT — in progress) |
| Testing | JUnit 5, Mockito, AssertJ, Testcontainers |
| Build | Gradle (Kotlin DSL) |
| Containerization | Docker |

---

## Architecture

The `surveying-app` module follows a layered architecture with a clear separation of concerns:

```
api/
  controller/       REST controllers, GlobalExceptionHandler
  dto/              Request/Response DTOs
  mapper/           MapStruct mappers (domain ↔ DTO)

domain/
  model/            Domain objects (Company, User, UserAuth, Job, ...)
  service/          Application services (AccountManager, LevelingService, ...)
  exception/        Domain exceptions (ResourceNotFoundException, ResourceAlreadyExistsException, ...)

infrastructure/
  database/
    entity/         JPA entities
    repository/     Spring Data repositories
  mapper/           MapStruct mappers (domain ↔ entity)
  parser/           File parsers (Trimble, Leica formats)
  security/         JWT token provider, Spring Security config
  configuration/    Spring beans, Clock, etc.
```

Key design decisions:
- Domain model is framework-agnostic — no JPA annotations in domain objects
- `surveying-math` module has no Spring dependency — pure Java, fully unit-testable
- File parsers return `LevelingObservation` directly — no redundant mapping layer
- Unidirectional `user → company` relation — avoids N+1 on company queries
- `Clock` injected as a Spring bean — enables deterministic time-based tests

---

## Domain

### Company & User accounts

A `Company` is an independent entity. A `User` always belongs to a company (`company_id NOT NULL`).
The first user registered for a company is automatically assigned the `ADMIN` role.
Subsequent users are added by the admin. A company must always have at least one active admin.

### Leveling calculations

Supports two calculation modes:

- **ONE_WAY** — single-direction leveling, supports both absolute (with known start/end heights) and relative mode
- **ONE_WAY_DOUBLE** — double leveling with station error calculation

Calculation flow:
```
File upload → Parser → Validator → LevelingObservation → MathEngine → Result → (user accepts) → Persist + Report
```

Results include: height differences, corrections, adjusted heights, misclosure, allowed misclosure,
tolerance check per station and for the whole sequence.

---

## Data model

```sql
company         -- surveying company
user_account    -- user belonging to a company
user_auth       -- hashed password, must_change flag
job             -- surveying job/project assigned to company and user
leveling_report -- calculation result with stations snapshot (JSONB)
```

Station snapshots are stored as `JSONB` — results are immutable after acceptance,
independent of any future changes to the station model.

---

## Testing approach

- **Unit tests** — service logic tested with Mockito, real `PasswordEncoder` and MapStruct implementations
  where it adds value, mocks only for repositories and `Clock`
- **Integration tests** — `@SpringBootTest` with Testcontainers (real PostgreSQL), no rollback,
  real database writes verified by direct repository queries
- TDD practiced throughout — tests written before implementation

---

## Current status

- [x] surveying-math module (leveling engine, ONE_WAY, ONE_WAY_DOUBLE)
- [x] Database schema + Flyway migrations
- [x] JPA entities + repositories
- [x] Domain model (Company, User, UserAuth, Job, LevelingReport)
- [x] AccountManager — company registration with admin (TDD, integration tests)
- [ ] AccountManager — add user to existing company
- [ ] LevelingService — file parsing, calculation, persistence
- [ ] REST API layer
- [ ] JWT authentication
- [ ] Report generation

---

## Running locally

> Prerequisites: Docker, Java 21

```bash
# Start PostgreSQL
docker-compose up -d

# Run application
./gradlew :surveying-app:bootRun

# Run tests
./gradlew test
```

---

## Future roadmap

- Invitation token system — admin generates a one-time link for self-registration
- Additional measurement types (tachymetry, GPS)
- Password reset / change flow
- Report export (PDF)
- Free tier with automatic cleanup after 21 days of inactivity
