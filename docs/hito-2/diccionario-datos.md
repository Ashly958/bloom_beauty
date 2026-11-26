# Diccionario de Datos – Bloom Beauty

Motor: **MySQL 8.0** (InnoDB, utf8mb4_spanish_ci). Esquema: `database/V1__esquema_inicial.sql` + `database/V2__ajustes_parcial2.sql`.
Cada columna indica su anotación de mapeo en Spring Data JPA (paquete `co.bloombeauty.domain`).

Convenciones: todas las relaciones `@ManyToOne` usan `FetchType.LAZY`; los ENUM se mapean con `@Enumerated(EnumType.STRING)`; los montos usan `BigDecimal` con `precision/scale` iguales al `DECIMAL` de la columna.

## 1. `productos` → `@Entity Producto`

Catálogo de productos de belleza. Soporta bloqueo optimista (RNF-02) y borrado lógico (RNF-03).

| Columna | Tipo SQL | Restricciones | Descripción | Mapeo JPA |
|---|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Identificador del producto | @Id @GeneratedValue(IDENTITY) Long |
| codigo_barras | VARCHAR(20) | NOT NULL, UNIQUE | Código EAN leído por el lector del POS | @Column(unique=true, length=20) |
| nombre | VARCHAR(100) | NOT NULL | Nombre comercial del producto | @Column(nullable=false, length=100) |
| descripcion | VARCHAR(500) | NULL | Descripción para catálogo | @Column(length=500) |
| categoria_id | INT | NOT NULL, FK → categorias(id) RESTRICT | Categoría (Rostro, Ojos, Labios…) | @ManyToOne(fetch=LAZY) @JoinColumn |
| marca_id | INT | NOT NULL, FK → marcas(id) RESTRICT | Marca comercializada | @ManyToOne(fetch=LAZY) @JoinColumn |
| tono | VARCHAR(40) | NULL | Tono o color (ej. Rojo Pasión) | @Column(length=40) |
| precio_costo | DECIMAL(12,2) | NOT NULL, CHECK ≥ 0 | Costo de compra unitario | BigDecimal @Column(precision=12, scale=2) |
| precio_venta | DECIMAL(12,2) | NOT NULL, CHECK > 0 | Precio al público con IVA incluido | BigDecimal @Column(precision=12, scale=2) |
| porcentaje_iva | DECIMAL(5,2) | NOT NULL, DEFAULT 19.00 | Tarifa de IVA del producto | BigDecimal |
| stock_actual | INT | NOT NULL, DEFAULT 0, CHECK ≥ 0 | Unidades disponibles | int — modificado solo por métodos de dominio |
| stock_minimo | INT | NOT NULL, DEFAULT 5, CHECK ≥ 0 | Umbral de alerta de stock bajo (RF-13) | int |
| fecha_vencimiento | DATE | NULL | Fecha de vencimiento del lote | LocalDate |
| imagen_url | VARCHAR(255) | NULL | Ruta de la imagen (≤ 2 MB) | String |
| inactivo | BOOLEAN | NOT NULL, DEFAULT FALSE | Borrado lógico (RF-07) | @SQLDelete + @SQLRestriction("inactivo = false") |
| version | INT | NOT NULL, DEFAULT 0 | Versión para bloqueo optimista | @Version Integer |
| created_at / updated_at | DATETIME | NOT NULL / NULL | Auditoría de creación y modificación | @CreationTimestamp / @UpdateTimestamp |

**Índices:** ix_productos_nombre (nombre) – búsqueda RF-08; ix_productos_stock (stock_actual, stock_minimo) – alertas; ix_productos_categoria_marca (categoria_id, marca_id, inactivo) – filtros del catálogo; ix_productos_vencimiento (fecha_vencimiento) – alerta 30 días

## 2. `ventas` → `@Entity Venta`

Encabezado de la factura del POS; raíz del agregado Venta.

