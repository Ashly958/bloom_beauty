# ADR-001: Monolito con Arquitectura en Capas para el backend

- **Estado:** Aceptado
- **Fecha:** 2026-11-25
- **Decisores:** Equipo Bloom Beauty
- **Requerimientos relacionados:** RNF-01, RNF-02, RNF-03, RNF-07, RNF-09

## Contexto y problema

Bloom Beauty es una sola tienda con dos roles (Administrador y Trabajador), unos 20 usuarios concurrentes como máximo y 5.000 productos. La operación más crítica (HU-01) toca en una misma transacción la venta, sus pagos, el stock, el kárdex, la caja y el consecutivo de facturación. El equipo es pequeño, tiene un semestre para construir el sistema y el despliegue será en un único VPS. Hay que elegir un estilo que garantice consistencia transaccional, sea fácil de mantener y se pueda explicar y probar dentro del curso.

## Alternativas evaluadas

| Alternativa | A favor | En contra |
|---|---|---|
| **A. Monolito en capas** (controller → service → repository → domain) | Una sola transacción ACID por caso de uso; estructura que Spring Boot propone por defecto; despliegue de un único JAR; curva de aprendizaje baja. | Riesgo de acoplamiento si las capas no se respetan; escala como un bloque. |
| **B. Arquitectura hexagonal / Clean Architecture** | Dominio totalmente aislado de frameworks; adaptadores intercambiables. | Más interfaces, puertos y mapeos (doble modelo dominio/JPA) para un dominio pequeño; sobrecosto para el equipo y el plazo. |
| **C. Microservicios** (ventas, inventario, caja) | Escalado y despliegue independientes. | La venta necesitaría transacciones distribuidas (Saga) entre stock y caja; más infraestructura (gateway, descubrimiento, varias BD); injustificable para una tienda. |

## Decisión tomada

Se adopta la **alternativa A: monolito con arquitectura en capas**, organizado en los paquetes `controller`, `service` (interfaces + `impl`), `repository`, `domain`, `dto`, `mapper`, `exception`, `event`, `security` y `config`. Se toman dos ideas de la arquitectura limpia sin pagar su costo completo: (1) las reglas de negocio viven en métodos de las entidades (`Venta.validarPagosCubrenTotal()`, `Producto.descontarStock()`, `SesionCaja.cerrar()`), y (2) los servicios dependen de interfaces (`InventarioService`, `NotificacionService`, `ProcesadorPago`) inyectadas por constructor.

Reglas de dependencia: un controlador solo llama servicios; un servicio llama repositorios, dominio y otros servicios; nadie fuera de `repository` usa `EntityManager`; las entidades nunca salen del backend (se exponen DTOs).

## Consecuencias

**Positivas**
- La venta completa es una transacción ACID local (`@Transactional`): si falla el stock o el pago, se revierte todo (HU-01 E-5).
- Un solo artefacto (`bloom-beauty-api.jar`) en un contenedor Docker simplifica el despliegue y el monitoreo.
- La separación por capas permite probar el dominio sin Spring (`VentaDominioTest`) y los controladores con `@WebMvcTest`.

**Negativas / trade-offs**
- Todo el backend escala junto; si un día hubiera varias sedes con alta carga habría que extraer módulos.
- Las entidades JPA cumplen también el rol de modelo de dominio, por lo que el dominio depende de anotaciones de Jakarta Persistence.
- Requiere disciplina (revisión de código) para que ningún controlador acceda a repositorios directamente.
