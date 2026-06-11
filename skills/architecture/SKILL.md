---
name: architecture
description: Reglas de arquitectura — package by feature, BaseEntity con auditoría y soft delete, UUID v7, controladores, servicios, repositorios, DTOs como records, mapping manual, enums con label en español, seguridad OAuth2, anotaciones OpenAPI, application.yaml. Activar al crear o modificar entidades, repositorios, servicios, controladores, DTOs o configuración de seguridad.
---

# Architecture

## Package by feature

Un folder por concepto de dominio. Todo lo relacionado a ese concepto vive junto: entidad, repositorio, servicio, controlador, DTOs. Solo concerns transversales van en `shared/`.

## BaseEntity y CreatedEntity

```java
// shared/audit/BaseEntity.java — para entidades con ciclo de vida completo
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}

// shared/audit/CreatedEntity.java — para tablas append-only (audit_logs, notifications, stored_files)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class CreatedEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
```

`@EnableJpaAuditing` está en `MastersApplication`.

## UUID v7 — PK de entidades de negocio

```java
@Id
@GeneratedValue
@UuidGenerator(style = Style.VERSION_7)
@Column(name = "id", nullable = false, updatable = false)
private UUID id;
```

Usar `Style.VERSION_7` (time-ordered). Nunca `@GeneratedValue(strategy = GenerationType.UUID)` (emite v4 random, fragmenta índices).

**PKs de tablas catálogo** (`programs`, `semesters`, `states`): `Integer` con `GenerationType.IDENTITY`.
**PKs de tablas append-only** (`audit_logs`, `notifications`): `Long` con `GenerationType.IDENTITY`.
**`assignments`**: `Long` con `GenerationType.IDENTITY` + índice único parcial `(id_course, id_teacher, id_semester) WHERE deleted_at IS NULL`.

Las entidades auditables implementan `Auditable`. `getAuditId()` devuelve `Object` para soportar PKs UUID y numéricas; `audit_logs.id_entity` almacena el valor como texto.

## Soft delete

En toda entidad que tiene `deleted_at`:

```java
@SQLDelete(sql = "UPDATE <tabla> SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
```

- `@SQLDelete` intercepta `repository.delete(...)` y ejecuta un UPDATE.
- `@SQLRestriction` agrega `deleted_at IS NULL` a cada query de Hibernate automáticamente.
- **Nunca** setear `deletedAt` manualmente ni agregar `WHERE deleted_at IS NULL` en JPQL.

## Entidad — ejemplo abreviado

```java
@Entity
@Table(name = "courses")
@SQLDelete(sql = "UPDATE courses SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Course extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = Style.VERSION_7)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false, columnDefinition = "course_type")
    private CourseType type;

    // getters y setters — sin Lombok
}
```

## Repositorios

Extender `JpaRepository`. Puede quedar vacío si el servicio solo necesita CRUD. Agregar queries por nombre de método cuando son simples, `@Query` cuando no.

```java
public interface CourseRepository extends JpaRepository<Course, UUID> {
    boolean existsByPromotion_IdAndCode(Integer promotionId, String code);
}
```

`@SQLRestriction` asegura que `deleted_at IS NULL` se agrega automáticamente a todas las queries.

## Servicios

Usar `@Transactional` en servicios. El patrón más común del repo es anotar métodos: `@Transactional(readOnly = true)` para lectura y `@Transactional` para escritura. `@Transactional` a nivel de clase también es válido cuando casi todos los métodos son write-oriented.

```java
@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public CourseResponse create(CourseRequest req) {
        if (courseRepository.existsByPromotion_IdAndCode(req.promotionId(), req.code())) {
            throw new BusinessException("Ya existe un curso con código " + req.code());
        }
        Course course = new Course();
        course.setName(req.name());
        course.setCode(req.code());
        return toResponse(courseRepository.save(course));
    }

    Course getOrThrow(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    // En este repo getReference valida existencia antes de asignar relaciones.
    @Transactional(readOnly = true)
    public Course getReference(UUID id) {
        return getOrThrow(id);
    }

    private CourseResponse toResponse(Course c) {
        return new CourseResponse(c.getId(), c.getName(), c.getCode(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
```