| Columna | Tipo SQL | Restricciones | Descripción | Mapeo JPA |
|---|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Identificador de la venta | @Id @GeneratedValue(IDENTITY) Long |
| numero_factura | VARCHAR(20) | NOT NULL, UNIQUE | Consecutivo BB-000001, nunca reutilizado | @Column(unique=true, length=20) |
| cliente_id | BIGINT | NULL, FK → clientes(id) RESTRICT | Clienta; NULL = Consumidor final | @ManyToOne(fetch=LAZY) Cliente |
| usuario_id | BIGINT | NOT NULL, FK → usuarios(id) RESTRICT | Vendedor(a) que registró la venta | @ManyToOne(fetch=LAZY, optional=false) |
| sesion_caja_id | BIGINT | NOT NULL, FK → sesiones_caja(id) RESTRICT | Turno de caja al que pertenece | @ManyToOne(fetch=LAZY, optional=false) |
| fecha | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Fecha y hora de la venta | LocalDateTime |
| subtotal | DECIMAL(14,2) | NOT NULL, CHECK ≥ 0 | Σ cantidad × precio unitario | BigDecimal |
| descuento_total | DECIMAL(14,2) | NOT NULL, DEFAULT 0 | Σ descuentos por línea | BigDecimal |
| iva_total | DECIMAL(14,2) | NOT NULL, DEFAULT 0 | IVA contenido en el total | BigDecimal |
| total | DECIMAL(14,2) | NOT NULL, CHECK > 0 | subtotal − descuento_total | BigDecimal |
| estado | ENUM('PAGADA','ANULADA') | NOT NULL, DEFAULT 'PAGADA' | Estado de la venta | @Enumerated(EnumType.STRING) EstadoVenta |
| motivo_anulacion | VARCHAR(200) | NULL; CHECK estado='PAGADA' OR motivo NOT NULL | Motivo obligatorio al anular | String |
| anulada_por | BIGINT | NULL, FK → usuarios(id) | Administrador que anuló | @ManyToOne(fetch=LAZY) Usuario |
| fecha_anulacion | DATETIME | NULL | Momento de la anulación | LocalDateTime |

**Índices:** ix_ventas_fecha (fecha) – reportes por periodo; ix_ventas_caja_estado (sesion_caja_id, estado) – arqueo de caja; ix_ventas_cliente_fecha (cliente_id, fecha) – historial de la clienta

## 3. `detalle_venta` → `@Entity DetalleVenta`

Líneas de producto de cada venta (resuelve la N:M ventas–productos).

| Columna | Tipo SQL | Restricciones | Descripción | Mapeo JPA |
|---|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Identificador de la línea | @Id Long |
| venta_id | BIGINT | NOT NULL, FK → ventas(id) CASCADE | Venta a la que pertenece | @ManyToOne(fetch=LAZY) — lado dueño |
| producto_id | BIGINT | NOT NULL, FK → productos(id) RESTRICT | Producto vendido | @ManyToOne(fetch=LAZY) Producto |
| cantidad | INT | NOT NULL, CHECK > 0 | Unidades vendidas | int |
| precio_unitario | DECIMAL(12,2) | NOT NULL, CHECK > 0 | Precio congelado al momento de vender | BigDecimal |
| porcentaje_descuento | DECIMAL(5,2) | NOT NULL, DEFAULT 0, CHECK 0–30 | Descuento de la línea (RN-3) | BigDecimal |
| subtotal | DECIMAL(14,2) | NOT NULL | Valor neto de la línea | BigDecimal |

**Índices:** uq_venta_producto UNIQUE (venta_id, producto_id); ix_detalle_venta_producto (producto_id) – productos más vendidos

## 4. `pagos_venta` → `@Entity PagoVenta`

Pagos de una venta; varias filas por venta permiten el pago mixto.

| Columna | Tipo SQL | Restricciones | Descripción | Mapeo JPA |
|---|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Identificador del pago | @Id Long |
| venta_id | BIGINT | NOT NULL, FK → ventas(id) CASCADE | Venta pagada | @ManyToOne(fetch=LAZY) |
| metodo_pago_id | INT | NOT NULL, FK → metodos_pago(id) RESTRICT | Efectivo, transferencia o tarjeta | @ManyToOne(fetch=LAZY) MetodoPago |
| monto | DECIMAL(14,2) | NOT NULL, CHECK > 0 | Valor aplicado a la venta | BigDecimal |
| monto_recibido | DECIMAL(14,2) | NULL | Efectivo entregado por la clienta | BigDecimal |
| cambio | DECIMAL(14,2) | NULL, DEFAULT 0 | Vueltas (solo efectivo) | BigDecimal |
| referencia | VARCHAR(40) | NULL (obligatoria si el método la requiere) | Número de aprobación / transferencia | String — validado por la Strategy |

