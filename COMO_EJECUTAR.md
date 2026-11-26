# Cómo ejecutar Bloom Beauty en tu PC (Windows)

## 1. Instalar (una sola vez)

| Programa | Para qué | Descarga |
|---|---|---|
| Java JDK 17 (Temurin) | Ejecutar el backend | https://adoptium.net |
| IntelliJ IDEA Community | Abrir y ejecutar el proyecto (trae Maven incluido) | https://www.jetbrains.com/idea/download |
| Docker Desktop | Levantar MySQL sin instalarlo | https://www.docker.com/products/docker-desktop |
| Git | Subir el proyecto a GitHub | https://git-scm.com |

> Si ya tienes MySQL 8 instalado, puedes saltarte Docker: ejecuta en MySQL Workbench
> `database/V1__esquema_inicial.sql` y luego `database/V2__ajustes_parcial2.sql`, y crea el usuario
> `bloom_app` con clave `cambiar` con permisos sobre `bloom_beauty`.

## 2. Levantar la base de datos

En PowerShell, dentro de la carpeta del proyecto:

```powershell
cd C:\Users\ashly\bloom-beauty
docker compose -f docker-compose.dev.yml up -d
```

La primera vez crea la base `bloom_beauty` con las 18 tablas y los datos de ejemplo (5 productos, categorías, marcas y métodos de pago).

## 3. Ejecutar el backend

**Opción A – IntelliJ (recomendada):**
1. *File → Open* → selecciona la carpeta `backend` → *Trust project*. Espera a que descargue las dependencias.
2. *Run → Edit Configurations → BloomBeautyApplication → Environment variables* y agrega:
   `ADMIN_EMAIL=admin@bloombeauty.co;ADMIN_PASSWORD=Bloom2026*`
3. Abre `BloomBeautyApplication.java` y presiona ▶.

**Opción B – Consola (requiere Maven instalado):**
```powershell
cd C:\Users\ashly\bloom-beauty\backend
$env:ADMIN_EMAIL="admin@bloombeauty.co"; $env:ADMIN_PASSWORD="Bloom2026*"
mvn spring-boot:run
```

Cuando veas `Started BloomBeautyApplication` y `Administrador inicial creado`, la API está lista en `http://localhost:8080`.

## 4. Probar la API (Swagger)

Abre **http://localhost:8080/swagger-ui.html**

1. `POST /api/v1/auth/login`
   ```json
   { "email": "admin@bloombeauty.co", "password": "Bloom2026*" }
   ```
   Copia el `token` de la respuesta → botón **Authorize** (arriba a la derecha) → pégalo.
2. `POST /api/v1/cajas` – abrir caja
   ```json
   { "montoInicial": 100000 }
   ```
3. `GET /api/v1/productos` – ver el catálogo.
4. `POST /api/v1/ventas` – registrar una venta (HU-01)
   ```json
   {
     "lineas": [ { "productoId": 3, "cantidad": 2, "porcentajeDescuento": 0 } ],
     "pagos":  [ { "metodo": "EFECTIVO", "monto": 35000, "montoRecibido": 50000 } ]
   }
   ```
   Respuesta **201** con el comprobante `BB-000001` y cambio de $15.000.
5. Para ver los errores del diagrama de secuencia:
   - Pago de `30000` en lugar de `35000` → **422 PAGO_INCOMPLETO**.
   - `"cantidad": 100` con pago de `1750000` (el total exacto, para pasar la validación de pago) → **409 STOCK_AGOTADO**.
   - Cerrar la caja con `POST /api/v1/cajas/actual/cierre`:
     ```json
     { "efectivoDeclarado": 135000, "transferenciaDeclarada": 0, "tarjetaDeclarada": 0 }
     ```
     (base 100.000 + venta 35.000). Si declaras un valor con más de $5.000 de diferencia sin `observaciones` → **422**.
     Después de cerrar, intenta vender otra vez → **409 CAJA_NO_DISPONIBLE**.
6. `GET /api/v1/inventario/alertas` – el Polvo Traslúcido aparece con stock bajo.

## 5. Ejecutar las pruebas

```powershell
cd C:\Users\ashly\bloom-beauty\backend
mvn test
```
(o clic derecho sobre la carpeta `src/test` en IntelliJ → *Run 'All Tests'*).

## 6. Ver el prototipo de interfaz

Doble clic en `frontend/prototipo/index.html`.

## 7. Apagar la base de datos

```powershell
docker compose -f docker-compose.dev.yml down        # conserva los datos
docker compose -f docker-compose.dev.yml down -v     # borra los datos y empieza de cero
```
