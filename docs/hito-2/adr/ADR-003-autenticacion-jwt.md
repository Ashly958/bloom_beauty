# ADR-003: Autenticación stateless con JWT y autorización por rol

- **Estado:** Aceptado
- **Fecha:** 2026-11-25
- **Requerimientos relacionados:** RF-01, RF-02, RF-03, RNF-01, RNF-06

## Contexto y problema

La SPA en React consume una API REST. Solo el Administrador puede crear productos, cambiar precios, anular ventas o cerrar caja; un Trabajador que lo intente debe recibir **403 Forbidden** en menos de 100 ms (RNF-01). Las contraseñas deben guardarse cifradas, el tráfico debe ir por TLS y la sesión debe vencer (RNF-06). Se necesita además bloquear la cuenta tras 5 intentos fallidos (RF-02).

## Alternativas evaluadas

| Alternativa | A favor | En contra |
|---|---|---|
| **A. Sesión en servidor** (cookie `JSESSIONID`) | Revocación inmediata; soporte nativo de Spring Security. | Estado en memoria del backend; requiere protección CSRF y complica escalar a más instancias. |
| **B. JWT firmado (HS256) stateless** | Sin estado en el servidor; el rol viaja en el token y se valida en cada petición sin consultar la BD; estándar para SPA + API. | No se puede revocar antes de vencer sin una lista negra. |
| **C. Proveedor externo OAuth2 / OIDC** (Keycloak, Auth0) | Funciones avanzadas (MFA, SSO). | Otro servicio que operar o pagar; excesivo para dos roles internos. |

## Decisión tomada

Se adopta **B: JWT HS256 con vigencia de 8 horas**, emitido por `JwtService` en `POST /api/v1/auth/login` y validado por `JwtAuthenticationFilter` en cada petición. Spring Security se configura `STATELESS`, con CSRF deshabilitado (no hay cookies de sesión), CORS restringido al dominio de la SPA y `BCryptPasswordEncoder(12)` para las contraseñas. La autorización se declara en los controladores con `@PreAuthorize("hasRole('ADMINISTRADOR')")`; los errores se devuelven como `ProblemDetail` (401, 403, 423).

El bloqueo por intentos se implementa como regla de dominio en `Usuario.registrarIntentoFallido()` y el login usa `@Transactional(noRollbackFor = AutenticacionException.class)` para que el contador se guarde aunque la autenticación falle. La recuperación de contraseña usa un token aleatorio de 256 bits, de un solo uso y válido 30 minutos, enviado por correo; la respuesta es idéntica exista o no el correo.

## Consecuencias

**Positivas**
- El backend no guarda sesiones: cualquier contenedor puede atender cualquier petición.
- La verificación de rol es local y rápida (cumple < 100 ms).
- El secreto JWT y las credenciales se inyectan por variables de entorno, nunca en el repositorio.

**Negativas / trade-offs**
- Si se inactiva a un usuario, su token sigue siendo válido hasta 8 h; se mitiga con el cierre por inactividad de 30 min en la SPA y, a futuro, con una lista de tokens revocados.
- El token debe guardarse en memoria de la SPA (no en `localStorage`) para reducir el riesgo de XSS.
- Rotar el secreto invalida todas las sesiones activas.