**Índices:** Índice implícito de la FK venta_id

## 5. `sesiones_caja` → `@Entity SesionCaja`

Turno de caja: apertura, arqueo y cierre diario.

| Columna | Tipo SQL | Restricciones | Descripción | Mapeo JPA |
|---|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Identificador del turno | @Id Long |
| usuario_apertura_id | BIGINT | NOT NULL, FK → usuarios(id) | Quién abrió la caja | @ManyToOne(fetch=LAZY, optional=false) |
| usuario_cierre_id | BIGINT | NULL, FK → usuarios(id) | Administrador que cerró | @ManyToOne(fetch=LAZY) |
| fecha_apertura / fecha_cierre | DATETIME | NOT NULL / NULL | Inicio y fin del turno | LocalDateTime |
| monto_inicial | DECIMAL(12,2) | NOT NULL, CHECK ≥ 0 | Base en efectivo | BigDecimal |
| total_ventas_efectivo / _transferencia / _tarjeta | DECIMAL(14,2) | NOT NULL, DEFAULT 0 | Acumulados por método de pago | BigDecimal (registrarIngreso) |
| total_gastos_efectivo | DECIMAL(14,2) | NOT NULL, DEFAULT 0 | Gastos pagados desde la caja | BigDecimal |
| efectivo_esperado | DECIMAL(14,2) | NULL | base + ventas efectivo − gastos efectivo | BigDecimal |
| efectivo_declarado / transferencia_declarada / tarjeta_declarada | DECIMAL(14,2) | NULL, CHECK ≥ 0 | Valores contados en el arqueo | BigDecimal |
| diferencia | DECIMAL(14,2) | NULL | Sobrante (+) o faltante (−) | BigDecimal |
| observaciones | VARCHAR(300) | NULL (obligatoria si \|dif\| > umbral) | Justificación del descuadre | String |
| estado | ENUM('ABIERTA','CERRADA') | NOT NULL, DEFAULT 'ABIERTA'; CHECK cierre | Estado del turno | @Enumerated(EnumType.STRING) EstadoCaja |
| version | INT | NOT NULL, DEFAULT 0 (nuevo en V2) | Evita doble cierre concurrente | @Version Integer |

**Índices:** ix_caja_estado_apertura (estado, fecha_apertura) – caja ABIERTA

## 6. `usuarios` → `@Entity Usuario`

Personas que ingresan al sistema (Administrador / Trabajador).

| Columna | Tipo SQL | Restricciones | Descripción | Mapeo JPA |
|---|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Identificador | @Id Long |
| rol_id | INT | NOT NULL, FK → roles(id) RESTRICT | Rol asignado | @ManyToOne(fetch=LAZY) Rol |
| tipo_documento | ENUM('CC','CE','PPT','PAS') | NOT NULL | Tipo de documento | @Enumerated(EnumType.STRING) |
| numero_documento | VARCHAR(15) | NOT NULL, UNIQUE | Documento de identidad | @Column(unique=true) |
| nombres / apellidos | VARCHAR(60) | NOT NULL | Nombre completo | String |
| email | VARCHAR(120) | NOT NULL, UNIQUE | Usuario de inicio de sesión | @Column(unique=true, length=120) |
| telefono | VARCHAR(10) | NULL | Celular | String |
| password_hash | VARCHAR(100) | NOT NULL | Hash BCrypt (costo 12) | String — nunca sale en un DTO |
| estado | ENUM('ACTIVO','INACTIVO') | NOT NULL, DEFAULT 'ACTIVO' | Habilitación de la cuenta | @Enumerated(EnumType.STRING) EstadoUsuario |
| intentos_fallidos | TINYINT | NOT NULL, DEFAULT 0 | Contador para bloqueo (RF-02) | int |
| bloqueado_hasta | DATETIME | NULL | Fin del bloqueo de 15 min | LocalDateTime |
| ultimo_acceso | DATETIME | NULL | Último inicio de sesión | LocalDateTime |