- Inyección por constructor. Nunca inyección de campo (`@Autowired` en campo).
- Lanzar `ResourceNotFoundException` (404) o `BusinessException` (409) — el handler los mapea a HTTP.
- Mensajes de error al usuario en **español**.

## Controladores

```java
@RestController
@RequestMapping("/courses")
@Tag(name = "Courses", description = "Course management")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/{id}")
    @Authorize(roles = {UserRole.ADMIN, UserRole.TEACHER})
    @Operation(summary = "Get a course by id")
    public ApiResponse<CourseResponse> findById(@PathVariable UUID id) {
        return ApiResponse.ok(courseService.findById(id));
    }

    @PostMapping
    @Authorize(roles = {UserRole.ADMIN}, description = "Crear un nuevo curso")
    @Operation(summary = "Create a new course")
    public ResponseEntity<ApiResponse<CourseResponse>> create(@Valid @RequestBody CourseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(courseService.create(req), "Curso creado correctamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a course")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        courseService.delete(id);
        return ApiResponse.ok(null, "Curso eliminado correctamente");
    }
}
```

- El prefijo `/api/v1` lo agrega automáticamente `WebConfig.configurePathMatch`.
- Respuestas exitosas → `ApiResponse<T>`, `ApiResponse<Void>` o `void` con 204 cuando intencionalmente no hay body.
- `@Tag` a nivel de clase, `@Operation` a nivel de método.
- `@Authorize` o `@Public` — ver `skills/roles/SKILL.md`.

## DTOs como records

```java
public record CourseRequest(
    @NotNull Integer programId,
    @NotNull Integer promotionId,
    @NotBlank @Size(max = 100) String code,
    @NotBlank @Size(max = 255) String name,
    @NotNull CourseType type
) {}

public record CourseResponse(
    UUID id,
    String code,
    String name,
    Instant createdAt,
    Instant updatedAt
) {}
```

- Validaciones Bean Validation en los componentes del record.
- Un DTO por dirección. No reutilizar el request como response.
- Mapping manual en el service (método `toResponse`).

## Archivos en módulos de dominio

Los módulos de negocio no almacenan URLs de GCS. Guardan una relación a `StoredFile` por FK y los requests reciben el UUID del archivo subido:

```java
public record CourseRequest(
    // ...otros campos...
    UUID syllabusFileId
) {}

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "id_syllabus_file")
private StoredFile syllabusFile;
```

- Subir primero con `POST /files` y usar el `id` devuelto.
- Para responses de dominio, devolver metadata resumida del archivo, no `downloadUrl`.
- Para descargar, el cliente llama `GET /files/{id}` y usa el `downloadUrl` temporal que genera `GcsStorageService`.
- Convenciones actuales: `courses.id_syllabus_file`, `enrollments.id_resolution_file`, `vouchers.id_file`.

## Enum con label en español

```java
public enum CourseType implements LabeledEnum {
    REGULAR("Regular"),
    THESIS("Tesis"),
    TOPICS("Tópicos");

    private final String label;

    CourseType(String label) { this.label = label; }

    @JsonCreator
    public static CourseType fromValue(String value) {
        return LabeledEnum.fromValue(CourseType.class, value);
    }

    @Override
    @JsonValue
    public String getLabel() { return label; }
}
```

El nombre Java del enum (`REGULAR`) debe coincidir con el valor del ENUM de PostgreSQL.

## Anti-patrones

- `@GeneratedValue(strategy = GenerationType.UUID)` — emite v4, fragmenta índices.
- Setear `deletedAt` manualmente.
- `WHERE deleted_at IS NULL` en JPQL — `@SQLRestriction` ya lo hace.
- Retornar entidades JPA desde el controlador.
- `@Transactional` en un método de controlador.
- Inyección de campo en lugar de constructor.
- `LocalDateTime` para campos de auditoría — usar `Instant`.
- `ddl-auto: update` o `create` — el schema lo maneja el SQL.
- `open-in-view: true`.
- Guardar URLs de GCS en tablas de dominio — usar FK a `stored_files.id`.
