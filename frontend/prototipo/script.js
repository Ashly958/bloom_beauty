/**
 * Bloom Beauty - Figma Design System
 * Lógica de navegación entre vistas, roles de login y modales
 */

// Estado del Carrito
const estadoCarrito = {
  items: [
    { nombre: 'Paleta Rose Gold', precio: 45000, desc: '18 tonos pigmentados', foto: 'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?auto=format&fit=crop&w=120&q=80' },
    { nombre: 'Labial Velvet Bloom', precio: 28000, desc: 'Tono cereza aterciopelado', foto: 'https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=120&q=80' }
  ]
};

// 1. Selector de Vistas de la Barra Demo
const tabs = document.querySelectorAll('.demo-tab[data-vista]');
const secciones = document.querySelectorAll('.vista-seccion');

tabs.forEach(tab => {
  tab.addEventListener('click', () => {
    const vistaId = tab.dataset.vista;
    
    // Actualizar tabs activos
    tabs.forEach(t => t.classList.remove('is-active'));
    tab.classList.add('is-active');

    // Mostrar sección correspondiente
    secciones.forEach(sec => {
      if (sec.id === `vista-${vistaId}`) {
        sec.classList.add('is-active');
      } else {
        sec.classList.remove('is-active');
      }
    });

    window.scrollTo({ top: 0, behavior: 'smooth' });
  });
});

// 2. Selector de Roles en Login (Figma)
const roles = document.querySelectorAll('.btn-rol');
const loginSaludo = document.getElementById('login-saludo-txt');
const loginImg = document.getElementById('login-img');

const rolesData = {
  cliente: {
    saludo: 'Bienvenido a Bloom Beauty',
    img: 'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?auto=format&fit=crop&w=800&q=80'
  },
  colaborador: {
    saludo: 'Bienvenido colaborador',
    img: 'https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?auto=format&fit=crop&w=800&q=80'
  },
  admin: {
    saludo: 'Bienvenido Admin',
    img: 'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?auto=format&fit=crop&w=800&q=80'
  }
};

roles.forEach(btn => {
  btn.addEventListener('click', () => {
    roles.forEach(b => b.classList.remove('is-active'));
    btn.classList.add('is-active');

    const rol = btn.dataset.rol;
    if (rolesData[rol]) {
      loginSaludo.textContent = rolesData[rol].saludo;
      loginImg.src = rolesData[rol].img;
    }
  });
});

// 3. Funciones de Apertura y Cierre de Modales
function abrirModal(modalId, tituloExtra) {
  const modal = document.getElementById(modalId);
  if (!modal) return;

  if (tituloExtra && modalId === 'modal-editar-prod') {
    const tit = document.getElementById('modal-editar-prod-tit');
    const campo = document.getElementById('campo-prod-nombre');
    if (tit) tit.textContent = tituloExtra.startsWith('Nuevo') ? 'AGREGAR PRODUCTO' : 'EDITAR PRODUCTO';
    if (campo && !tituloExtra.startsWith('Nuevo')) campo.value = tituloExtra;
  }

  if (tituloExtra && modalId === 'modal-editar-colab') {
    const tit = document.getElementById('modal-colab-tit');
    const campo = document.getElementById('campo-colab-nombre');
    if (tit) tit.textContent = tituloExtra === 'Nuevo' ? 'AGREGAR COLABORADOR' : 'EDITAR COLABORADOR';
    if (campo && tituloExtra !== 'Nuevo') campo.value = tituloExtra;
  }

  modal.classList.add('is-open');
  document.body.style.overflow = 'hidden';
}

function cerrarModales() {
  document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('is-open'));
  document.body.style.overflow = '';
}

// Cerrar modales al hacer clic fuera del cuadro blanco
document.querySelectorAll('.modal-overlay').forEach(overlay => {
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) {
      cerrarModales();
    }
  });
});

// Cerrar modales con tecla Escape
window.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    cerrarModales();
  }
});

// Botones de Carrito
const btnTiendaCarro = document.getElementById('btn-tienda-carro');
const demoAbrirCarro = document.getElementById('demo-abrir-carro');

btnTiendaCarro?.addEventListener('click', () => abrirModal('modal-carrito'));
demoAbrirCarro?.addEventListener('click', () => abrirModal('modal-carrito'));

// 4. Lógica de Carrito de Compras
function formatearPrecio(valor) {
  return '$ ' + valor.toLocaleString('es-CO');
}

function actualizarVistaCarrito() {
  const badge = document.getElementById('tienda-carro-badge');
  const lista = document.getElementById('carrito-lista-items');
  const totalElem = document.getElementById('carrito-total-num');

  if (badge) badge.textContent = estadoCarrito.items.length;

  if (lista) {
    if (estadoCarrito.items.length === 0) {
      lista.innerHTML = '<p style="text-align:center; color:#71717a; padding:20px 0;">Tu carrito está vacío.</p>';
    } else {
      lista.innerHTML = estadoCarrito.items.map((item, idx) => `
        <div class="carrito-item">
          <div class="carrito-item__foto"><img src="${item.foto}" alt="${item.nombre}"></div>
          <div class="carrito-item__info">
            <p class="carrito-item__nombre">${item.nombre}</p>
            <p class="carrito-item__desc">${item.desc}</p>
          </div>
          <span class="carrito-item__precio">${formatearPrecio(item.precio)}</span>
          <button type="button" class="carrito-item__eliminar" onclick="eliminarDelCarrito(${idx})" title="Eliminar">&times;</button>
        </div>
      `).join('');
    }
  }

  const total = estadoCarrito.items.reduce((sum, item) => sum + item.precio, 0);
  if (totalElem) totalElem.textContent = formatearPrecio(total);
}

function agregarAlCarrito(nombre, precio, foto) {
  estadoCarrito.items.push({
    nombre,
    precio,
    desc: 'Producto cosmético Bloom',
    foto: foto || 'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?auto=format&fit=crop&w=120&q=80'
  });
  actualizarVistaCarrito();
  abrirModal('modal-carrito');
}

function eliminarDelCarrito(idx) {
  estadoCarrito.items.splice(idx, 1);
  actualizarVistaCarrito();
}

// Inicializar carrito
actualizarVistaCarrito();
