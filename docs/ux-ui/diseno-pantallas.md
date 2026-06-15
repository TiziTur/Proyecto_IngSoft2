# Diseño UX/UI — Pantallas principales de Aprovecha!

## 1. Introducción

Este documento describe el diseño de experiencia de usuario (wireframe + mockup) de las
3 pantallas obligatorias para el Trabajo de Campo, evaluadas contra las **10 heurísticas
de usabilidad de Jakob Nielsen** (1994).

Las 3 pantallas conceptuales se mapean a las pantallas ya implementadas en la app
(módulo `feature/products`):

| # | Pantalla conceptual | Pantalla real en Aprovecha |
|---|---|---|
| 1 | Principal / Home | `HomeConsumerScreen` — bloque header + buscador |
| 2 | Funcionalidad Clave (Feed de Ofertas Flash) | `HomeConsumerScreen` — bloque lista de packs (`PackCard`) |
| 3 | Detalle / Formulario / Input crítico | `PackDetailScreen` |

`HomeConsumerScreen` integra en una sola pantalla scrolleable el Home y el Feed de
Ofertas Flash del documento original (header fijo arriba, feed de cards abajo), lo cual
es consistente con el patrón "feed con header" típico de apps mobile de delivery.

### Paleta y tipografía (referencia)

Definidas en `app/src/main/java/com/aprovecha/app/ui/theme/Color.kt`:

| Uso | Color | Hex |
|---|---|---|
| Primario (rescate) | Verde 80 / 60 / 40 | `#2E7D32` / `#388E3C` / `#66BB6A` |
| Container primario | Verde container | `#E8F5E9` |
| Acento urgencia | Naranja 80 / 60 / 40 | `#E65100` / `#EF6C00` / `#FF8F00` |
| Fondo | Background | `#FAFAFA` |
| Superficie | Surface | `#FFFFFF` |
| Texto principal | OnSurface | `#212121` |
| Texto secundario | OnSurfaceVariant | `#757575` |
| Error | Error | `#B00020` |

Tipografía: Material 3 `Typography` por defecto (Roboto), consistente con la guía del
documento original (Inter/Roboto).

---

## 2. Pantalla 1 — Principal / Home

**Objetivo:** permitir al usuario ver de un vistazo su saludo, acceder a la búsqueda y
empezar a explorar packs cercanos sin fricción.

### Wireframe (estructura)

```
┌─────────────────────────────────┐
│ [Saludo "Hola 👋"]      [🔔]     │  ← header
│ "¿Qué vas a rescatar hoy?"       │
├─────────────────────────────────┤
│ [🔍  Buscar packs, comercios...] │  ← search bar
├─────────────────────────────────┤
│ "Destacados 🔥"                  │
│ ┌───────────────────────────┐   │
│ │ [img] Nombre   $$  $$$$    │   │  ← cards (continúan en
│ │       qty · distancia      │   │     pantalla 2)
│ └───────────────────────────┘   │
├─────────────────────────────────┤
│ [INICIO]  [RESERVAS]  [PERFIL]   │  ← tab bar inferior
└─────────────────────────────────┘
```

### Mockup (implementación real)

- **Header**: gradiente vertical Verde 80→60 (`#2E7D32` → `#388E3C`), texto blanco,
  saludo "Hola 👋" + título "¿Qué vas a rescatar hoy?" + ícono de notificaciones.
- **Buscador**: `OutlinedTextField` con bordes redondeados (24dp), ícono de lupa,
  fondo blanco sobre el fondo gris claro de la pantalla (`#FAFAFA`).
- **Tab bar inferior**: fija, fondo blanco, 3 ítems (INICIO activo en pill verde,
  RESERVAS, PERFIL).

### Checklist heurísticas de Nielsen

| # | Heurística | Cómo se cumple |
|---|---|---|
| 1 | Visibilidad del estado del sistema | `CircularProgressIndicator` verde mientras `PacksUiState.Loading`; mensaje de error visible si falla la carga. |
| 2 | Coincidencia sistema/mundo real | Lenguaje cotidiano: "Hola 👋", "¿Qué vas a rescatar hoy?", "Destacados 🔥", íconos estándar (lupa = buscar, campana = notificaciones). |
| 3 | Control y libertad del usuario | El usuario puede borrar/editar libremente el texto de búsqueda en cualquier momento; tab bar siempre disponible para volver a Inicio. |
| 4 | Consistencia y estándares | Tab bar inferior con 3 ítems fijos en toda la sección consumidor; mismo verde (`#2E7D32`) para acciones/estado activo en toda la app. |
| 5 | Prevención de errores | Buscador no bloquea ni navega mientras el usuario escribe; el filtro se aplica de forma local y reversible. |
| 6 | Reconocimiento antes que recuerdo | El usuario no necesita recordar categorías: el feed las muestra directamente como "Destacados"; placeholder del buscador sugiere qué escribir. |
| 7 | Flexibilidad y eficiencia de uso | El buscador filtra en tiempo real sobre la lista ya cargada (sin recargar red), permitiendo tanto exploración casual como búsqueda directa para usuarios frecuentes. |
| 8 | Diseño estético y minimalista | Header + buscador + feed: solo 3 bloques visibles, sin elementos decorativos que compitan por atención. |
| 9 | Ayudar a reconocer/recuperarse de errores | Estado `PacksUiState.Error` muestra el mensaje en el color de error del tema (`MaterialTheme.colorScheme.error`) en lugar de pantalla en blanco. |
| 10 | Ayuda y documentación | Mensaje vacío explícito "No hay packs disponibles cerca" cuando el feed/búsqueda no devuelve resultados, orientando al usuario sobre el estado actual. |

