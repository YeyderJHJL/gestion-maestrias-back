---
name: security
description: Seguridad OAuth2 con Google — flujo completo, AppJwtAuthenticationConverter, AppUserPrincipal, SecurityHelper, perfiles dev vs prod, CORS, primer login con vinculación de google_sub. Activar al agregar endpoints que necesiten el usuario autenticado, modificar seguridad, trabajar con JWT, implementar CORS o preguntar cómo obtener el usuario actual.
---

# Security

## Flujo completo

```
Frontend (http://localhost:3000)
    │
    ├─[1]─► Google Identity Services popup
    │           └─ Google emite ID Token (JWT firmado RS256, expira en 1h)
    │
    ├─[2]─► GET/POST /api/v1/...
    │        Authorization: Bearer <id_token_de_google>
    │
    └─[3]─► Backend valida JWT
                ├─ Verifica firma con clave pública de Google (issuer-uri)
                ├─ Verifica audience en prod
                └─ AppJwtAuthenticationConverter consulta BD → AppUserPrincipal
```

No hay JWT propio. No hay JWT secret. Google firma; el backend solo verifica.

## AppUserPrincipal

El principal que vive en `SecurityContextHolder` una vez autenticado:

```java
public record AppUserPrincipal(UUID userId, UserRole role) {}
```

## SecurityHelper — obtener el usuario actual

```java
// Desde cualquier controlador o servicio
UUID id    = SecurityHelper.currentUserId();
AppUserPrincipal p = SecurityHelper.currentPrincipal();
```

Lanza `AccessDeniedException` si no hay usuario autenticado. Solo usar en código que se ejecuta tras autenticación.

## AppJwtAuthenticationConverter

Converter que convierte el JWT de Google en un `UsernamePasswordAuthenticationToken` con `AppUserPrincipal` como principal y `ROLE_<ROLE>` como authority.

**Lógica de búsqueda (primer login):**
1. Busca por `google_sub` (claim `sub`) → usuario que ya inició sesión antes.
2. Si no encuentra, busca por `email` → usuario pre-registrado por el admin (google_sub era placeholder `"pending-<email>"`).
3. Vincula el `google_sub` real y actualiza nombre/apellido desde Google si el sub era un placeholder.
4. Lanza `AccessDeniedException` si el email tampoco existe en la BD.
5. Lanza `AccessDeniedException` si el usuario tiene `is_active = false`.

**Pre-registro de usuarios:**
El admin crea el usuario desde la UI (o con `ADMIN_EMAIL` env var para el admin inicial). El usuario se autentica con Google la primera vez y queda vinculado automáticamente.

## Perfiles de seguridad

| Perfil | SecurityConfig | Comportamiento |
|---|---|---|
| `dev` / `test` | `SecurityConfig` | `permitAll()` + JWT procesado **cuando viene** → principal se establece si el JWT es válido |
| `prod` | `ProdSecurityConfig` | Toda request requiere JWT válido; `/actuator/health` público |

En dev: sin JWT → anónimo (OK para Swagger). Con JWT válido → `AppUserPrincipal` disponible.

## CORS

Configurado en `WebConfig.addCorsMappings`. El origen permitido se lee de:

```yaml
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

Para agregar un origen en prod:
```
CORS_ALLOWED_ORIGINS=https://app.tu-dominio.com
```

Para múltiples orígenes separar con coma:
```
CORS_ALLOWED_ORIGINS=https://app.tu-dominio.com,https://otro.dominio.com
```

Ambos `SecurityConfig` (dev) y `ProdSecurityConfig` incluyen `.cors(Customizer.withDefaults())` para que Spring Security respete la config de Spring MVC.

## Variables de entorno requeridas

| Variable | Descripción | Ejemplo |
|---|---|---|
| `GOOGLE_OAUTH_ISSUER_URI` | Issuer URL de Google (tiene default) | `https://accounts.google.com` |
| `GOOGLE_OAUTH_CLIENT_ID` | Client ID del proyecto en GCP | `12345.apps.googleusercontent.com` |
| `GOOGLE_OAUTH_AUDIENCE` | Audience del JWT (default = CLIENT_ID) | igual que CLIENT_ID |

`CLIENT_SECRET` ya NO es necesario en el backend (era para el flujo `oauth2.client` que fue eliminado).

## Endpoint /users/me

```
GET /api/v1/users/me
Authorization: Bearer <token>
```

Devuelve el perfil del usuario autenticado. Accesible por todos los roles.

## Anti-patrones

- No usar `@PreAuthorize` — usar `@Authorize` para consistencia con Swagger.
- No inyectar `Authentication` en controladores — usar `SecurityHelper`.
- No almacenar el JWT del frontend — el frontend lo guarda en memoria y lo envía en cada request.
- No crear un JWT propio — Google es el issuer, no el backend.
- No configurar `oauth2.client.registration` en el backend — solo `oauth2.resourceserver`.
