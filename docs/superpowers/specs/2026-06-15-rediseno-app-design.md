# Rediseño visual + nuevas funcionalidades — Aprovecha!

**Fecha:** 2026-06-15
**Estado:** Aprobado para implementación

## Contexto y objetivo

La app "Aprovecha!" (Kotlin + Jetpack Compose + Room + Hilt, multi-módulo) tiene
una UI funcional pero visualmente genérica (Material 3 por defecto, paleta
verde/naranja clásica), y varios botones/iconos sin funcionalidad (`onClick = {}`),
IDs de usuario/comercio hardcodeados, y un selector de cantidad engañoso en el
detalle de pack.

El objetivo es un cambio drástico de identidad visual ("Eco-moderno vibrante"),
sumado a un conjunto de funcionalidades nuevas que le dan sentido real a los
elementos hoy decorativos, manteniendo la cobertura de tests (JaCoCo ≥70%
instrucciones / ≥60% ramas) y sin romper la suite existente.

## 1. Sistema de diseño — "Eco-moderno vibrante"

Nueva paleta (reemplaza `app/src/main/java/com/aprovecha/app/ui/theme/Color.kt`):

| Token | Valor | Uso |
|---|---|---|
| Primario (Verde bosque) | `#1B5E20` / `#2E7D32` | Marca, headers, CTAs principales |
| Acento vibrante (Lima) | `#AEEA00` | Highlights, precios, elementos destacados |
| Urgencia/descuento (Coral) | `#FF5252` | Badges de descuento, urgencia, errores leves |
| Fondo (Menta clara) | `#F1F8F4` | Background general |
| Superficie | `#FFFFFF` | Cards, sheets |
| Texto primario | `#1A1C19` | Texto principal |
| Texto secundario | `#5F6F60` | Texto secundario/metadata |

Nuevo `Shape.kt` compartido vía `MaterialTheme.shapes`:

- Cards: `RoundedCornerShape(24.dp)`
- Botones primarios: `RoundedCornerShape(28.dp)` (pill)
- Inputs: `RoundedCornerShape(16.dp)`
- Bottom nav: barra flotante pill `RoundedCornerShape(32.dp)`, con margen respecto
  a los bordes de la pantalla
- Badges/chips: `RoundedCornerShape(50)` (pill completo)

Tipografía: se mantiene la escala actual de `Type.kt`. Los textos de precios y
descuentos pasan a usar el color de acento lima para dar contraste visual.

Las 7 pantallas existentes dejan de usar `Color(0xFF...)` hardcodeados sueltos
y pasan a `MaterialTheme.colorScheme.*` / `MaterialTheme.shapes.*`.

## 2. Arquitectura de sesión

Problema actual: `AuthRepository.getCurrentUser()` siempre devuelve `null`,
`logout()` es un no-op, y `ProductsViewModel` / `ReservationsViewModel` usan
`DEFAULT_USER_ID = 1L` / `DEFAULT_COMMERCE_ID = 1L` hardcodeados.

Cambios:

- Nuevo `SessionManager` en `core/data`, respaldado por DataStore Preferences
  (nueva dependencia `androidx.datastore:datastore-preferences`). Guarda
  `userId`, `name`, `email`, `role`.
- `AuthRepositoryImpl.login()` / `.register()` escriben la sesión al autenticar
  con éxito. `logout()` la limpia. `getCurrentUser()` la lee desde
  `SessionManager`.
- `AuthViewModel`, `ProductsViewModel`, `ReservationsViewModel` dejan de usar
  IDs hardcodeados y obtienen el usuario actual desde la sesión.
- Para `commerceId` en rol `COMMERCE`, se usa el mismo `userId` de sesión
  (consistente con el hardcode actual donde `DEFAULT_USER_ID == DEFAULT_COMMERCE_ID == 1L`).