---

## 3. Pantalla 2 — Funcionalidad Clave (Feed de Ofertas Flash)

**Objetivo:** mostrar los packs disponibles cerca del usuario, ordenados y con la
información crítica para decidir rápido (descuento, precio, cantidad, distancia).

### Wireframe (estructura)

```
┌─────────────────────────────────┐
│ "Destacados 🔥"                  │
│ ┌───────────────────────────┐   │
│ │ ┌────┐ Nombre del pack     │   │
│ │ │img │ N disponibles ·dist │   │
│ │ │-XX%│ $oferta  $original  │   │
│ │ └────┘                     │   │
│ └───────────────────────────┘   │
│ ┌───────────────────────────┐   │
│ │   ... más cards (scroll)   │   │
│ └───────────────────────────┘   │
│           (lista vacía)          │
│   "No hay packs disponibles      │
│         cerca"                   │
└─────────────────────────────────┘
```

### Mockup (implementación real — `PackCard`)

- **Card**: fondo blanco, bordes redondeados 16dp, elevación 2dp.
- **Imagen placeholder**: cuadrado 72dp con gradiente Verde 80→40, con badge de
  descuento (`-XX%`) en Naranja 80 (`#E65100`) en la esquina superior derecha.
- **Texto**: nombre en `SemiBold` 15sp (`#212121`); línea secundaria
  "N disponibles · ~X km" en 12sp gris (`#757575`).
- **Precios**: precio de oferta en Verde 80 bold 16sp; precio original tachado en
  gris claro (`#BDBDBD`).
- **Scroll**: `LazyColumn` vertical, espaciado de 12dp entre cards.

### Checklist heurísticas de Nielsen

| # | Heurística | Cómo se cumple |
|---|---|---|
| 1 | Visibilidad del estado del sistema | Spinner verde durante la carga (`PacksUiState.Loading`) antes de mostrar el feed. |
| 2 | Coincidencia sistema/mundo real | "N disponibles", distancia en km, precio con signo `$` y porcentaje de descuento: lenguaje y unidades familiares para compras. |
| 3 | Control y libertad del usuario | El usuario puede scrollear libremente y tocar cualquier card para ver más sin compromiso; volver atrás no afecta el feed. |
| 4 | Consistencia y estándares | Todas las cards comparten el mismo layout (imagen + badge + nombre + precios), permitiendo comparar productos de un vistazo. |
| 5 | Prevención de errores | Tocar una card solo navega al detalle (no reserva ni cobra); la acción irreversible (reservar) queda en la pantalla siguiente con confirmación explícita. |
| 6 | Reconocimiento antes que recuerdo | El descuento, precio y disponibilidad están siempre visibles en la card — el usuario no necesita entrar al detalle para comparar opciones. |
| 7 | Flexibilidad y eficiencia de uso | Combinado con el buscador de la pantalla 1, un usuario frecuente puede filtrar directamente por nombre de pack/comercio sin navegar el feed completo. |
| 8 | Diseño estético y minimalista | Cada card muestra solo 4 datos (imagen, nombre, disponibilidad/distancia, precios) — la información mínima necesaria para decidir. |
| 9 | Ayudar a reconocer/recuperarse de errores | Si no hay resultados (feed vacío o búsqueda sin coincidencias), se muestra un mensaje claro en vez de una lista en blanco ambigua. |
| 10 | Ayuda y documentación | El badge de descuento y el precio tachado funcionan como "ayuda visual" autoexplicativa, sin necesitar tooltips ni texto adicional. |

---

## 4. Pantalla 3 — Detalle / Formulario / Input Crítico

**Objetivo:** convencer al usuario con el detalle del pack y asegurar, mediante un
input crítico, que comprende y acepta las condiciones de retiro antes de reservar.

### Wireframe (estructura)

