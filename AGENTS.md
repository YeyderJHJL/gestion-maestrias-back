# UNSA Masters Management — Backend

REST API for the Graduate Office of the Universidad Nacional de San Agustín (UNSA).
Covers academic management of the Master's in Computer Science: users, teachers, students,
courses, assignments, enrollments, grades, pensions, payments, vouchers, files, and notifications.
Out of scope: undergraduate degrees and titles.

## Tech stack

| Component | Version |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.6 |
| Spring Framework | 7.0.7 |
| Hibernate / Spring Data JPA | 7.x |
| Spring Security (OAuth2 Resource Server) | 7.x |
| PostgreSQL | 18-alpine (Docker) |
| Flyway | 11.x (managed by Spring Boot) |
| Gradle | 9.4.1 |
| SpringDoc OpenAPI | 3.0.2 |
| Spring Cloud GCP | 8.0.3 |

**No Lombok** — manual getters/setters. DTOs as `record`. No MapStruct — manual mapping in services.

## Commands

```bash
docker compose -f compose.dev.yml up --build   # start dev environment (API + PostgreSQL + fake GCS + frontend test page)
docker compose -f compose.dev.yml logs -f api  # tail API logs
docker exec masters-db psql -U root -d postgres # connect to DB
```

Tests and builds run **inside Docker** — there is no local Gradle wrapper JAR in the repo.

## Critical conventions

1. **UUID v7** for business entities → `@UuidGenerator(style = Style.VERSION_7)`. Never `GenerationType.UUID` (emits v4, fragments indexes).
2. **Soft delete** via `@SQLDelete` + `@SQLRestriction("deleted_at IS NULL")` on every entity with `deleted_at`. Never set `deletedAt` manually.
3. **Timestamps in UTC** → `Instant` for audit timestamps, `LocalDate` for calendar dates.
4. **100% Google OAuth2** — no passwords in the DB. `users.google_sub` = `sub` claim from the Google JWT. `AppJwtAuthenticationConverter` resolves the User entity and sets `AppUserPrincipal` in the `SecurityContextHolder`.
5. **Responses to client in Spanish** — enums serialize with `@JsonValue` to their Spanish label. Error messages in Spanish.
6. **Frozen schema** — entities must match the DB exactly. Never rename columns without a Flyway migration.
7. **`spring.jpa.open-in-view: false`** — never change to `true`.
8. **`ddl-auto: none`** — schema is owned by Flyway migrations in `src/main/resources/db/migration/`.
9. **Default role = ADMIN** — every endpoint is ADMIN-only unless annotated with `@Authorize` or `@Public`. See `skills/roles/SKILL.md`.
10. **Personal data lives in `users`** — `first_name`, `last_name`, `dni` are on the `users` table, not on `teachers` or `students`.
11. **`SecurityHelper.currentUserId()`** — the canonical way to get the authenticated user's UUID inside controllers and services. Never inject `Authentication` directly.

## Package structure (package by feature)

```
src/main/java/com/claudecoders/masters/
├── MastersApplication.java
├── shared/
│   ├── audit/          BaseEntity, CreatedEntity
│   ├── config/         OpenApiConfig, SecurityConfig (dev/test), ProdSecurityConfig, WebConfig
│   ├── exception/      ApiError, ApiResponse, BusinessException,
│   │                   GlobalExceptionHandler, ResourceNotFoundException
│   ├── security/       @Authorize, @Public, AppJwtAuthenticationConverter,
│   │                   AppUserPrincipal, RolesEnforcementAspect,
│   │                   RolesOperationCustomizer, SecurityHelper
│   ├── seed/           DatabaseSeeder
│   ├── storage/        GcsStorageService
│   └── enums/          LabeledEnum
├── assignment/
├── auditlog/
├── course/
├── enrollment/
├── file/               StoredFile — stored_files table
├── grade/
├── notification/
├── payment/
├── pension/
├── program/
├── promotion/
├── state/
├── student/
├── teacher/
├── user/
└── voucher/
```

## Available skills

- **`skills/architecture/SKILL.md`** — entities, repositories, services, controllers, DTOs, enums, BaseEntity, UUID v7, soft delete patterns.
- **`skills/database/SKILL.md`** — Flyway migrations, ID strategy, soft delete with partial indexes, PostgreSQL enums, `stored_files` table, query patterns.
- **`skills/error-handling/SKILL.md`** — `ApiResponse<T>` / `ApiError` shapes, exception hierarchy, `GlobalExceptionHandler`, HTTP codes, response body examples.
- **`skills/roles/SKILL.md`** — `@Authorize` / `@Public` system, Swagger visualization, `RolesEnforcementAspect`, available roles.
- **`skills/security/SKILL.md`** — OAuth2 Resource Server flow, `AppJwtAuthenticationConverter`, `AppUserPrincipal`, `SecurityHelper`, dev vs prod profiles, CORS, first-login google_sub linking.

## Adding a new Flyway migration

Create a new file — never modify an existing one:

```
src/main/resources/db/migration/V2__your_description.sql
```

Flyway runs automatically on startup and applies pending migrations.

## Definition of Done

A task is complete when:
1. `./gradlew test` passes (run inside Docker with compose.dev.yml running).
2. New endpoints have `@Operation` + `@Tag`, return `ApiResponse<T>` (or 204), and validate input with `@Valid`.
3. New entities extend `BaseEntity` or `CreatedEntity`, use the correct PK type (UUID v7 for business, IDENTITY for catalogs), and have `@SQLDelete` + `@SQLRestriction` if they have `deleted_at`.
4. Services use constructor injection and `@Transactional`.
5. Error messages to the user in Spanish; technical logs in English.
6. Any schema change has a corresponding Flyway migration (never modify existing migration files).
7. Endpoints that need the authenticated user call `SecurityHelper.currentUserId()` — never accept a `uploaderId` or similar param that bypasses auth.
