---
name: database
description: Referencia de base de datos — Flyway, estrategia de IDs, soft delete con índices parciales, enums PostgreSQL, tabla stored_files, patrones de query, convenciones de columnas. Activar al crear o modificar entidades JPA, escribir queries custom, mapear enums de PostgreSQL, trabajar con archivos, migraciones o responder preguntas sobre el schema.
---

# Database

## Stack

PostgreSQL 18-alpine (Docker dev). Schema gestionado por **Flyway** — migraciones en `src/main/resources/db/migration/`. `ddl-auto: none` (dev) y `validate` (prod). Flyway corre automáticamente al arrancar la aplicación.

```bash
# Conectar a la BD
docker exec masters-db psql -U root -d postgres

# Ver historial de migraciones aplicadas
docker exec masters-db psql -U root -d postgres -c "SELECT * FROM flyway_schema_history;"
```

## Flyway

Convenciones de naming para archivos de migración:

| Tipo | Formato | Ejemplo |
|---|---|---|
| Versioned (irreversible) | `V{n}__{descripcion}.sql` | `V2__add_column_x.sql` |
| Repeatable (idempotente) | `R__{descripcion}.sql` | `R__refresh_view.sql` |

- Cada archivo se ejecuta **una sola vez** (versioned) o **cuando cambia su checksum** (repeatable).
- Nunca modificar un archivo de migración ya aplicado — crear uno nuevo.
- `baseline-on-migrate: true` y `baseline-version: 1` permiten que bases existentes (sin `flyway_schema_history`) convivan. Para instalaciones frescas, borra `postgres_data/` y relanza el compose.
- Los ENUMs de PostgreSQL deben crearse **antes** de las tablas que los usan (orden garantizado en un único archivo de migración).

### Para agregar una migración nueva

```
src/main/resources/db/migration/V{next}__descripcion_corta.sql
```

Usar el siguiente número disponible. No tocar `V1__initial_schema.sql` ni ninguna migración ya aplicada.

## Estrategia de IDs

| Tipo de PK | Tablas | Generación en Java |
|---|---|---|
| `UUID` (v7) | `users`, `teachers`, `students`, `courses`, `enrollments`, `grades`, `payments`, `vouchers`, `stored_files` | `@UuidGenerator(style = Style.VERSION_7)` |
| `INTEGER` IDENTITY | `programs`, `semesters`, `states` | `@GeneratedValue(strategy = IDENTITY)` |
| `BIGINT` IDENTITY | `audit_logs`, `notifications`, `assignments` | `@GeneratedValue(strategy = IDENTITY)` |

`assignments` usa BIGINT IDENTITY como PK surrogate + índice único parcial `(id_course, id_teacher, id_semester) WHERE deleted_at IS NULL` para soportar reasignación después de soft delete y repetir el curso/docente en otro semestre.

## Enums de PostgreSQL

Los enums de PG se declaran en inglés mayúsculo. El label en español se maneja en la capa Java. Mapeo en la entidad:

```java
@Enumerated(EnumType.STRING)
@JdbcTypeCode(SqlTypes.NAMED_ENUM)
@Column(name = "role", nullable = false, columnDefinition = "user_role")
private UserRole role;
```

`SqlTypes.NAMED_ENUM` (Hibernate 7) indica a Hibernate que use el tipo nombrado de PostgreSQL. Los valores Java del enum deben coincidir exactamente con los valores del ENUM de PG.

| PG type | Valores Java | Labels español |
|---|---|---|
| `user_role` | `ADMIN`, `TEACHER`, `STUDENT`, `COORDINATOR` | Administrador, Docente, Estudiante, Coordinador |
| `teacher_category` | `PRINCIPAL`, `ASSOCIATE`, `AUXILIARY` | Principal, Asociado, Auxiliar |
| `teacher_type` | `INTERNAL`, `EXTERNAL` | Interno, Externo |
| `academic_degree` | `MASTER`, `DOCTOR` | Magíster, Doctor |
| `course_type` | `REGULAR`, `THESIS`, `TOPICS` | Regular, Tesis, Tópicos |
| `student_status` | `REGULAR`, `REACTUALIZATION` | Regular, Reactualización |
| `notification_type` | `VOUCHER_UPLOADED`, `VOUCHER_VALIDATED`, `VOUCHER_OBSERVED`, `VOUCHER_REJECTED`, `GRADE_REGISTERED`, `GRADE_MODIFIED`, `ENROLLMENT_UPDATED` | (ver NotificationType.java) |

## Soft delete con índices parciales