- No hay auto-login persistente entre reinicios de la app: la sesión vive
  durante la ejecución de la app vía DataStore. `Logout` limpia la sesión y
  navega a `LOGIN` limpiando el back stack.

## 3. Perfil + Logout + Bottom Nav unificado

Nueva pantalla `ProfileScreen` (en `feature/auth`):

- Header con avatar placeholder, nombre y email del usuario actual (desde
  `SessionManager`).
- Card con rol (Consumidor/Comercio).
  - Consumidor: accesos directos a "Favoritos" y "Mis Reservas".
  - Comercio: accesos directos a "Mis Packs" y "Reservas a confirmar".
- Botón "Cerrar sesión" (coral) → `AuthViewModel.logout()` → navega a `LOGIN`
  limpiando el back stack.

Nuevo módulo `core/ui` (solo Compose, sin lógica de negocio) con
`AprovechaBottomBar`, barra flotante pill:

- **Consumidor**: Inicio · Favoritos · Reservas · Perfil
- **Comercio**: Inicio · Reservas a confirmar · Perfil

Reemplaza el `ConsumerTabBar` actual (tab "PERFIL" muerto, inexistente en
pantallas de Comercio).

Nueva ruta `PROFILE` en `NavGraph`.

## 4. Favoritos de packs

Persistencia:

- Nueva entidad Room `FavoriteEntity(userId, packId)` con índice único
  compuesto.
- Nuevo `FavoriteDao` (insert/delete, `getFavoritesByUser` como `Flow`,
  `isFavorite` query).
- Se agrega a `AprovechaDatabase` (versión 2, con
  `fallbackToDestructiveMigration()`).

Dominio — nuevo `FavoriteRepository` (interfaz en `core/domain`, impl en
`core/data`):

- `toggleFavorite(userId, packId)`
- `isFavorite(userId, packId): Flow<Boolean>`
- `getFavoritePacks(userId): Flow<List<FoodPack>>` (join con `PackDao`)

UI:

- `PackDetailScreen`: el ícono ❤️ (hoy `onClick = {}`) alterna entre
  `FavoriteBorder` / `Favorite` según `isFavorite`, llamando a
  `ProductsViewModel.toggleFavorite(packId)`.
- Nueva `FavoritesScreen` (en `feature/products`, reutiliza `PackCard`):
  lista de packs favoritos, accesible desde el bottom nav del Consumidor.
  Empty state si no hay favoritos.

`ProductsViewModel` gana: `favoritesState: StateFlow<List<FoodPack>>`,
`isPackFavorite(packId): StateFlow<Boolean>`, `toggleFavorite(packId)`.

## 5. Centro de notificaciones

Notificaciones derivadas de datos existentes (sin servidor push):

**Consumidor:**
- "Tu reserva vence pronto" — reserva `RESERVED` con `pack.expirationTime` a
  menos de 2hs.
- "Retiro confirmado" — reservas pasadas a `WITHDRAWN` recientemente.

**Comercio:**
- "Nueva reserva recibida" — reservas `RESERVED` sobre packs propios.
- "Pack por vencer sin reservar" — packs `AVAILABLE` propios con
  `expirationTime` a menos de 2hs.

Implementación:

- Nuevo módulo `feature/notifications` (screen + viewmodel + tests, mismo
  patrón que los demás features).
- `GetNotificationsUseCase` en `core/domain`: combina `ReservationRepository`
  + `PackRepository` según `userId`/`role`, devuelve `List<NotificationItem>`
  (id, título, mensaje, tipo, timestamp).
- `NotificationReadStateStore` (DataStore, en `core/data`, reutiliza la
  dependencia agregada para sesión): guarda IDs de notificaciones leídas →
  permite calcular badge de no leídas y botón "Marcar todas como leídas".
- La campanita 🔔 (hoy `onClick = {}`) en Home Consumidor y Home Comercio
  navega a `NotificationsScreen`, mostrando un badge si hay no leídas.

## 6. Panel Comercio — "Reservas a confirmar"

