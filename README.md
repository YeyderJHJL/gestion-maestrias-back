# Sistema de Gestión Académica de Maestrías — Backend

API REST para la Oficina de Posgrado de la **Universidad Nacional de San Agustín (UNSA)**.  
Gestiona usuarios, docentes, estudiantes, cursos, asignaciones, matrículas, notas, pensiones, pagos, vouchers, archivos y notificaciones del programa de Maestría en Informática.

---

## Stack tecnológico

| Componente | Versión |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.6 |
| Spring Framework | 7.0.7 |
| Hibernate / Spring Data JPA | 7.x |
| Spring Security (OAuth2 Resource Server) | 7.x |
| PostgreSQL | 18-alpine |
| Flyway | 11.x (gestionado por Spring Boot) |
| Gradle | 9.4.1 (wrapper incluido en Docker) |
| SpringDoc OpenAPI | 3.0.2 |
| Spring Cloud GCP (Storage) | 8.0.3 |

> Sin Lombok. DTOs como `record`. Mapping manual en servicios. Sin MapStruct.

---

## Arquitectura

### Package by feature

Cada dominio de negocio tiene su propio paquete con entidad, repositorio, servicio, controlador y DTOs. Solo concerns transversales están en `shared/`.

```
src/main/java/com/claudecoders/masters/
├── MastersApplication.java
├── shared/
│   ├── audit/          BaseEntity, CreatedEntity
│   ├── config/         OpenApiConfig, SecurityConfig (dev), ProdSecurityConfig, WebConfig
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

### Respuestas HTTP

- **2xx** → `ApiResponse<T>` con `success`, `data`, `message`
- **4xx / 5xx** → `ApiError` con `status`, `error`, `message`, `path`, `timestamp`
- Prefijo automático `/api/v1` para todos los `@RestController`

### Autenticación (OAuth2 + Google)

El backend actúa como **Resource Server** — no maneja redirecciones de login. El flujo es:

```
Frontend  →  Google Identity Services  →  ID Token (JWT, expira en 1h)
Frontend  →  Authorization: Bearer <token>  →  Backend
                                                ├─ Verifica firma RS256 (clave pública de Google)
                                                ├─ AppJwtAuthenticationConverter busca user en BD
                                                └─ AppUserPrincipal(userId, role) en SecurityContext
```

No hay JWT propio ni JWT secret. Google firma; el backend solo verifica.

### Migraciones de BD (Flyway)

El schema es propiedad de Flyway, no de Hibernate (`ddl-auto: none`). Las migraciones están en:

```
src/main/resources/db/migration/
└── V1__initial_schema.sql   ← schema completo inicial
    V2__...sql               ← siguientes cambios
```

Flyway corre automáticamente al arrancar la aplicación.

---

## Levantar el entorno de desarrollo

### Requisitos previos

- Docker Desktop corriendo
- Archivo `.env` configurado (ver sección Variables de entorno)

### Primer levantamiento

```bash
# 1. Copia el archivo de ejemplo y ajusta los valores
cp .env .env.local    # o edita .env directamente

# 2. Levanta todos los servicios
docker compose -f compose.dev.yml up --build
```

Servicios que se levantan:

| Servicio | URL | Descripción |
|---|---|---|
| `api` (Spring Boot) | http://localhost:8080 | Backend principal |
| `postgres` | localhost:5432 | PostgreSQL 18 |
| `gcs` (fake-gcs) | http://localhost:4443 | Emulador de Google Cloud Storage |
| `frontend` (nginx) | http://localhost:3000 | Página de prueba de login |

### Acceso a la documentación

- **Swagger UI**: http://localhost:8080/docs
- **OpenAPI JSON**: http://localhost:8080/docs/openapi.json

### Comandos útiles

```bash
# Ver logs de la API en tiempo real
docker compose -f compose.dev.yml logs -f api

# Conectar a la base de datos
docker exec masters-db psql -U root -d postgres

# Ver migraciones aplicadas por Flyway
docker exec masters-db psql -U root -d postgres -c "SELECT * FROM flyway_schema_history;"

# Reconstruir imagen de la API (tras cambios en Dockerfile o build.gradle)
docker compose -f compose.dev.yml up --build api

# Reinicio limpio (elimina datos de BD y GCS)
docker compose -f compose.dev.yml down
rm -rf postgres_data/ gcs_data/
docker compose -f compose.dev.yml up --build
```

---

## Variables de entorno (`.env`)

| Variable | Descripción | Valor dev |
|---|---|---|
| `PORT` | Puerto del servidor | `8080` |
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `dev` |
| `GRADLE_CACHE_DIR` | Caché de Gradle local | `C:/Users/tu_usuario/.gradle` |
| `DB_URL` | JDBC URL de PostgreSQL | `jdbc:postgresql://postgres:5432/postgres` |
| `DB_USER` | Usuario de BD | `root` |
| `DB_PASSWORD` | Contraseña de BD | `root` |
| `DB_POOL_MAX` | Máximo de conexiones HikariCP | `12` |
| `DB_POOL_MIN_IDLE` | Mínimo de conexiones ociosas | `4` |
| `GCP_PROJECT_ID` | ID del proyecto en GCP | `masters-dev` (ignorado en dev) |
| `GCS_BUCKET` | Bucket de GCS | `masters-dev` |
| `GOOGLE_OAUTH_CLIENT_ID` | Client ID de Google OAuth | `xxxx.apps.googleusercontent.com` |
| `GOOGLE_OAUTH_AUDIENCE` | Audience del JWT (= CLIENT_ID) | igual que `GOOGLE_OAUTH_CLIENT_ID` |
| `GOOGLE_OAUTH_ISSUER_URI` | Issuer de Google | `https://accounts.google.com` |
| `ADMIN_EMAIL` | Email del usuario admin inicial | `admin@unsa.edu.pe` |
| `CORS_ALLOWED_ORIGINS` | Orígenes CORS permitidos | `http://localhost:3000` |

