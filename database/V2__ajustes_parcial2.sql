-- =====================================================
-- Bloom Beauty - Migración V2 (Parcial 2)
-- Se ejecuta después de V1__esquema_inicial.sql (script del Parcial 1).
-- Alinea el esquema con el mapeo JPA y agrega índices compuestos
-- para las consultas más frecuentes (RNF-07).
-- =====================================================
USE bloom_beauty;

-- Bloqueo optimista del cierre de caja (HU-02 E-3: "La caja ya fue cerrada")
ALTER TABLE sesiones_caja ADD COLUMN version INT NOT NULL DEFAULT 0;

-- Índices compuestos para consultas frecuentes
CREATE INDEX ix_productos_categoria_marca ON productos (categoria_id, marca_id, inactivo);  -- filtro del catálogo
CREATE INDEX ix_productos_vencimiento     ON productos (fecha_vencimiento);                  -- alerta de vencimiento
CREATE INDEX ix_usuarios_rol_estado       ON usuarios (rol_id, estado);                      -- admins activos
CREATE INDEX ix_clientes_nombres          ON clientes (nombres, apellidos);                  -- búsqueda en POS
CREATE INDEX ix_caja_estado_apertura      ON sesiones_caja (estado, fecha_apertura);         -- caja ABIERTA
CREATE INDEX ix_ventas_caja_estado        ON ventas (sesion_caja_id, estado);                -- arqueo de caja
CREATE INDEX ix_ventas_cliente_fecha      ON ventas (cliente_id, fecha);                     -- historial de clienta
CREATE INDEX ix_detalle_venta_producto    ON detalle_venta (producto_id);                    -- más vendidos
CREATE INDEX ix_gastos_fecha_categoria    ON gastos (fecha, categoria);                      -- reporte de gastos
CREATE INDEX ix_auditoria_entidad         ON auditoria (entidad, entidad_id);                -- trazabilidad

-- Reemplaza índices simples que quedan cubiertos por los compuestos
DROP INDEX ix_gastos_fecha ON gastos;

-- El usuario de aplicación (bloom_app) lo crea el contenedor MySQL a partir de
-- MYSQL_USER / MYSQL_PASSWORD del archivo .env; nunca se escribe la clave aquí.
