# Hito 2 – Arquitectura, C4, UML, Persistencia y ADRs

Anexos técnicos del Parcial 2 de Ingeniería de Software II. Todos los diagramas son **diagramación como código** (Mermaid.js); las imágenes de `img/` se generan a partir de los `.mmd`.

```
docs/hito-2/
├── c4/
│   ├── 01-contexto.mmd             Nivel 1 – Contexto
│   ├── 02-contenedores.mmd         Nivel 2 – Contenedores
│   └── 03-componentes-backend.mmd  Nivel 3 – Componentes del backend
├── uml/
│   ├── 01-clases-dominio.mmd               Clases de dominio
│   ├── 02-secuencia-registrar-venta.mmd    Secuencia full stack HU-01 (alt/opt, 400/401/409/422)
│   └── 03-despliegue.mmd                   Despliegue (VPS + Docker)
├── adr/
│   ├── ADR-001-estilo-arquitectonico.md
│   ├── ADR-002-concurrencia-stock.md
│   ├── ADR-003-autenticacion-jwt.md
│   └── ADR-004-motor-bd-y-despliegue.md
├── diccionario-datos.md
└── img/                            PNG renderizados
```

## Regenerar las imágenes

```bash
npm install -g @mermaid-js/mermaid-cli
for f in c4/*.mmd uml/*.mmd; do mmdc -i "$f" -o "img/$(basename "$f" .mmd).png" -w 2400 -s 2 -b white; done
```

GitHub también dibuja los `.mmd` directamente si se pegan dentro de un bloque ```` ```mermaid ````.

## Trazabilidad con el Parcial 1

| Artefacto | HU / RF / RNF |
|---|---|
| Secuencia `registrar venta` | HU-01 · RF-15 a RF-18 · RNF-02, RNF-04 |
| `@Version` en `Producto` y `SesionCaja` | RNF-02 · HU-02 E-3 |
| `@SQLDelete` + `@SQLRestriction` en `Producto` | RF-07 · RNF-03 |
| `@PreAuthorize` + JWT | RF-01 · RNF-01 · RNF-06 |
| Índices compuestos (V2) | RNF-07 |
| `acepta_datos` + `@AssertTrue` | RNF-10 |
