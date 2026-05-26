---
name: roles
description: Sistema de roles con @Authorize y @Public — visualización en Swagger, enforcement en producción, comportamiento por defecto. Activar al agregar o modificar endpoints, configurar acceso por rol, o preguntar cómo proteger un endpoint.
---

# Roles

## Resumen del sistema

| Situación | Resultado |
|---|---|
| Endpoint sin anotación | Solo **ADMIN** puede acceder (por defecto) |
| `@Authorize(roles = {...})` | Solo los roles indicados pueden acceder |
| `@Public` | Sin autenticación requerida |

Todo esto se documenta automáticamente en Swagger via `RolesOperationCustomizer` y se enforce en producción via `RolesEnforcementAspect`.

## @Authorize

```java
// Acceso para múltiples roles
@GetMapping
@Authorize(roles = {UserRole.ADMIN, UserRole.TEACHER}, description = "Ver todos los cursos")
@Operation(summary = "List all courses")
public ApiResponse<List<CourseResponse>> findAll() { ... }

// Solo ADMIN (equivalente a no poner @Authorize)
@DeleteMapping("/{id}")
@Authorize  // roles = {UserRole.ADMIN} es el default
@Operation(summary = "Delete a course")
public void delete(@PathVariable UUID id) { ... }

// Todos los roles autenticados
@GetMapping("/{id}")
@Authorize(roles = {UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT, UserRole.COORDINATOR})
@Operation(summary = "Get a course by id")
public ApiResponse<CourseResponse> findById(@PathVariable UUID id) { ... }
```

`@Authorize` también puede aplicarse a nivel de clase (controlador), en cuyo caso aplica a todos los métodos que no tengan su propia anotación:

```java
@RestController
@Authorize(roles = {UserRole.ADMIN, UserRole.TEACHER})  // aplica a todos los métodos
public class CourseController { ... }
```

## @Public

```java
// Endpoint completamente público — sin token requerido
@GetMapping("/health")
@Public
@Operation(summary = "Health check")
public String health() { return "ok"; }
```

## Cómo se ve en Swagger

`RolesOperationCustomizer` agrega automáticamente al description de cada operación:

- `@Public` → `> Acceso público — no se requiere autenticación.`
- `@Authorize(roles = {ADMIN, TEACHER})` → `**Roles requeridos:** ADMIN, TEACHER`
- Sin anotación → `> Acceso restringido a **ADMIN** (por defecto).`

Los endpoints protegidos también reciben el `SecurityRequirement("bearerAuth")` en el spec OpenAPI, lo que hace que Swagger UI muestre el candado y requiera el token para "Try it out".

## Cómo se enforce en producción

`RolesEnforcementAspect` es un `@Aspect` activo con `@Profile("!(dev | test)")`. Intercepta todos los métodos de `@RestController` y:

1. Si el método/clase tiene `@Public` → continúa sin verificar.
2. Si no hay autenticación o es anónima → lanza `AccessDeniedException` (→ 403).
3. Si tiene `@Authorize` → verifica que la autenticación tenga alguno de los roles requeridos.
4. Sin anotación → verifica que la autenticación tenga rol `ADMIN`.

En dev (`SPRING_PROFILES_ACTIVE=dev`) el aspecto está desactivado — el `SecurityConfig` de dev hace `permitAll()`.

## Roles disponibles

```java
public enum UserRole {
    ADMIN,       // Administrador — acceso completo
    TEACHER,     // Docente
    STUDENT,     // Estudiante
    COORDINATOR  // Coordinador de programa
}
```

## Los roles en JWT

El `RolesEnforcementAspect` busca authorities con el prefijo `ROLE_`:
- `ROLE_ADMIN`
- `ROLE_TEACHER`
- `ROLE_STUDENT`
- `ROLE_COORDINATOR`

El `SecurityConfig` de producción (a implementar) debe convertir el claim `role` del JWT de Google en estas authorities usando un `JwtAuthenticationConverter`.

## Anti-patrones

- Dejar endpoints sin `@Authorize` con la intención de que sean públicos — sin `@Public` el default es ADMIN.
- Usar `@PreAuthorize` directamente — usar `@Authorize` para consistencia con la documentación Swagger.
- Poner `@Public` en un controlador entero si solo uno o dos métodos son públicos.
