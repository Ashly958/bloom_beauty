# Bloom Beauty 🌸

Sistema web de punto de venta (POS), inventario, caja y reportes para **Bloom Beauty**, tienda de maquillaje y cuidado personal.
Proyecto formativo de **Ingeniería de Software II** – Tecnológico de Antioquia, CAT Los Patios.

| Capa | Tecnología |
|---|---|
| Frontend | React 18 + Vite (prototipo HTML/CSS en `frontend/prototipo`) |
| Backend | Java 17 · Spring Boot 3.3 · Spring Security + JWT · Spring Data JPA (Hibernate 6) |
| Base de datos | MySQL 8.0 (InnoDB, utf8mb4) |
| Despliegue | Docker Compose · Nginx (TLS Let's Encrypt) · VPS Ubuntu 24.04 |

## Estructura del repositorio

```
bloom-beauty/
├── backend/                 API REST Spring Boot (arquitectura en capas)
│   └── src/main/java/co/bloombeauty/
│       ├── controller/  service/ (impl/, pago/)  repository/  domain/ (enums/)
│       ├── dto/  mapper/  event/  exception/  security/  config/
├── frontend/
│   ├── prototipo/           Prototipo navegable (index.html, styles.css, script.js)
│   └── Dockerfile
├── database/
│   ├── V1__esquema_inicial.sql     Esquema del Parcial 1 (18 tablas)
│   └── V2__ajustes_parcial2.sql    Índices compuestos y versión de caja
├── deploy/nginx/bloom.conf  Proxy inverso con HTTPS
├── docs/hito-2/             C4, UML, ADRs y diccionario de datos (Parcial 2)
├── docker-compose.yml
└── .env.example
```

## Ejecutar en tu PC

Guía paso a paso (base de datos, backend, pruebas con Swagger): **[COMO_EJECUTAR.md](COMO_EJECUTAR.md)**.

## Desplegar en producción con Docker

```bash
cp .env.example .env          # completar claves
docker compose up -d --build
```

- Primera vez en el servidor: obtener el certificado antes de levantar el proxy con
  `docker compose run --rm -p 80:80 --entrypoint certbot certbot certonly --standalone -d <dominio> --agree-tos -m <correo>`.
- API: `https://<dominio>/api/v1` · Swagger UI: `/swagger-ui.html`
- El primer administrador se crea con `ADMIN_EMAIL` / `ADMIN_PASSWORD` del `.env`.

## Ejecutar el backend en local

```bash
cd backend
mvn spring-boot:run     # requiere MySQL 8 con los scripts de /database
mvn test                # pruebas de dominio
```

## Endpoints principales

| Método | Ruta | Rol | HU |
|---|---|---|---|
| POST | `/api/v1/auth/login` | público | HU-03 |
| GET | `/api/v1/productos?q=&categoriaId=&stockBajo=` | autenticado | HU-07 |
| POST / PUT / DELETE | `/api/v1/productos[/{id}]` | ADMINISTRADOR | HU-05, HU-06 |
| POST | `/api/v1/cajas` | autenticado | HU-11 |
| POST | `/api/v1/cajas/actual/cierre` | ADMINISTRADOR | HU-02 |
| POST | `/api/v1/ventas` | TRABAJADOR / ADMINISTRADOR | HU-01 |
| PATCH | `/api/v1/ventas/{id}/anulacion` | ADMINISTRADOR | HU-12 |
| GET | `/api/v1/inventario/alertas` | autenticado | HU-07 |

## Documentación de arquitectura (Hito 2)

Ver [`docs/hito-2/README.md`](docs/hito-2/README.md).
