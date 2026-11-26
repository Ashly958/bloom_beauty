-- =====================================================
-- Bloom Beauty - Esquema de base de datos (MySQL 8.0)
-- Parcial 1 - Modelo de datos
-- =====================================================
DROP DATABASE IF EXISTS bloom_beauty;
CREATE DATABASE bloom_beauty CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE bloom_beauty;

-- Roles del sistema (ADMINISTRADOR, TRABAJADOR).
CREATE TABLE roles (
  id INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(30) NOT NULL UNIQUE,
  descripcion VARCHAR(150) NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Personas que ingresan al sistema.
CREATE TABLE usuarios (
  id BIGINT NOT NULL AUTO_INCREMENT,
  rol_id INT NOT NULL,
  tipo_documento ENUM('CC','CE','PPT','PAS') NOT NULL,
  numero_documento VARCHAR(15) NOT NULL UNIQUE,
  nombres VARCHAR(60) NOT NULL,
  apellidos VARCHAR(60) NOT NULL,
  email VARCHAR(120) NOT NULL UNIQUE,
  telefono VARCHAR(10) NULL,
  password_hash VARCHAR(100) NOT NULL,
  estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  intentos_fallidos TINYINT NOT NULL DEFAULT 0,
  bloqueado_hasta DATETIME NULL,
  ultimo_acceso DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Tokens de un solo uso para restablecer contraseña.
CREATE TABLE tokens_recuperacion (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  token CHAR(64) NOT NULL UNIQUE,
  expira_en DATETIME NOT NULL,
  usado BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Categorías del catálogo (Rostro, Ojos, Labios, Uñas, Skincare, Accesorios).
CREATE TABLE categorias (
  id INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL UNIQUE,
  descripcion VARCHAR(200) NULL,
  inactivo BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Marcas comercializadas.
CREATE TABLE marcas (
  id INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(60) NOT NULL UNIQUE,
  inactivo BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Proveedores de mercancía.
CREATE TABLE proveedores (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nit VARCHAR(15) NOT NULL UNIQUE,
  razon_social VARCHAR(120) NOT NULL,
  nombre_contacto VARCHAR(80) NULL,
  telefono VARCHAR(10) NOT NULL,
  email VARCHAR(120) NULL,
  direccion VARCHAR(150) NULL,
  ciudad VARCHAR(60) NULL,
  inactivo BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Catálogo de productos de belleza.
CREATE TABLE productos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  codigo_barras VARCHAR(20) NOT NULL UNIQUE,
  nombre VARCHAR(100) NOT NULL,
  descripcion VARCHAR(500) NULL,
  categoria_id INT NOT NULL,
  marca_id INT NOT NULL,
  tono VARCHAR(40) NULL,
  precio_costo DECIMAL(12,2) NOT NULL,
  precio_venta DECIMAL(12,2) NOT NULL,
  porcentaje_iva DECIMAL(5,2) NOT NULL DEFAULT 19.00,
  stock_actual INT NOT NULL DEFAULT 0,
  stock_minimo INT NOT NULL DEFAULT 5,
  fecha_vencimiento DATE NULL,
  imagen_url VARCHAR(255) NULL,
  inactivo BOOLEAN NOT NULL DEFAULT FALSE,
  version INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT ck_productos_precio_costo CHECK (precio_costo >= 0),
  CONSTRAINT ck_productos_precio_venta CHECK (precio_venta > 0),
  CONSTRAINT ck_productos_stock_actual CHECK (stock_actual >= 0),
  CONSTRAINT ck_productos_stock_minimo CHECK (stock_minimo >= 0)
) ENGINE=InnoDB;

-- Tabla intermedia N:M entre productos y proveedores.
CREATE TABLE producto_proveedor (
  producto_id BIGINT NOT NULL,
  proveedor_id BIGINT NOT NULL,
  costo_referencia DECIMAL(12,2) NULL,
  es_principal BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (producto_id, proveedor_id),
  CONSTRAINT ck_producto_proveedor_costo_referencia CHECK (costo_referencia >= 0)
) ENGINE=InnoDB;

-- Clientes que compran en la tienda.
CREATE TABLE clientes (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tipo_documento ENUM('CC','CE','TI','NIT','PAS') NOT NULL,
  numero_documento VARCHAR(15) NOT NULL UNIQUE,
  nombres VARCHAR(60) NOT NULL,
  apellidos VARCHAR(60) NULL,
  telefono VARCHAR(10) NULL,
  email VARCHAR(120) NULL,
  fecha_nacimiento DATE NULL,
  acepta_datos BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Entradas de mercancía (facturas de proveedor).
CREATE TABLE compras (
  id BIGINT NOT NULL AUTO_INCREMENT,
  proveedor_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  numero_factura_proveedor VARCHAR(30) NOT NULL,
  fecha_compra DATE NOT NULL,
  total DECIMAL(14,2) NOT NULL,
  observaciones VARCHAR(300) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT ck_compras_total CHECK (total >= 0)
) ENGINE=InnoDB;

-- Productos incluidos en cada compra.
CREATE TABLE detalle_compra (
  id BIGINT NOT NULL AUTO_INCREMENT,
  compra_id BIGINT NOT NULL,
  producto_id BIGINT NOT NULL,
  cantidad INT NOT NULL,
  costo_unitario DECIMAL(12,2) NOT NULL,
  subtotal DECIMAL(14,2) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT ck_detalle_compra_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_detalle_compra_costo_unitario CHECK (costo_unitario >= 0)
) ENGINE=InnoDB;

-- Turno de caja: apertura, arqueo y cierre diario (HU-02).
CREATE TABLE sesiones_caja (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_apertura_id BIGINT NOT NULL,
  usuario_cierre_id BIGINT NULL,
  fecha_apertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_cierre DATETIME NULL,
  monto_inicial DECIMAL(12,2) NOT NULL,
  total_ventas_efectivo DECIMAL(14,2) NOT NULL DEFAULT 0,
  total_ventas_transferencia DECIMAL(14,2) NOT NULL DEFAULT 0,
  total_ventas_tarjeta DECIMAL(14,2) NOT NULL DEFAULT 0,
  total_gastos_efectivo DECIMAL(14,2) NOT NULL DEFAULT 0,
  efectivo_esperado DECIMAL(14,2) NULL,
  efectivo_declarado DECIMAL(14,2) NULL,
  transferencia_declarada DECIMAL(14,2) NULL,
  tarjeta_declarada DECIMAL(14,2) NULL,
  diferencia DECIMAL(14,2) NULL,
  observaciones VARCHAR(300) NULL,
  estado ENUM('ABIERTA','CERRADA') NOT NULL DEFAULT 'ABIERTA',
  PRIMARY KEY (id),
  CONSTRAINT ck_sesiones_caja_monto_inicial CHECK (monto_inicial >= 0),
  CONSTRAINT ck_sesiones_caja_efectivo_declarado CHECK (efectivo_declarado >= 0),
  CONSTRAINT ck_sesiones_caja_transferencia_declarada CHECK (transferencia_declarada >= 0),
  CONSTRAINT ck_sesiones_caja_tarjeta_declarada CHECK (tarjeta_declarada >= 0)
) ENGINE=InnoDB;

-- Catálogo de medios de pago.
CREATE TABLE metodos_pago (
  id INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(30) NOT NULL UNIQUE,
  requiere_referencia BOOLEAN NOT NULL DEFAULT FALSE,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Encabezado de la factura de venta (POS).
CREATE TABLE ventas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  numero_factura VARCHAR(20) NOT NULL UNIQUE,
  cliente_id BIGINT NULL,
  usuario_id BIGINT NOT NULL,
  sesion_caja_id BIGINT NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  subtotal DECIMAL(14,2) NOT NULL,
  descuento_total DECIMAL(14,2) NOT NULL DEFAULT 0,
  iva_total DECIMAL(14,2) NOT NULL DEFAULT 0,
  total DECIMAL(14,2) NOT NULL,
  estado ENUM('PAGADA','ANULADA') NOT NULL DEFAULT 'PAGADA',
  motivo_anulacion VARCHAR(200) NULL,
  anulada_por BIGINT NULL,
  fecha_anulacion DATETIME NULL,
  PRIMARY KEY (id),
  CONSTRAINT ck_ventas_subtotal CHECK (subtotal >= 0),
  CONSTRAINT ck_ventas_total CHECK (total > 0)
) ENGINE=InnoDB;

-- Líneas de producto de cada venta.
CREATE TABLE detalle_venta (
  id BIGINT NOT NULL AUTO_INCREMENT,
  venta_id BIGINT NOT NULL,
  producto_id BIGINT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(12,2) NOT NULL,
  porcentaje_descuento DECIMAL(5,2) NOT NULL DEFAULT 0,
  subtotal DECIMAL(14,2) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT ck_detalle_venta_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_detalle_venta_precio_unitario CHECK (precio_unitario > 0),
  CONSTRAINT ck_detalle_venta_porcentaje_descuento CHECK (porcentaje_descuento BETWEEN 0 AND 30)
) ENGINE=InnoDB;

-- Pagos de una venta; permite pago mixto (RNF-04).
CREATE TABLE pagos_venta (
  id BIGINT NOT NULL AUTO_INCREMENT,
  venta_id BIGINT NOT NULL,
  metodo_pago_id INT NOT NULL,
  monto DECIMAL(14,2) NOT NULL,
  monto_recibido DECIMAL(14,2) NULL,
  cambio DECIMAL(14,2) NULL DEFAULT 0,
  referencia VARCHAR(40) NULL,
  PRIMARY KEY (id),
  CONSTRAINT ck_pagos_venta_monto CHECK (monto > 0)
) ENGINE=InnoDB;

-- Kárdex: toda variación de stock queda registrada.
CREATE TABLE movimientos_inventario (
  id BIGINT NOT NULL AUTO_INCREMENT,
  producto_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  tipo ENUM('ENTRADA_COMPRA','SALIDA_VENTA','AJUSTE_POSITIVO','AJUSTE_NEGATIVO','REINTEGRO_ANULACION') NOT NULL,
  cantidad INT NOT NULL,
  stock_anterior INT NOT NULL,
  stock_resultante INT NOT NULL,
  motivo VARCHAR(200) NULL,
  venta_id BIGINT NULL,
  compra_id BIGINT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT ck_movimientos_inventario_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_movimientos_inventario_stock_resultante CHECK (stock_resultante >= 0)
) ENGINE=InnoDB;

-- Egresos del negocio (contabilidad básica).
CREATE TABLE gastos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  sesion_caja_id BIGINT NULL,
  metodo_pago_id INT NOT NULL,
  categoria ENUM('ARRIENDO','SERVICIOS','NOMINA','INSUMOS','PUBLICIDAD','TRANSPORTE','OTRO') NOT NULL,
  descripcion VARCHAR(200) NOT NULL,
  monto DECIMAL(14,2) NOT NULL,
  fecha DATE NOT NULL,
  soporte_url VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT ck_gastos_monto CHECK (monto > 0)
) ENGINE=InnoDB;

-- Parámetros generales (un único registro).
CREATE TABLE configuracion_negocio (
  id TINYINT NOT NULL,
  nombre_comercial VARCHAR(100) NOT NULL,
  nit VARCHAR(15) NOT NULL,
  direccion VARCHAR(150) NOT NULL,
  telefono VARCHAR(10) NOT NULL,
  prefijo_factura VARCHAR(5) NOT NULL DEFAULT 'BB',
  consecutivo_actual INT NOT NULL DEFAULT 0,
  descuento_maximo_trabajador DECIMAL(5,2) NOT NULL DEFAULT 10.00,
  umbral_diferencia_caja DECIMAL(12,2) NOT NULL DEFAULT 5000.00,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Bitácora de acciones sensibles.
CREATE TABLE auditoria (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NULL,
  accion VARCHAR(40) NOT NULL,
  entidad VARCHAR(40) NOT NULL,
  entidad_id BIGINT NULL,
  detalle JSON NULL,
  ip VARCHAR(45) NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Restricciones adicionales
ALTER TABLE compras ADD CONSTRAINT uq_compra_factura UNIQUE (proveedor_id, numero_factura_proveedor);
ALTER TABLE detalle_venta ADD CONSTRAINT uq_venta_producto UNIQUE (venta_id, producto_id);
ALTER TABLE detalle_compra ADD CONSTRAINT uq_compra_producto UNIQUE (compra_id, producto_id);
ALTER TABLE configuracion_negocio ADD CONSTRAINT ck_config_unica CHECK (id = 1);
ALTER TABLE ventas ADD CONSTRAINT ck_venta_anulacion CHECK (estado = 'PAGADA' OR motivo_anulacion IS NOT NULL);
ALTER TABLE sesiones_caja ADD CONSTRAINT ck_caja_cierre CHECK (estado = 'ABIERTA' OR fecha_cierre IS NOT NULL);

-- Llaves foráneas
ALTER TABLE usuarios ADD CONSTRAINT fk_usuarios_rol_id FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE tokens_recuperacion ADD CONSTRAINT fk_tokens_recuperacion_usuario_id FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE productos ADD CONSTRAINT fk_productos_categoria_id FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE productos ADD CONSTRAINT fk_productos_marca_id FOREIGN KEY (marca_id) REFERENCES marcas(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE producto_proveedor ADD CONSTRAINT fk_producto_proveedor_producto_id FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE producto_proveedor ADD CONSTRAINT fk_producto_proveedor_proveedor_id FOREIGN KEY (proveedor_id) REFERENCES proveedores(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE compras ADD CONSTRAINT fk_compras_proveedor_id FOREIGN KEY (proveedor_id) REFERENCES proveedores(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE compras ADD CONSTRAINT fk_compras_usuario_id FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE detalle_compra ADD CONSTRAINT fk_detalle_compra_compra_id FOREIGN KEY (compra_id) REFERENCES compras(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE detalle_compra ADD CONSTRAINT fk_detalle_compra_producto_id FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE sesiones_caja ADD CONSTRAINT fk_sesiones_caja_usuario_apertura_id FOREIGN KEY (usuario_apertura_id) REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE sesiones_caja ADD CONSTRAINT fk_sesiones_caja_usuario_cierre_id FOREIGN KEY (usuario_cierre_id) REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE ventas ADD CONSTRAINT fk_ventas_cliente_id FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE ventas ADD CONSTRAINT fk_ventas_usuario_id FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE ventas ADD CONSTRAINT fk_ventas_sesion_caja_id FOREIGN KEY (sesion_caja_id) REFERENCES sesiones_caja(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE ventas ADD CONSTRAINT fk_ventas_anulada_por FOREIGN KEY (anulada_por) REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE detalle_venta ADD CONSTRAINT fk_detalle_venta_venta_id FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE detalle_venta ADD CONSTRAINT fk_detalle_venta_producto_id FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE pagos_venta ADD CONSTRAINT fk_pagos_venta_venta_id FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE pagos_venta ADD CONSTRAINT fk_pagos_venta_metodo_pago_id FOREIGN KEY (metodo_pago_id) REFERENCES metodos_pago(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE movimientos_inventario ADD CONSTRAINT fk_movimientos_inventario_producto_id FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE movimientos_inventario ADD CONSTRAINT fk_movimientos_inventario_usuario_id FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE movimientos_inventario ADD CONSTRAINT fk_movimientos_inventario_venta_id FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE movimientos_inventario ADD CONSTRAINT fk_movimientos_inventario_compra_id FOREIGN KEY (compra_id) REFERENCES compras(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE gastos ADD CONSTRAINT fk_gastos_usuario_id FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE gastos ADD CONSTRAINT fk_gastos_sesion_caja_id FOREIGN KEY (sesion_caja_id) REFERENCES sesiones_caja(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE gastos ADD CONSTRAINT fk_gastos_metodo_pago_id FOREIGN KEY (metodo_pago_id) REFERENCES metodos_pago(id) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE auditoria ADD CONSTRAINT fk_auditoria_usuario_id FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT ON UPDATE CASCADE;

-- Índices de consulta
CREATE INDEX ix_productos_nombre ON productos(nombre);
CREATE INDEX ix_productos_stock ON productos(stock_actual, stock_minimo);
CREATE INDEX ix_ventas_fecha ON ventas(fecha);
CREATE INDEX ix_mov_producto_fecha ON movimientos_inventario(producto_id, fecha);
CREATE INDEX ix_gastos_fecha ON gastos(fecha);
CREATE INDEX ix_auditoria_fecha ON auditoria(fecha);

-- Datos iniciales
INSERT INTO roles (nombre, descripcion) VALUES
  ('ADMINISTRADOR','Gestiona catálogo, usuarios, caja, reportes y contabilidad'),
  ('TRABAJADOR','Factura ventas y consulta productos e inventario');
INSERT INTO metodos_pago (nombre, requiere_referencia) VALUES
  ('EFECTIVO', FALSE), ('TRANSFERENCIA', TRUE), ('TARJETA_DEBITO', TRUE), ('TARJETA_CREDITO', TRUE);
INSERT INTO categorias (nombre, descripcion) VALUES
  ('Rostro','Bases, correctores, polvos, rubores'), ('Ojos','Sombras, pestañinas, delineadores'),
  ('Labios','Labiales, brillos, delineadores de labios'), ('Uñas','Esmaltes y tratamientos'),
  ('Skincare','Limpiadores, sérums, hidratantes, protector solar'), ('Accesorios','Brochas, esponjas, cosmetiqueras');
INSERT INTO marcas (nombre) VALUES ('Vogue'), ('Samy'), ('Maybelline'), ('L''Oréal'), ('Ésika'), ('Trendy');
INSERT INTO configuracion_negocio (id, nombre_comercial, nit, direccion, telefono) VALUES
  (1, 'Bloom Beauty', '901234567-8', 'Calle 10 # 5-23, Local 4', '3001234567');
INSERT INTO productos (codigo_barras, nombre, categoria_id, marca_id, tono, precio_costo, precio_venta, stock_actual, stock_minimo) VALUES
  ('7702044123456','Polvo Traslúcido', 1, 1, 'Universal', 14500, 26900, 1, 5),
  ('7702044123463','Corrector Alta Cobertura', 1, 2, 'Beige 02', 11000, 21900, 18, 5),
  ('7702044123470','Labial Mate Larga Duración', 3, 6, 'Rojo Pasión', 8500, 17500, 32, 8),
  ('7702044123487','Pestañina Volumen Extremo', 2, 3, 'Negro', 19000, 38900, 12, 4),
  ('7702044123494','Protector Solar FPS 50', 5, 4, NULL, 32000, 59900, 9, 3);
