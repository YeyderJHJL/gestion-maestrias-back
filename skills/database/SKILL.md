---
name: database
description: Referencia de base de datos — estrategia de IDs, soft delete con índices parciales, enums PostgreSQL, tabla stored_files, patrones de query, convenciones de columnas. Activar al crear o modificar entidades JPA, escribir queries custom, mapear enums de PostgreSQL, trabajar con archivos o responder preguntas sobre el schema.
---

# Database

## Stack

PostgreSQL 18-alpine (Docker dev). Schema gestionado manualmente — no hay Flyway ni Liquibase todavía. `ddl-auto: none` en dev. Para ver el schema actual, conectar a la BD con:

```bash
docker exec masters-db psql -U root -d postgres
```

## Estrategia de IDs

| Tipo de PK | Tablas | Generación en Java |
|---|---|---|
| `UUID` (v7) | `users`, `teachers`, `students`, `courses`, `enrollments`, `grades`, `payments`, `vouchers`, `stored_files` | `@UuidGenerator(style = Style.VERSION_7)` |
| `INTEGER` IDENTITY | `programs`, `promotions`, `pensions`, `states` | `@GeneratedValue(strategy = IDENTITY)` |
| `BIGINT` IDENTITY | `audit_logs`, `notifications`, `assignments` | `@GeneratedValue(strategy = IDENTITY)` |

`assignments` usa BIGINT IDENTITY como PK surrogate + índice único parcial `(id_course, id_teacher) WHERE deleted_at IS NULL` para soportar reasignación después de soft delete.

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
| `user_role` | `ADMIN`, `TEACHER`, `STUDENT` | Administrador, Docente, Estudiante |
| `teacher_category` | `PRINCIPAL`, `ASSOCIATE`, `AUXILIARY` | Principal, Asociado, Auxiliar |
| `teacher_type` | `INTERNAL`, `EXTERNAL` | Interno, Externo |
| `academic_degree` | `MASTER`, `DOCTOR` | Magíster, Doctor |
| `course_type` | `REGULAR`, `THESIS`, `TOPICS` | Regular, Tesis, Tópicos |
| `notification_type` | `VOUCHER_UPLOADED`, `VOUCHER_VALIDATED`, `VOUCHER_OBSERVED`, `VOUCHER_REJECTED`, `GRADE_REGISTERED`, `GRADE_MODIFIED`, `ENROLLMENT_UPDATED` | (ver NotificationType.java) |

## Soft delete con índices parciales

`@SQLRestriction("deleted_at IS NULL")` filtra automáticamente. Para unicidad post-soft-delete, el schema usa índices únicos parciales:

```sql
-- Ejemplo: asegurar que solo hay un assignment activo por (curso, docente)
CREATE UNIQUE INDEX uq_assignments_active
    ON assignments (id_course, id_teacher)
    WHERE deleted_at IS NULL;
```

JPA no declara estos índices — son invariantes a nivel BD. Para pre-validar en Java:

```java
if (repo.existsByCourse_IdAndTeacher_Id(courseId, teacherId)) {
    throw new BusinessException("Ya existe una asignación activa para este curso y docente");
}
```

Si hay race condition, `DataIntegrityViolationException` → el `GlobalExceptionHandler` la convierte en 409.

## Tabla stored_files

Patrón estándar para gestión de archivos sin exponer URLs de GCS:

```sql
CREATE TABLE stored_files (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_name   VARCHAR(255) NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    size_bytes      BIGINT NOT NULL,
    object_key      VARCHAR(500) NOT NULL,  -- path en GCS, nunca exponer al cliente
    id_uploaded_by  UUID NOT NULL REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_stored_files_uploader ON stored_files (id_uploaded_by);
```

- Nunca almacenar la URL firmada en la BD — se genera on-demand en `GcsStorageService.signedDownloadUrl()`.
- Convención de `object_key`: `files/{año}/{uuid}.{ext}`, e.g. `files/2026/0192f8c1-....pdf`.
- Otros módulos (vouchers, syllabi) pueden referenciar `stored_files.id` en vez de almacenar paths propios.

## Columnas estándar

| Columna | Tipo PG | Uso |
|---|---|---|
| `id` | `UUID` o `BIGINT` | PK |
| `created_at` | `TIMESTAMPTZ NOT NULL DEFAULT NOW()` | Auditoría de creación |
| `updated_at` | `TIMESTAMPTZ NOT NULL DEFAULT NOW()` | Auditoría de modificación |
| `deleted_at` | `TIMESTAMPTZ` (nullable) | Soft delete — NULL = activo |

## Patrones de query

**Relación simple** — `@SQLRestriction` aplica automáticamente:
```java
List<Student> findByPromotionId(Integer promotionId);
```

**JPQL con join** — no agregar `deleted_at IS NULL` manualmente:
```java
@Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.course.id = :courseId")
Optional<Enrollment> findActiveByStudentAndCourse(@Param("studentId") UUID s, @Param("courseId") UUID c);
```

**Verificar existencia con FK compuesta**:
```java
boolean existsByCourse_IdAndTeacher_Id(UUID courseId, UUID teacherId);
```

**Obtener referencia lazy** (sin hit a BD, para setear relaciones ManyToOne):
```java
Course course = courseRepository.getReferenceById(courseId);
assignment.setCourse(course);
```

## Anti-patrones

- `@Enumerated(EnumType.ORDINAL)` — frágil ante reordenamientos del enum.
- Almacenar URLs firmadas de GCS en la BD — expiran y contaminan columnas.
- Hard-delete en tablas con `deleted_at`.
- Agregar `WHERE deleted_at IS NULL` manualmente en JPQL — `@SQLRestriction` ya lo hace.
- `@UniqueConstraint` estándar en tablas con soft delete — usar índice parcial en SQL.
