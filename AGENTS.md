# UNSA Maestrías Backend

REST API para la Oficina de Posgrado de la Universidad Nacional de San Agustín (UNSA).
Cubre la gestión académica de la Maestría en Informática: usuarios, docentes, estudiantes,
cursos, asignaciones, matrículas, notas, pensiones, pagos, vouchers, archivos y notificaciones.
Fuera de alcance: grados y títulos de pregrado.

## Stack actual

| Componente | Versión |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.6 |
| Spring Framework | 7.0.7 |
| Hibernate / Spring Data JPA | 7.x |
| Spring Security | 7.x |
| PostgreSQL | 18-alpine (Docker) |
| Gradle | 9.4.1 |
| SpringDoc OpenAPI | 3.0.2 |
| Spring Cloud GCP | 8.0.3 |

**Sin Lombok** — getters/setters manuales. DTOs como `record`. Sin MapStruct — mapping manual en servicios.

## Comandos

```bash
docker compose -f compose.dev.yml up --build   # levantar entorno dev
./gradlew test                                   # solo tests
./gradlew build                                  # compilar + tests
```

## Convenciones críticas

1. **UUID v7** en entidades de negocio → `@UuidGenerator(style = Style.VERSION_7)`.
2. **Soft delete** via `@SQLDelete` + `@SQLRestriction("deleted_at IS NULL")` en toda entidad con `deleted_at`. Nunca setear `deletedAt` manualmente.
3. **Timestamps en UTC** → `Instant` para timestamps, `LocalDate` para fechas de calendario.
4. **Autenticación 100% Google OAuth2** — no hay passwords en la BD. `users.google_sub` = claim `sub` del JWT de Google.
5. **Respuestas al cliente en español** — enums serializan con `@JsonValue` a su label en español. Mensajes de error en español.
6. **Schema congelado** — entidades deben coincidir exactamente con la BD. No renombrar columnas sin migrar el SQL.
7. **`spring.jpa.open-in-view: false`** — nunca cambiar a true.
8. **`ddl-auto: none`** — el schema es propiedad del SQL, no de Hibernate.
9. **Roles por defecto ADMIN** — todo endpoint es ADMIN-only a menos que tenga `@Authorize` o `@Public`. Ver `skills/roles/SKILL.md`.

## Estructura de paquetes (package by feature)

```
src/main/java/com/claudecoders/masters/
├── MastersApplication.java
├── shared/
│   ├── audit/          BaseEntity, CreatedEntity
│   ├── config/         OpenApiConfig, SecurityConfig, WebConfig
│   ├── exception/      ApiError, ApiResponse, BusinessException,
│   │                   GlobalExceptionHandler, ResourceNotFoundException
│   ├── security/       @Authorize, @Public, RolesEnforcementAspect,
│   │                   RolesOperationCustomizer
│   ├── seed/           DatabaseSeeder
│   ├── storage/        GcsStorageService
│   └── enums/          LabeledEnum
├── assignment/
├── auditlog/
├── course/
├── enrollment/
├── file/               StoredFile — tabla stored_files
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

## Skills disponibles

- **`skills/architecture/SKILL.md`** — entidades, repositorios, servicios, controladores, DTOs, enums, BaseEntity, UUID v7, soft delete.
- **`skills/database/SKILL.md`** — patrones de BD, IDs, soft delete, enums, índices parciales, tabla `stored_files`.
- **`skills/error-handling/SKILL.md`** — formas de respuesta (`ApiResponse<T>` y `ApiError`), jerarquía de excepciones, `GlobalExceptionHandler`, códigos HTTP y ejemplos de body.
- **`skills/roles/SKILL.md`** — sistema `@Authorize`/`@Public`, visualización en Swagger, `RolesEnforcementAspect`.

## Definition of Done

Una tarea está completa cuando:
1. `./gradlew test` pasa.
2. Nuevos endpoints tienen `@Operation` + `@Tag`, devuelven `ApiResponse<T>` (o 204), y validan entrada con `@Valid`.
3. Nuevas entidades extienden `BaseEntity` o `CreatedEntity`, usan el tipo de PK correcto (UUID v7 para negocio, IDENTITY para catálogos), y tienen `@SQLDelete` + `@SQLRestriction` si tienen `deleted_at`.
4. Servicios usan inyección por constructor y `@Transactional`.
5. Mensajes de error al usuario en español; logs técnicos en inglés.