`@SQLRestriction("deleted_at IS NULL")` filtra automáticamente. Para unicidad post-soft-delete, el schema usa índices únicos parciales:

```sql
-- Ejemplo: asegurar que solo hay un assignment activo por (curso, docente, semestre)
CREATE UNIQUE INDEX uq_assignments_active
    ON assignments (id_course, id_teacher, id_semester)
    WHERE deleted_at IS NULL;
```

JPA no declara estos índices — son invariantes a nivel BD. Para pre-validar en Java:

```java
if (repo.existsByCourse_IdAndTeacher_IdAndSemester_Id(courseId, teacherId, semesterId)) {
    throw new BusinessException("Ya existe una asignación activa para este curso, docente y semestre");
}
```

Si hay race condition, `DataIntegrityViolationException` → el `GlobalExceptionHandler` la convierte en 409.

## Tabla stored_files

Patrón estándar para gestión de archivos sin exponer URLs de GCS:

```sql
CREATE TABLE stored_files (
    id              UUID PRIMARY KEY,
    original_name   VARCHAR(255) NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    size_bytes      BIGINT NOT NULL,
    object_key      VARCHAR(500) NOT NULL,  -- path en GCS, nunca exponer al cliente
    id_uploaded_by  UUID NOT NULL REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

- `stored_files.id` se genera en Java con `@UuidGenerator(style = Style.VERSION_7)`, no con `DEFAULT gen_random_uuid()`.
- Nunca almacenar la URL firmada en la BD — se genera on-demand en `GcsStorageService.signedDownloadUrl()`.
- Convención de `object_key`: `files/{año}/{uuid}.{ext}`, e.g. `files/2026/0192f8c1-....pdf`.
- Las tablas de dominio referencian `stored_files.id`; no guardan URLs ni paths propios. Convenciones actuales: `students.id_reactualization_file`, `courses.id_syllabus_file`, `enrollments.id_resolution_file`, `vouchers.id_file`.
- En respuestas de dominio, devolver metadata resumida del archivo. Para descarga, el cliente debe llamar `GET /api/v1/files/{id}` y usar el `downloadUrl` temporal.

## Columnas estándar

| Columna | Tipo PG | Uso |
|---|---|---|
| `id` | `UUID`, `INTEGER` o `BIGINT` | PK |
| `created_at` | `TIMESTAMPTZ NOT NULL DEFAULT NOW()` | Auditoría de creación |
| `updated_at` | `TIMESTAMPTZ NOT NULL DEFAULT NOW()` | Auditoría de modificación |
| `deleted_at` | `TIMESTAMPTZ` (nullable) | Soft delete — NULL = activo |

En tablas con auditoría completa, `created_at`, `updated_at` y `deleted_at` deben ser las últimas columnas físicas, en ese orden. Si una migración agrega columnas de negocio a una tabla existente y el orden físico importa, no basta con `ALTER TABLE ... ADD COLUMN` porque PostgreSQL las agrega al final; reconstruir la tabla en una migración nueva, copiar datos, renombrar y recrear constraints/índices.

## Patrones de query

**Relación simple** — `@SQLRestriction` aplica automáticamente:
```java
List<Enrollment> findByCourse_IdOrderByEnrollmentDateAsc(UUID courseId);
```

**JPQL con join** — no agregar `deleted_at IS NULL` manualmente:
```java
@Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.course.id = :courseId")
Optional<Enrollment> findActiveByStudentAndCourse(@Param("studentId") UUID s, @Param("courseId") UUID c);
```

**Verificar existencia con FK compuesta**:
```java
boolean existsByCourse_IdAndTeacher_IdAndSemester_Id(UUID courseId, UUID teacherId, Integer semesterId);
```

**Obtener referencia para relaciones** — en servicios del proyecto, `getReference(...)` normalmente valida existencia con `findById` antes de asignar relaciones:
```java
Course course = courseService.getReference(courseId);
assignment.setCourse(course);
```

Usar `repository.getReferenceById(...)` solo en código interno donde se acepte una referencia lazy sin validar existencia inmediatamente.

## Anti-patrones

- `@Enumerated(EnumType.ORDINAL)` — frágil ante reordenamientos del enum.
- Almacenar URLs firmadas de GCS en la BD — expiran y contaminan columnas. Guardar `stored_files.id`.
- Hard-delete en tablas con `deleted_at`.
- Agregar `WHERE deleted_at IS NULL` manualmente en JPQL — `@SQLRestriction` ya lo hace.
- `@UniqueConstraint` estándar en tablas con soft delete — usar índice parcial en SQL.
