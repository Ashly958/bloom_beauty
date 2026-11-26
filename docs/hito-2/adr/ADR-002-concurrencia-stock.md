# ADR-002: Bloqueo optimista para el stock y bloqueo pesimista para el consecutivo

- **Estado:** Aceptado
- **Fecha:** 2026-11-25
- **Requerimientos relacionados:** RF-17, RF-18, RNF-02, HU-01 (E-2), HU-02 (E-3)

## Contexto y problema

Dos trabajadoras pueden cobrar al mismo tiempo la última unidad de un producto. RNF-02 exige atender a la primera y devolver **409 "Stock agotado"** a la segunda, con respuesta menor a 500 ms con 50 peticiones/s. Además, el número de factura BB-XXXXXX debe ser consecutivo y nunca repetirse (RF-18), y dos administradores no deben poder cerrar la misma caja (HU-02 E-3).

## Alternativas evaluadas

| Alternativa | A favor | En contra |
|---|---|---|
| **A. Bloqueo pesimista** (`SELECT … FOR UPDATE`) sobre cada producto vendido | Nunca hay conflicto al confirmar. | Bloquea filas durante toda la venta; con carritos de varios productos aumenta la espera y el riesgo de *deadlock*. |
| **B. Bloqueo optimista** con columna `version` (`@Version`) | Sin bloqueos mientras se arma la venta; el conflicto (raro) se detecta en el `UPDATE … WHERE version = ?`. | El perdedor debe reintentar; requiere traducir la excepción a 409. |
| **C. `UPDATE productos SET stock = stock − n WHERE stock ≥ n`** atómico | Muy rápido. | La lógica de stock sale del dominio y queda en SQL; no protege otros campos ni ediciones concurrentes del producto. |
| **D. Tabla `AUTO_INCREMENT` o UUID** para el consecutivo | Sin bloqueos. | AUTO_INCREMENT deja huecos al hacer rollback; UUID no cumple el formato BB-000001. |

## Decisión tomada

1. **Stock:** bloqueo optimista (B). `Producto` tiene `@Version Integer version`. `VentaServiceImpl` descuenta stock con `Producto.descontarStock()` y fuerza `flush()`; si otra transacción ya cambió la versión, Hibernate lanza `ObjectOptimisticLockingFailureException`, la transacción completa hace *rollback* y `GlobalExceptionHandler` responde **409 STOCK_AGOTADO**. La validación previa `cantidad > stockActual` lanza `StockAgotadoException` (también 409) para el caso no concurrente.
2. **Consecutivo:** bloqueo pesimista puntual (A) solo sobre la fila única de `configuracion_negocio` (`@Lock(PESSIMISTIC_WRITE)` en `NumeradorFacturas`). Se toma al final de la venta, por lo que el bloqueo dura milisegundos, y si la venta falla el consecutivo no se consume.
3. **Cierre de caja:** `@Version` en `SesionCaja` (columna agregada en la migración V2).

## Consecuencias

**Positivas**
- No hay bloqueos durante el armado del carrito; el rendimiento cumple RNF-02.
- La regla de negocio del stock sigue en el dominio y es probable en pruebas unitarias.
- Los números de factura son estrictamente consecutivos y sin duplicados.

**Negativas / trade-offs**
- Ante un conflicto, la trabajadora debe actualizar el carrito y volver a cobrar (la SPA lo hace automáticamente al recibir 409).
- El bloqueo sobre `configuracion_negocio` serializa la confirmación final de ventas; aceptable para el volumen de una tienda, pero sería un cuello de botella con muchas cajas simultáneas.
- Hay que mantener la columna `version` en sincronía en cualquier script manual sobre `productos`.