> `GOOGLE_OAUTH_CLIENT_SECRET` ya no es necesario en el backend (el flujo `oauth2.client` fue eliminado).

---

## Autenticación y prueba de login

### Configurar Google Cloud Console

1. Ir a [Google Cloud Console → APIs & Services → Credentials](https://console.cloud.google.com/apis/credentials)
2. Crear un **OAuth 2.0 Client ID** de tipo "Web application"
3. Agregar en **Authorized JavaScript origins**:
   - `http://localhost:3000`
4. Copiar el **Client ID** al `.env` como `GOOGLE_OAUTH_CLIENT_ID`

### Página de prueba del login

Con el compose levantado, abre **http://localhost:3000**:

1. **Configuración** → ingresa tu Google Client ID
2. **Sign in with Google** → popup de autenticación
3. **Sesión activa** → muestra tu ID Token, nombre y email
4. **Tests de API** → botones para llamar endpoints con el token

La página envía automáticamente `Authorization: Bearer <token>` en cada request.

### Pre-registrar usuarios

Los usuarios deben existir en la BD antes de poder autenticarse. El `DatabaseSeeder` crea el admin inicial al arrancar (si `ADMIN_EMAIL` está configurado):

```yaml
# .env
ADMIN_EMAIL=tu@correo.com
```

En el primer login de ese correo, el `AppJwtAuthenticationConverter` vincula automáticamente el `google_sub` y actualiza el nombre desde Google.

Para crear otros usuarios (docentes, estudiantes): usar la API con un token de admin.

---

## Roles y autorización

| Rol | Acceso |
|---|---|
| `ADMIN` | Acceso completo |
| `TEACHER` | Operaciones propias de docente |
| `STUDENT` | Operaciones propias de estudiante |
| `COORDINATOR` | Operaciones propias de coordinador de programa |

Los endpoints se protegen con:
- `@Authorize(roles = {...})` → acceso para roles indicados
- `@Public` → sin autenticación
- Sin anotación → solo `ADMIN` (por defecto)

En perfil `dev`: seguridad deshabilitada (permitAll) para facilitar el desarrollo. Los roles se validan solo en `prod`.

---

## Base de datos

### Convenciones del schema

| Aspecto | Regla |
|---|---|
| PKs de entidades de negocio | UUID v7 generado en Java por Hibernate |
| PKs de catálogos (`programs`, `states`, etc.) | `INTEGER GENERATED ALWAYS AS IDENTITY` |
| PKs append-only (`audit_logs`, `notifications`, `assignments`) | `BIGINT GENERATED ALWAYS AS IDENTITY` |
| Soft delete | `deleted_at TIMESTAMPTZ NULL` + índice único parcial `WHERE deleted_at IS NULL` |
| Datos personales | `first_name`, `last_name`, `dni` viven en `users`, no en `teachers`/`students` |

### Agregar una migración nueva

```bash
# Crear archivo con versión incremental
# src/main/resources/db/migration/V2__descripcion_del_cambio.sql

ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

**Nunca modificar** `V1__initial_schema.sql` ni ningún archivo de migración ya aplicado.

---

## Estructura de un feature nuevo

```
src/main/java/com/claudecoders/masters/mi_feature/
├── MiEntidad.java                  # @Entity extiende BaseEntity/CreatedEntity
├── MiEntidadRepository.java        # JpaRepository<MiEntidad, UUID>
├── MiEntidadService.java           # @Service @Transactional
├── MiEntidadController.java        # @RestController con @Authorize/@Public
└── dto/
    ├── MiEntidadRequest.java       # record con validaciones Bean Validation
    └── MiEntidadResponse.java      # record
```

**Checklist** antes de marcar una tarea como terminada:
1. `./gradlew test` pasa (ejecutado en Docker)
2. Endpoints tienen `@Operation` + `@Tag`, devuelven `ApiResponse<T>` o `void` (204)
3. Entidades extienden `BaseEntity` o `CreatedEntity` con el tipo de PK correcto
4. Servicios usan inyección por constructor y `@Transactional`
5. Mensajes de error al usuario en español; logs técnicos en inglés
6. Migraciones SQL para todo cambio de schema

---

## Skills de desarrollo

Los archivos en `skills/` documentan los patrones del proyecto. Los agentes de IA los leen automáticamente.

| Skill | Descripción |
|---|---|
| [`skills/architecture/SKILL.md`](skills/architecture/SKILL.md) | Entidades, repositorios, servicios, controladores, DTOs, enums, UUID v7, soft delete |
| [`skills/database/SKILL.md`](skills/database/SKILL.md) | Flyway, IDs, soft delete, enums PG, índices parciales, `stored_files` |
| [`skills/error-handling/SKILL.md`](skills/error-handling/SKILL.md) | `ApiResponse<T>`, `ApiError`, excepciones, `GlobalExceptionHandler` |
| [`skills/roles/SKILL.md`](skills/roles/SKILL.md) | `@Authorize`, `@Public`, `RolesEnforcementAspect`, Swagger |
| [`skills/security/SKILL.md`](skills/security/SKILL.md) | OAuth2, `AppJwtAuthenticationConverter`, `SecurityHelper`, CORS, primer login |