**Índices:** UNIQUE (email), UNIQUE (numero_documento); ix_usuarios_rol_estado (rol_id, estado) – administradores activos

## 7. `clientes` → `@Entity Cliente`

Clientas de la tienda (Ley 1581 de 2012).

| Columna | Tipo SQL | Restricciones | Descripción | Mapeo JPA |
|---|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Identificador | @Id Long |
| tipo_documento | ENUM('CC','CE','TI','NIT','PAS') | NOT NULL | Tipo de documento | @Enumerated(EnumType.STRING) |
| numero_documento | VARCHAR(15) | NOT NULL, UNIQUE | Documento (búsqueda en POS) | @Column(unique=true) |
| nombres / apellidos | VARCHAR(60) | NOT NULL / NULL | Nombre de la clienta | String |
| telefono / email | VARCHAR(10) / VARCHAR(120) | NULL | Datos de contacto | String |
| fecha_nacimiento | DATE | NULL | Para campañas de cumpleaños | LocalDate |
| acepta_datos | BOOLEAN | NOT NULL, DEFAULT FALSE | Autorización de tratamiento de datos (RNF-10) | boolean + @AssertTrue en el DTO |

**Índices:** ix_clientes_nombres (nombres, apellidos) – búsqueda por nombre

## 8. `movimientos_inventario` → `@Entity MovimientoInventario`

Kárdex inmutable: toda variación de stock queda registrada.

| Columna | Tipo SQL | Restricciones | Descripción | Mapeo JPA |
|---|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | Identificador | @Id Long |
| producto_id | BIGINT | NOT NULL, FK → productos(id) | Producto afectado | @ManyToOne(fetch=LAZY) updatable=false |
| usuario_id | BIGINT | NOT NULL, FK → usuarios(id) | Responsable | @ManyToOne(fetch=LAZY) |
| tipo | ENUM('ENTRADA_COMPRA','SALIDA_VENTA','AJUSTE_POSITIVO','AJUSTE_NEGATIVO','REINTEGRO_ANULACION') | NOT NULL | Naturaleza del movimiento | @Enumerated(EnumType.STRING) TipoMovimiento |
| cantidad | INT | NOT NULL, CHECK > 0 | Unidades movidas | int |
| stock_anterior / stock_resultante | INT | NOT NULL; resultante CHECK ≥ 0 | Saldo antes y después | int |
| motivo | VARCHAR(200) | NULL | Justificación (ajustes y anulaciones) | String |
| venta_id / compra_id | BIGINT | NULL, FK → ventas / compras | Documento origen | @ManyToOne(fetch=LAZY) |
| fecha | DATETIME | NOT NULL | Momento del movimiento | LocalDateTime |

**Índices:** ix_mov_producto_fecha (producto_id, fecha) – consulta del kárdex

## 9. `metodos_pago` → `@Entity MetodoPago`

Catálogo de medios de pago.

| Columna | Tipo SQL | Restricciones | Descripción | Mapeo JPA |
|---|---|---|---|---|
| id | INT | PK, AUTO_INCREMENT | Identificador | @Id Integer |
| nombre | VARCHAR(30) | NOT NULL, UNIQUE | EFECTIVO, TRANSFERENCIA, TARJETA_DEBITO, TARJETA_CREDITO | @Enumerated(EnumType.STRING) TipoMetodoPago |
| requiere_referencia | BOOLEAN | NOT NULL, DEFAULT FALSE | Exige número de aprobación | boolean |
| activo | BOOLEAN | NOT NULL, DEFAULT TRUE | Disponible en el POS | boolean |

**Índices:** UNIQUE (nombre)

## Otras tablas mapeadas

Las demás tablas del modelo también tienen su clase `@Entity` en el código fuente:

- `roles` → `Rol`
- `tokens_recuperacion` → `TokenRecuperacion`
- `categorias` → `Categoria`
- `marcas` → `Marca`
- `proveedores` → `Proveedor`
- `producto_proveedor` → `ProductoProveedor (@EmbeddedId + @MapsId)`
- `compras` → `Compra`
- `detalle_compra` → `DetalleCompra`
- `gastos` → `Gasto`
- `configuracion_negocio` → `ConfiguracionNegocio`
- `auditoria` → `Auditoria (@JdbcTypeCode(SqlTypes.JSON))`
