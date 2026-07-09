# UNSA Masters Management - Backend

Spring Boot REST API for UNSA master's academic management. Single Gradle project, Java 25, PostgreSQL, Flyway, Google OAuth2 Resource Server, fake GCS in dev.

## High-Value Commands

- Start dev stack: `docker compose -f compose.dev.yml up --build`.
- Dev services: API `http://localhost:8080`, Scalar `http://localhost:8080/api/scalar`, Swagger UI `http://localhost:8080/api/docs`, OpenAPI JSON `http://localhost:8080/api/docs/openapi.json`, test login page `http://localhost:3000`, fake GCS `http://localhost:4443`.
- PostgreSQL maps to host `localhost:5430`; inside Docker use `postgres:5432`.
- Tail API logs: `docker compose -f compose.dev.yml logs -f api`.
- Full project test: `docker compose -f compose.dev.yml exec api ./gradlew test`.
- Focused test: `docker compose -f compose.dev.yml exec api ./gradlew test --tests 'com.claudecoders.masters.SomeTest.someMethod'`.
- Compile-only check matching CI intent: `docker compose -f compose.dev.yml exec api ./gradlew testClasses --no-daemon`.
- DB shell: `docker exec masters-db psql -U root -d postgres`.
- Flyway history: `docker exec masters-db psql -U root -d postgres -c "SELECT * FROM flyway_schema_history;"`.

Run Gradle checks inside the `api` container when possible; the wrapper exists locally, but Docker provides the Java 25/dev-service environment used by this repo.

## Repo Skills

- `opencode.json` registers repo-local `skills/`; load the matching skill before touching architecture, JPA/Flyway, error responses, roles, or security.
- Most useful skills: `architecture`, `database`, `error-handling`, `roles`, `security`.

## Wiring And Boundaries

- Controllers map feature paths like `/users`; `WebConfig` adds `/api/v1` to all `@RestController` routes.
- Package by feature under `src/main/java/com/claudecoders/masters/{domain}`; keep only cross-cutting code in `shared/`.
- Current domain packages include `semester/`; older `promotion/` references are stale.
- No Lombok and no MapStruct. DTOs are Java `record`s; mapping is manual in services.
- Services use constructor injection and Spring transactions. Prefer `@Transactional(readOnly = true)` for reads and `@Transactional` for writes.

## Database Rules

- Schema is owned by Flyway in `src/main/resources/db/migration/`; never edit an applied migration, add `V{next}__description.sql`.
- Keep `spring.jpa.open-in-view: false`; dev `ddl-auto` defaults to `none`, prod uses `validate`. Do not switch to `update` or `create`.
- Business UUID PKs use `@GeneratedValue` plus `@UuidGenerator(style = Style.VERSION_7)`. Never use `GenerationType.UUID`.
- Catalog PKs (`programs`, `semesters`, `states`) use `Integer` IDENTITY; append-only/surrogate PKs (`assignments`, `audit_logs`, `notifications`) use `Long` IDENTITY.
- Entities with `deleted_at` need `@SQLDelete` and `@SQLRestriction("deleted_at IS NULL")`; do not set `deletedAt` manually or duplicate that filter in JPQL.
- Uniqueness with soft delete belongs in SQL partial indexes (`WHERE deleted_at IS NULL`), not plain JPA `@UniqueConstraint`.
- PostgreSQL enums are named types; map with `@Enumerated(EnumType.STRING)` and `@JdbcTypeCode(SqlTypes.NAMED_ENUM)`. Java enum constants must match PG values.
- Enums implement `LabeledEnum` and serialize Spanish labels via `@JsonValue`; new client-facing domain messages should be Spanish, while technical logs stay English.
- Audit timestamps use `Instant`; calendar/business dates use `LocalDate`.

## Security And Roles

- The backend only verifies Google ID tokens. Do not add backend passwords, JWT secrets, or custom JWT issuance.
- Users are pre-registered; first login links `users.google_sub` from the Google `sub` claim by matching email.
- Use `SecurityHelper.currentUserId()` or `currentPrincipal()` in controllers/services; do not inject `Authentication` into controllers or accept client-supplied actor/uploader IDs.
- In `dev` and `test`, the security filter chain is `permitAll()` and processes a JWT only if provided.
- `RolesEnforcementAspect` is active in `dev` and `prod`, disabled in `test`; an unannotated controller method defaults to ADMIN-only. Use `@Public` for truly public routes and `@Authorize` for role-specific access.
- In `prod`, all routes require JWT except health; app roles come from `users.role`, not from Google JWT claims.

## API Shape

- Successful endpoints return `ApiResponse<T>` or intentional `void`/204; errors use `ApiError` through the existing `GlobalExceptionHandler` and `SecurityExceptionResponder`.
- New controllers need class-level `@Tag`, method-level `@Operation`, `@Valid` on request bodies, and `@Authorize` or `@Public` where the default ADMIN-only behavior is not intended.
- Use `ResourceNotFoundException` for 404 and `BusinessException` for 409-style domain conflicts; do not add a second `@RestControllerAdvice`.

## Files And GCS

- `stored_files` stores metadata and `object_key`; domain tables store FKs to `stored_files.id`, never GCS URLs or signed URLs.
- Upload files first through `/api/v1/files...`, then pass the returned UUID in domain requests.
- Domain responses should include stored-file summary metadata. `GET /api/v1/files/{id}` is the endpoint that returns a temporary `downloadUrl`.
- Current syllabus FK is `assignments.id_syllabus_file`; older `courses.id_syllabus_file` references are stale after `V6__move_syllabus_file_to_assignments.sql`.

## Auditing

- Entities that should produce audit logs implement `Auditable`; `AuditHibernateListener` records only fields returned by `auditFields()` and only when a current user is present.
- `audit_logs.id_entity` stores IDs as text so both UUID and numeric PK entities can be audited.

## CI And Branch Flow

- PR flow is enforced: `feature/* -> develop`, `develop -> main`.
- CI validates branch flow, temporary merge with the target branch, and `./gradlew testClasses --no-daemon` on Java 25.
- Before handing off code changes, prefer the full Docker test command above; if it cannot run, state the blocker explicitly.
