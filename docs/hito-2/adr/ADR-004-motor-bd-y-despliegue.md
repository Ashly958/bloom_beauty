# ADR-004: MySQL 8 y despliegue con Docker Compose en un VPS

- **Estado:** Aceptado
- **Fecha:** 2026-11-25
- **Requerimientos relacionados:** RNF-05, RNF-06, RNF-09

## Contexto y problema

El Parcial 1 definió MySQL 8 como motor y un esquema de 18 tablas con `CHECK`, `ENUM` y llaves foráneas. Hay que decidir si se mantiene ese motor y cómo se despliega el sistema en producción con un presupuesto de pequeña empresa, HTTPS obligatorio y recuperación rápida ante fallas.

## Alternativas evaluadas

| Alternativa | A favor | En contra |
|---|---|---|
| **A. MySQL 8 + Docker Compose en un VPS** | Motor ya modelado en el Parcial 1, InnoDB transaccional, soporte de `CHECK` y `JSON`; un solo servidor (≈ USD 12/mes); la misma configuración corre en el PC del desarrollador. | Un único nodo: si el VPS cae, cae el sistema; los respaldos son responsabilidad del equipo. |
| **B. PostgreSQL** | Índices parciales (permitirían "una sola caja ABIERTA" en la BD), tipos más ricos. | Obligaría a rehacer el script y el modelo entregados en el Parcial 1 sin una necesidad concreta. |
| **C. PaaS administrado** (Render, Railway + BD administrada) | Sin administrar servidores; respaldos automáticos. | Costo mayor y variable; *cold starts*; menos control de red y de la versión de MySQL. |
| **D. Kubernetes** | Alta disponibilidad y autoescalado. | Complejidad desproporcionada para una tienda. |

## Decisión tomada

Se mantiene **MySQL 8.0 (InnoDB, utf8mb4)** y se despliega con **Docker Compose en un VPS Ubuntu 24.04** con cinco contenedores: `proxy` (Nginx con TLS 1.2/1.3 de Let's Encrypt), `certbot` (renovación), `frontend` (SPA compilada), `backend` (Spring Boot, JRE 17) y `db` (MySQL). La base de datos y el backend están en una red Docker `internal`, sin puertos publicados; el backend usa además una red de salida solo para enviar correo SMTP, y únicamente el proxy expone 80 y 443. Los datos persisten en el volumen `mysql_data` y los secretos se leen de un archivo `.env` fuera del repositorio. El esquema se versiona con scripts `V1__…sql` (Parcial 1) y `V2__…sql` (Parcial 2) y Hibernate trabaja con `ddl-auto: none`.

## Consecuencias

**Positivas**
- Mismo entorno en desarrollo y producción (`docker compose up`).
- Superficie de ataque mínima: la BD no es accesible desde internet.
- Reinicio automático (`restart: unless-stopped`) y *healthchecks* aportan a la disponibilidad de RNF-05.

**Negativas / trade-offs**
- Punto único de falla; se mitiga con respaldo diario `mysqldump` hacia almacenamiento externo y un procedimiento de restauración documentado.
- La regla "una sola caja ABIERTA" se garantiza en la aplicación (bloqueo sobre `configuracion_negocio`) y no con un índice parcial de la BD.
- El equipo debe aplicar las actualizaciones de seguridad del sistema operativo y de las imágenes.