```
┌─────────────────────────────────┐
│ [←]                        [♡]   │  ← hero con gradiente
│                                   │
│           [-XX%]                 │
├─────────────────────────────────┤
│ Nombre del pack                  │
│ 🏪 Comercio   📍 ~X km           │
│ ───────────────────────────────  │
│ Descripción...                   │
│ ┌───────────────────────────┐   │
│ │ $original (tachado)        │   │
│ │ $oferta          Ahorrás $$│   │
│ └───────────────────────────┘   │
│ N unidades disponibles           │
│                                   │
│ Cantidad:   [-]  1  [+]          │  ← NUEVO (input crítico)
│ ┌───────────────────────────┐   │
│ │ ⚠ Retirá hoy entre las     │   │  ← NUEVO
│ │   19:00 y las 21:00 hs.    │   │
│ │   No hay devoluciones por  │   │
│ │   llegada tardía.          │   │
│ └───────────────────────────┘   │
│ [ ] Entiendo y acepto el         │  ← NUEVO (checkbox)
│     horario de retiro            │
│                                   │
│ [   Reservar $XXX ahora 🛒   ]   │  ← deshabilitado hasta tildar
└─────────────────────────────────┘
```

### Mockup (implementación real + nuevo input crítico)

- **Hero**: gradiente vertical Verde 90→80 (`#1B5E20` → `#2E7D32`), 260dp de alto,
  botones "Volver" y "Favorito" en blanco superpuestos, badge `-XX%` en Naranja 80.
- **Tarjeta de precios**: fondo `#F5F5F5`, bordes 16dp; precio de oferta en Verde 80
  28sp bold, precio original tachado, chip "Ahorrás $$" en Naranja 80 con 10% alpha.
- **Selector de cantidad** *(nuevo)*: fila con botones `[-]` / `[+]` circulares y el
  número de unidades en el medio; actualiza el precio total mostrado en el botón CTA.
- **Caja de aviso legal** *(nuevo)*: fondo amarillo claro `#FFF3CD` con borde Naranja
  60, ícono de alerta, texto de franja horaria de retiro.
- **Checkbox obligatorio** *(nuevo)*: "Entiendo y acepto el horario de retiro".
- **Botón CTA**: ancho completo, 56dp de alto, bordes 28dp.
  - **Deshabilitado** (checkbox sin tildar): fondo gris `#DADADA`, texto gris.
  - **Habilitado** (checkbox tildado): fondo Verde 80, texto blanco, label
    "Reservar $<precio×cantidad> ahora 🛒".

### Checklist heurísticas de Nielsen

| # | Heurística | Cómo se cumple |
|---|---|---|
| 1 | Visibilidad del estado del sistema | Spinner mientras carga el detalle (`pack == null`); spinner dentro del botón mientras `ReserveUiState.Loading`. |
| 2 | Coincidencia sistema/mundo real | "Ahorrás $$", "unidades disponibles", "Retirá hoy entre las 19:00 y las 21:00 hs": lenguaje cotidiano, sin jerga técnica. |
| 3 | Control y libertad del usuario | Botón "Volver" siempre visible en el header; el usuario puede destildar el checkbox y el CTA vuelve a deshabilitarse sin perder los datos ingresados. |
| 4 | Consistencia y estándares | Mismo verde de marca para precios destacados y CTA en toda la app; mismo ícono de flecha para "Volver" que en el resto de los flujos. |
| 5 | Prevención de errores | El botón "Reservar" está **deshabilitado por defecto** y solo se habilita tras tildar el checkbox de aceptación de la franja horaria — evita reservas hechas "sin leer" las condiciones. |
| 6 | Reconocimiento antes que recuerdo | La franja horaria de retiro y el precio total quedan visibles en la propia pantalla (caja de aviso + botón), sin que el usuario deba recordarlos de otra pantalla. |
| 7 | Flexibilidad y eficiencia de uso | El selector `[-]/[+]` permite ajustar la cantidad sin recargar la pantalla; el precio total se recalcula al instante. |
| 8 | Diseño estético y minimalista | La información se organiza en bloques claros (hero, precios, input crítico, CTA) con un único llamado a la acción por pantalla. |
| 9 | Ayudar a reconocer/recuperarse de errores | Si la reserva falla, `AlertDialog` muestra el mensaje de error concreto (`ReserveUiState.Error`) y permite reintentar. |
| 10 | Ayuda y documentación | La caja de aviso legal funciona como ayuda contextual in-situ, explicando la condición antes de que el usuario la acepte. |

---

## 5. Flujo de navegación

```
[Pantalla 1: Home]
   │  (scroll)
   ▼
[Pantalla 2: Feed de Ofertas Flash]
   │  (tap en card de producto)
   ▼
[Pantalla 3: Detalle / Input crítico]
   │  (tildar checkbox → botón se habilita → "Reservar")
   ▼
[Confirmación] → AlertDialog / vuelve al feed con la reserva creada
```

## 6. Alcance de implementación

El selector de cantidad, la caja de aviso legal, el checkbox y el botón CTA dinámico
descritos en la Pantalla 3 se implementan como estado local de UI en
`PackDetailScreen.kt`. La cantidad seleccionada solo afecta el precio mostrado; la
reserva (`reservePack(packId)`) sigue representando 1 pack, sin cambios en el modelo
de dominio ni en el backend.
