# Bloom Beauty - Sistema de Diseño Figma en HTML & CSS

Replicación fiel del sistema de diseño en Figma de **Bloom Beauty** (Tienda de maquillaje y cosméticos, login por roles y paneles de administración), extraído del documento `bloom figma.docx`.

## 📁 Estructura del Proyecto

```
bloom-beauty/
├── index.html        # Estructura HTML5 de todas las vistas del Figma
├── styles.css        # Sistema de diseño, tokens, paleta rosa y estética 3D
├── script.js         # Lógica de navegación entre vistas, roles y modales
└── README.md         # Documentación del proyecto
```

## 🚀 Cómo abrir y explorar

1. Haz doble clic en el archivo `index.html` para abrirlo en cualquier navegador (Chrome, Edge, Firefox, Safari).
2. O ejecuta en PowerShell:
   ```powershell
   Start-Process "C:\Users\ashly\bloom-beauty\index.html"
   ```

## 🖥️ Vistas Incluidas del Figma

Usa la **barra de control superior (*Demo Switcher*)** para navegar en vivo entre todas las pantallas diseñadas:

1. 🛍️ **Tienda / Catálogo**:
   - Cabecera rosada con tipografía script *Great Vibes* ("Bienvenida a Bloom Beauty").
   - Subtítulo: *"¡Tu tienda favorita de maquillaje!"*.
   - Rejilla de tarjetas de productos cosméticos con bordes magenta distintivos (`2px solid #f472b6`), imágenes de alta definición y botón de agregar.
2. 🔑 **Login (Roles Dinámicos)**:
   - Fondo cosmético 3D con luces suaves y pedestales de maquillaje.
   - Tarjeta flotante dividida: fotografía editorial curva a la izquierda + formulario y logotipo circular a la derecha.
   - Pestañas interactivas para alternar entre los 3 roles de Figma: **Cliente**, **Colaborador** y **Admin**.
3. 📦 **Admin: Productos**:
   - Cuadrícula de productos con iconos de lápiz (`✎`) para abrir el modal interactivo *"EDITAR PRODUCTO"*.
4. 📊 **Admin: Ventas**:
   - Tarjetas de registro de ventas con colaboradores, ID de venta, total y listado de productos vendidos.
   - Modal interactivo *"AGREGAR VENTA"*.
5. 👥 **Admin: Personal**:
   - Fichas de equipo con biografía, cargo, número de documento y teléfono.
   - Modal interactivo *"EDITAR COLABORADOR"*.
6. 🛒 **Modal Carrito de Compras**:
   - Modal *"Tu carrito de compras"* con ítems seleccionados, eliminación, total en tiempo real y botón de pago.

## 🎨 Tokens de Diseño

- **Tipografías**:
  - `Great Vibes` (Google Fonts): Logotipo y titulares script.
  - `Poppins` (Google Fonts): Textos, botones, formularios y tablas.
- **Paleta de Colores**:
  - Magenta Principal: `#e91e63` / `#f472b6`
  - Cabecera y Fondos Suaves: `#fce7f3` / `#fdf2f8`
  - Tarjetas de Administración: `#fbe4ee`
  - Acentos Dorados: `#d4af37` / `#c5a059`