Nueva `PendingReservationsScreen` (en `feature/reservations`, accesible desde
el bottom nav del Comercio):

- Combina `getReservationsByCommerce(commerceId)` + packs del comercio.
  Por cada reserva `RESERVED`: nombre del pack, cantidad, horario de retiro
  (`19:00–21:00`), botón "Confirmar retiro".
- "Confirmar retiro" → `ReservationsViewModel.markAsWithdrawn(reservationId)`
  (ya existe en el VM, hoy sin UI). La lista se actualiza sola (flow reactivo).
- Empty state: "No tenés retiros pendientes 🎉".

`ReservationsViewModel` gana:

- `pendingReservationsState: StateFlow<PendingReservationsUiState>`, expone
  `List<PendingReservation>` (data class: `reservation: Reservation`,
  `pack: FoodPack`).
- `loadPendingReservations(commerceId)`.

## 7. Filtro en "Mis Reservas" + Stats reales del comercio

**Filtro (Mis Reservas):**

El ícono `FilterList` (hoy `onClick = {}`) abre un menú desplegable con
estado local (sin tocar el VM):

- Tab Activas: sin filtro adicional (todas `RESERVED`).
- Tab Historial: "Todos / Retirados / Cancelados" → filtra entre `WITHDRAWN`
  y `CANCELLED`.

**Stats reales del comercio:**

- `ReservationsViewModel` gana `commerceStatsState: StateFlow<CommerceStats>`
  (`CommerceStats(activePacks, totalPublished, kgRescatados)`), calculado
  combinando `getReservationsByCommerce(commerceId)` (cuenta reservas
  `WITHDRAWN`) con `commercePacksState`.
- `kgRescatados = reservasRetiradas * AVG_PACK_WEIGHT_KG` (constante, `0.45`).
  Se muestra como `"X.X kg"` en vez del emoji fijo 🌱.
- `HomeCommerceScreen` (ya usa `ReservationsViewModel`) consume este estado
  para las 3 `StatCard`.

## 8. Bug fixes finales

- `PackDetailScreen`: se quita el selector +/- de cantidad (engañoso, el
  modelo reserva el pack completo). Se reemplaza por texto informativo
  "Este pack incluye N unidades — se reserva completo" y el botón pasa a
  "Reservar pack completo por $X".
- Ícono ❤️ favorito → funcional (sección 4).
- Campanitas 🔔 (Consumer y Comercio) → funcional, navegan a Notificaciones
  (sección 5).
- Tab "PERFIL" → pantalla real + Logout (sección 3).
- Filtro 🔽 en Mis Reservas → funcional (sección 7).
- `markAsWithdrawn` conectado a UI (sección 6).
- IDs hardcodeados (`DEFAULT_USER_ID`, `DEFAULT_COMMERCE_ID`) → sesión real
  (sección 2).
- "Kg rescatados" → cálculo real (sección 7).

## 9. Estrategia de testing

- Cada pieza nueva de lógica (`SessionManager`, `FavoriteRepository`,
  `GetNotificationsUseCase`, `NotificationReadStateStore`, `CommerceStats`,
  `PendingReservation` mapping, `toggleFavorite`) lleva tests unitarios con
  MockK, siguiendo el patrón existente (`*Test.kt` en `src/test/kotlin`).
- Los Composables UI quedan excluidos del reporte JaCoCo (configurado en
  `98853b8`), por lo que el rediseño visual no afecta el % de cobertura.
- Al finalizar la implementación se corre la suite completa
  (`./gradlew test jacocoTestReport`) y se verifica que el % se mantenga
  ≥70% instrucciones / ≥60% ramas.

## Fuera de alcance

- Reservas parciales por cantidad (cambio de modelo de dominio mayor,
  descartado explícitamente).
- Auto-login persistente entre reinicios de la app.
- Notificaciones push reales / servidor.
- Modo oscuro.
