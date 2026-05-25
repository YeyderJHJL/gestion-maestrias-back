---
name: error-handling
description: Formas de respuesta REST (ApiResponse vs ApiError), jerarquía de excepciones, GlobalExceptionHandler, errores de validación, códigos HTTP a usar y ejemplos de body de respuesta. Activar al agregar un endpoint, lanzar excepciones desde un servicio, crear tipos de excepción, modificar el handler global o dar forma a cualquier respuesta de la API.
---

# Error Handling

## Dos formas de respuesta distintas

**`ApiResponse<T>`** — envuelve respuestas 2xx. Tres campos: `success`, `data`, `message`.

**`ApiError`** — para 4xx y 5xx. Incluye `status`, `error`, `message`, `path`, `timestamp` y `validationErrors` opcional.

El status HTTP es la fuente de verdad — no duplicarlo en el body.

## ApiResponse\<T\>

```java
public record ApiResponse<T>(boolean success, T data, String message) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }
}
```

Ejemplo de respuesta exitosa:
```json
{
  "success": true,
  "data": { "id": "0192f8c1-...", "name": "Algoritmos Avanzados" },
  "message": "Curso creado correctamente"
}
```

<!-- Paginación: cuando se implemente, usar PagedResponse<T> separado en lugar de
     modificar ApiResponse. El formato se definirá en coordinación con el cliente. -->

## ApiError

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(int status, String error, String message,
                       String path, Instant timestamp,
                       Map<String, String> validationErrors) {
    public static ApiError of(HttpStatus status, String message, String path) { ... }
    public static ApiError of(HttpStatus status, String message, String path,
                               Map<String, String> validationErrors) { ... }
}
```

`@JsonInclude(NON_NULL)` omite `validationErrors` cuando no aplica.

## Excepciones de dominio

```java
// 404 Not Found
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Object id) {
        super("%s with id %s not found".formatted(resource, id));
    }
}

// 409 Conflict — violación de regla de negocio, duplicado, estado inválido
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
```

Convenciones:
- Mensajes hacia el usuario → **español**.
- No crear una clase de excepción por cada error — el mensaje lleva los detalles.
- Solo crear una nueva excepción si se necesita un HTTP status diferente.

## GlobalExceptionHandler — mapeos completos

| Excepción | HTTP | Nota |
|---|---|---|
| `ResourceNotFoundException` | 404 | — |
| `BusinessException` | 409 | — |
| `MethodArgumentNotValidException` | 400 | incluye `validationErrors` por campo |
| `HandlerMethodValidationException` | 400 | incluye `validationErrors` por parámetro |
| `HttpMessageNotReadableException` | 400 | JSON mal formado |
| `MethodArgumentTypeMismatchException` | 400 | tipo incorrecto en path/query param |
| `DataIntegrityViolationException` | 409 | log WARN con causa específica |
| `AccessDeniedException` | 403 | de Spring Security |
| `Exception` (catch-all) | 500 | log ERROR con stack trace completo |

## Política de logging

| Severidad | Cuándo |
|---|---|
| Ninguno | Errores de cliente esperados (404, validación, tipo incorrecto, JSON mal formado) |
| WARN | `DataIntegrityViolationException` — posible race condition o bug de unicidad |
| ERROR | Catch-all `Exception.class` — bug inesperado |

Nunca loguear stack traces para errores de cliente. No filtrar `ex.getMessage()` hacia la respuesta en el catch-all.

## Códigos HTTP a usar

| Código | Cuándo |
|---|---|
| **200 OK** | GET, PUT exitoso |
| **201 Created** | POST que crea un recurso |
| **204 No Content** | DELETE, o PUT/PATCH sin body en la respuesta |
| **400 Bad Request** | Validación, JSON mal formado, tipo incorrecto en parámetro |
| **401 Unauthorized** | Token ausente o inválido |
| **403 Forbidden** | Autenticado pero sin el rol requerido |
| **404 Not Found** | Recurso no existe |
| **409 Conflict** | Violación de regla de negocio, duplicado, estado inválido |
| **500 Internal Server Error** | Solo excepciones inesperadas |

## Ejemplos de body de respuesta

**Validación (400):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/courses",
  "timestamp": "2026-05-24T14:00:00Z",
  "validationErrors": {
    "code": "no debe estar vacío",
    "name": "el tamaño debe estar entre 1 y 200"
  }
}
```

**Not Found (404):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Course with id 0192f8c1-... not found",
  "path": "/api/v1/courses/0192f8c1-...",
  "timestamp": "2026-05-24T14:00:00Z"
}
```

**Business rule (409):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe una asignación activa para este curso y docente",
  "path": "/api/v1/assignments",
  "timestamp": "2026-05-24T14:00:00Z"
}
```

**Forbidden (403):**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Insufficient role",
  "path": "/api/v1/users",
  "timestamp": "2026-05-24T14:00:00Z"
}
```

**Success con mensaje (201):**
```json
{
  "success": true,
  "data": { "id": "0192f8c1-...", "name": "Algoritmos Avanzados" },
  "message": "Curso creado correctamente"
}
```

**Success sin mensaje (200 GET):**
```json
{
  "success": true,
  "data": { "id": "0192f8c1-...", "name": "Algoritmos Avanzados" },
  "message": null
}
```

## Agregar un nuevo mapeo

Agregar un método al `GlobalExceptionHandler` existente. No crear un segundo `@RestControllerAdvice`.

## Anti-patrones

- `throw new RuntimeException(...)` genérico — usar una excepción de dominio.
- `catch (Exception e) { }` vacío — nunca tragar.
- Retornar `null` para indicar "no encontrado" — lanzar `ResourceNotFoundException`.
- `ex.getMessage()` hacia el cliente en el catch-all.
- Log ERROR para 404s o errores de validación.
- Duplicar el HTTP status dentro de `ApiResponse`.
