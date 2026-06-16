# Matriz de Trazabilidad de Requerimientos (RTM)
## Proyecto: Aprovecha! — Sistema de Gestión de Rescate de Alimentos
### Grupo: Gang of Four | Materia: Ingeniería de Software II — IUA
### Versión: 3.0 | Fecha: Junio 2026 | Referencia: IEEE 730 §7

---

## Leyenda de Estados

| Estado | Descripción |
|--------|-------------|
| ✅ Implementado | Código escrito, trazabilidad aplicada, test existente y ejecutable |
| ⚠️ Parcial | Implementación de código completa, test pendiente |
| ❌ No iniciado | Sin implementación ni test |

---

## RTM Principal — Requerimientos Funcionales

| ID | Descripción | Módulo | Clase / UseCase | DAO / Entidad | Archivo de Test | Estado CI | Estado |
|----|-------------|--------|-----------------|---------------|-----------------|-----------|--------|
| **REQ-F01** | Registro en la plataforma (comercio y consumidor) | `:feature:auth` | `RegisterUserUseCase` | `UserDao` / `UserEntity` | `RegisterUserUseCaseTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F01** | Login de usuario registrado | `:feature:auth` | `LoginUserUseCase` | `UserDao` | `AuthRepositoryImplTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F01** | Selección de rol COMMERCE / CONSUMER al registrarse | `:feature:auth` | `RegisterScreen` + `AuthViewModel` | `UserEntity.role` | `RegisterUserUseCaseTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F02** | El comercio publica packs de alimentos con descuento | `:feature:reservations` | `PublishPackUseCase` | `PackDao` / `PackEntity` | `PublishPackUseCaseTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F02** | Validación: precio descuento < precio original | `:core:domain` | `PublishPackUseCase` | — | `PublishPackUseCaseTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F02** | Pack con nombre, descripción, cantidad y precios | `:core:domain` | `FoodPack` model | `PackEntity` | `PublishPackUseCaseTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F03** | El consumidor ve packs disponibles en radio cercano | `:feature:products` | `GetNearbyPacksUseCase` + `HomeConsumerScreen` | `PackDao.getAvailablePacks()` | `PackRepositoryImplTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F03** | Filtrado por radio configurable (default 5 km) | `:core:domain` | `GetNearbyPacksUseCase` | `CommerceEntity.latitud/longitud` | `PackRepositoryImplTest.kt` | detekt + test | ⚠️ Parcial |
| **REQ-F04** | El usuario reserva un pack para ser retirado | `:feature:products` | `ReservePackUseCase` | `PackDao.reservePackAtomically()` + `ReservationDao` | `ReservePackUseCaseTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F04** | Estado AVAILABLE → RESERVED al reservar (atómico) | `:core:data` | `PackDao.reservePackAtomically()` | `PackEntity.status` | `ReservePackUseCaseTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F05** | El comercio marca una reserva como "retirada" | `:feature:reservations` | `MarkReservationWithdrawnUseCase` | `ReservationDao.markAsWithdrawn()` | `MarkReservationWithdrawnUseCaseTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F05** | Estado RESERVED → WITHDRAWN | `:core:data` | `ReservationRepositoryImpl.markAsWithdrawn()` | `ReservationEntity.status` | `MarkReservationWithdrawnUseCaseTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F06** | El usuario cancela una reserva antes del retiro | `:feature:reservations` | `CancelReservationUseCase` | `ReservationDao.cancelReservation()` | `CancelReservationUseCaseTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F06** | Estado RESERVED → CANCELLED; no cancelar WITHDRAWN | `:core:data` | `ReservationRepositoryImpl.cancelReservation()` | `ReservationEntity.status` | `CancelReservationUseCaseTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F07** | El consumidor marca/desmarca packs como favoritos | `:feature:products` | `FavoriteRepository` + `ProductsViewModel` | `FavoriteDao` / `FavoriteEntity` | `FavoriteRepositoryImplTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F07** | Toggle: si no es favorito → agregar; si ya es favorito → eliminar | `:core:data` | `FavoriteRepositoryImpl.toggleFavorite()` | `FavoriteDao.addFavorite()` / `removeFavorite()` | `FavoriteRepositoryImplTest.kt` | detekt + test | ✅ Implementado |
| **REQ-F07** | El consumidor ve su lista de packs favoritos | `:feature:products` | `FavoritesScreen` + `ProductsViewModel.favoritePackIds` | `FavoriteDao.getFavoritePackIds()` | `FavoriteRepositoryImplTest.kt` | detekt + test | ✅ Implementado |

---

## RTM — Requerimientos No Funcionales

| ID | Descripción | Mecanismo de verificación | Herramienta | Archivo / Config | Estado CI | Estado |
|----|-------------|--------------------------|-------------|------------------|-----------|--------|
| **REQ-NF01** | Exclusividad de reservas bajo concurrencia | Transacción atómica Room (`@Transaction`) + test coroutines concurrentes | `PackDao.reservePackAtomically()` | `ReservationConcurrencyTest.kt` | detekt + test | ✅ Implementado |
| **REQ-NF01** | 10 usuarios concurrentes → solo 1 reserva exitosa | Coroutines `async/awaitAll` + `AtomicInteger` | JUnit4 + `kotlinx-coroutines-test` | `ReservationConcurrencyTest.kt` | test | ✅ Implementado |
| **REQ-NF01-S** | Sesión persistente entre reinicios de la app | DataStore (Jetpack) + `SessionManager` | `AuthRepositoryImpl` + `SessionManager` | `SessionManagerTest.kt` | detekt + test | ✅ Implementado |
| **REQ-NF02** | APK no supera 50 MB | Medición APK release | Gradle build + ProGuard/R8 | `app/build.gradle.kts` (`isMinifyEnabled`) | build (manual) | ⚠️ Parcial |

---

## RTM — Trazabilidad en Código (Anotación @Requirement)

| Archivo | Clase / Función | Requerimiento | Forma de trazabilidad |
|---------|-----------------|---------------|-----------------------|
| `RegisterUserUseCase.kt` | `invoke()` | REQ-F01 | `@Requirement("REQ-F01", ...)` |
| `LoginUserUseCase.kt` | `invoke()` | REQ-F01 | `@Requirement("REQ-F01", ...)` |
| `AuthRepository.kt` | interfaz | REQ-F01 | `// @REQ-F01` en KDoc |
| `UserEntity.kt` | clase | REQ-F01 | `// @REQ-F01` en KDoc |
| `UserDao.kt` | `insertUser()`, `getUserByEmail()` | REQ-F01 | `// @REQ-F01` en comentarios |
| `PublishPackUseCase.kt` | `invoke()` | REQ-F02 | `@Requirement("REQ-F02", ...)` |
| `PackRepository.kt` | `publishPack()` | REQ-F02 | `// @REQ-F02` en KDoc |
| `PackEntity.kt` | clase | REQ-F02, REQ-F03, REQ-NF01 | `// @REQ-FXX` en KDoc de campos |
| `PackDao.kt` | `insertPack()`, `getAvailablePacks()` | REQ-F02, REQ-F03 | `// @REQ-F02`, `// @REQ-F03` |
| `GetNearbyPacksUseCase.kt` | `invoke()` | REQ-F03 | `@Requirement("REQ-F03", ...)` |
| `CommerceEntity.kt` | `latitud`, `longitud` | REQ-F03 | `// @REQ-F03` en campos |
| `ReservePackUseCase.kt` | `invoke()` | REQ-F04, REQ-NF01 | `@Requirement("REQ-F04", ...)` |
| `PackDao.kt` | `reservePackAtomically()` | REQ-NF01 | `// @REQ-NF01` en KDoc |
| `ReservationEntity.kt` | clase | REQ-F04, REQ-F05, REQ-F06, REQ-NF01 | `// @REQ-FXX` en KDoc |
| `ReservationDao.kt` | `insertReservation()` | REQ-F04 | `// @REQ-F04` |
| `ReservationDao.kt` | `markAsWithdrawn()` | REQ-F05 | `// @REQ-F05` |
| `ReservationDao.kt` | `cancelReservation()` | REQ-F06 | `// @REQ-F06` |
| `MarkReservationWithdrawnUseCase.kt` | `invoke()` | REQ-F05 | `@Requirement("REQ-F05", ...)` |
| `CancelReservationUseCase.kt` | `invoke()` | REQ-F06 | `@Requirement("REQ-F06", ...)` |
| `PackStatus.kt` | enum | REQ-F02, REQ-F04, REQ-F05, REQ-F06, REQ-NF01 | `// @REQ-FXX` en cada valor |
| `ReservationStatus.kt` (en `Reservation.kt`) | enum | REQ-F04, REQ-F05, REQ-F06 | `// @REQ-FXX` en cada valor |
| `FavoriteRepository.kt` | interfaz | REQ-F07 | `// @REQ-F07` en KDoc |
| `FavoriteRepositoryImpl.kt` | clase | REQ-F07 | implementa `toggleFavorite()`, `getFavoritePackIds()` |
| `FavoriteDao.kt` | interfaz Room | REQ-F07 | `addFavorite()`, `removeFavorite()`, `getFavoritePackIds()`, `isFavorite()` |
| `FavoriteEntity.kt` | entidad Room | REQ-F07 | tabla `favorites` con `userId` + `packId` |
| `SessionManager.kt` | DataStore wrapper | REQ-F01 sesión | `saveSession()`, `getCurrentUser()`, `clearSession()` |
| `ProfileScreen.kt` + `ProfileViewModel.kt` | pantalla y VM | REQ-F01 sesión | carga usuario activo, expone `logout()` |
| `FavoritesScreen.kt` | pantalla | REQ-F07 | muestra lista de packs favoritos del usuario |
| `PendingReservationsScreen.kt` | pantalla | REQ-F05 | panel de comercio con reservas RESERVED pendientes de retiro |

---

## RTM — Inventario de Tests Existentes

### core:domain — Use Case Tests

| Archivo | Requerimiento | Casos de Test | Estado |
|---------|---------------|---------------|--------|
| `LoginUserUseCaseTest.kt` | REQ-F01 | `givenValidConsumerCredentials_whenLogin_thenReturnsSuccessWithUser` | ✅ |
| `LoginUserUseCaseTest.kt` | REQ-F01 | `givenValidCommerceCredentials_whenLogin_thenReturnsSuccessWithCommerceRole` | ✅ |
| `LoginUserUseCaseTest.kt` | REQ-F01 | `givenBlankEmail_whenLogin_thenReturnsErrorWithoutCallingRepository` | ✅ |
| `LoginUserUseCaseTest.kt` | REQ-F01 | `givenBlankPassword_whenLogin_thenReturnsErrorWithoutCallingRepository` | ✅ |
| `LoginUserUseCaseTest.kt` | REQ-F01 | `givenBothFieldsBlank_whenLogin_thenReturnsError` | ✅ |
| `LoginUserUseCaseTest.kt` | REQ-F01 | `givenUnknownEmail_whenLogin_thenPropagatesRepositoryError` | ✅ |
| `LoginUserUseCaseTest.kt` | REQ-F01 | `givenEmailWithSpaces_whenLogin_thenEmailIsTrimmedBeforeRepository` | ✅ |
| `LoginUserUseCaseTest.kt` | REQ-F01 | `givenWrongPassword_whenLogin_thenPropagatesRepositoryError` | ✅ |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenValidCommerceData_whenRegister_thenReturnsSuccessWithUser` | ✅ |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenValidConsumerData_whenRegister_thenReturnsSuccessWithUser` | ✅ |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenEmptyEmail_whenRegister_thenReturnsError` | ✅ |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenPasswordTooShort_whenRegister_thenReturnsError` | ✅ |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenEmptyNombre_whenRegister_thenReturnsError` | ✅ |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenDuplicateEmail_whenRegister_thenPropagatesRepositoryError` | ✅ |
| `GetNearbyPacksUseCaseTest.kt` | REQ-F03 | `givenAvailablePacks_whenInvokedWithCoordinates_thenEmitsList` | ✅ |
| `GetNearbyPacksUseCaseTest.kt` | REQ-F03 | `givenNoPacks_whenInvoked_thenEmitsEmptyList` | ✅ |
| `GetNearbyPacksUseCaseTest.kt` | REQ-F03 | `givenNoRadiusSpecified_whenInvoked_thenUsesDefaultRadius` | ✅ |
| `GetNearbyPacksUseCaseTest.kt` | REQ-F03 | `givenCustomRadius_whenInvoked_thenPassesRadiusToRepository` | ✅ |
| `GetNearbyPacksUseCaseTest.kt` | REQ-F03 | `givenPacksInRepository_whenInvoked_thenDelegatesToRepository` | ✅ |
| `PublishPackUseCaseTest.kt` | REQ-F02 | `givenValidPack_whenPublish_thenReturnsSuccessWithStatusAvailable` | ✅ |
| `PublishPackUseCaseTest.kt` | REQ-F02 | `givenPackWithPrices_whenPublish_thenDiscountPercentageIsCorrect` | ✅ |
| `PublishPackUseCaseTest.kt` | REQ-F02 | `givenEmptyName_whenPublish_thenReturnsError` | ✅ |
| `PublishPackUseCaseTest.kt` | REQ-F02 | `givenDiscountPriceHigherThanOriginal_whenPublish_thenReturnsError` | ✅ |
| `PublishPackUseCaseTest.kt` | REQ-F02 | `givenZeroQuantity_whenPublish_thenReturnsError` | ✅ |
| `ReservePackUseCaseTest.kt` | REQ-F04 | `givenAvailablePack_whenUserReserves_thenReservationIsCreated` | ✅ |
| `ReservePackUseCaseTest.kt` | REQ-NF01 | `givenAlreadyReservedPack_whenSecondUserTries_thenReturnsError` | ✅ |
| `MarkReservationWithdrawnUseCaseTest.kt` | REQ-F05 | `givenReservedReservation_whenCommerceMarksWithdrawn_thenStatusIsWithdrawn` | ✅ |
| `MarkReservationWithdrawnUseCaseTest.kt` | REQ-F05 | `givenCancelledReservation_whenCommerceMarksWithdrawn_thenReturnsError` | ✅ |
| `CancelReservationUseCaseTest.kt` | REQ-F06 | `givenActiveReservation_whenUserCancels_thenStatusIsCancelled` | ✅ |
| `CancelReservationUseCaseTest.kt` | REQ-F06 | `givenWithdrawnReservation_whenUserCancels_thenReturnsError` | ✅ |
| `ReservationConcurrencyTest.kt` | REQ-NF01 | `givenSingleAvailablePack_whenMultipleUsersReserveConcurrently_thenOnlyOneSucceeds` | ✅ |
| `ReservationConcurrencyTest.kt` | REQ-NF01 | `givenAvailablePack_whenReserved_thenReservationHasCorrectStatus` | ✅ |

### core:data — Repository Tests

| Archivo | Requerimiento | Casos de Test | Estado |
|---------|---------------|---------------|--------|
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given new email When register consumer Then returns Success with user` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given new email When register commerce Then returns Success with COMMERCE role` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given existing email When register called Then returns Error with duplicate message` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given DAO throws When register called Then returns Result Error` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given successful register When called Then sessionManager saveSession invoked with new user` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given valid credentials When login called Then returns Success with user` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given unknown email When login called Then returns Error user not found` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given wrong password When login called Then returns Error wrong password` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given successful login When called Then sessionManager saveSession invoked with user` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given successful register When insert called Then DAO insertUser called once` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given saved session When getCurrentUser called Then returns sessionManager user` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given no saved session When getCurrentUser called Then returns null` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given active session When logout called Then sessionManager clearSession invoked` | ✅ |
| `PackRepositoryImplTest.kt` | REQ-F02 | `Given valid pack When publishPack called Then returns Success with assigned id` | ✅ |
| `PackRepositoryImplTest.kt` | REQ-F02 | `Given DAO throws When publishPack called Then returns Result Error` | ✅ |
| `PackRepositoryImplTest.kt` | REQ-F02 | `Given valid pack When publishPack Then DAO insertPack called once` | ✅ |
| `PackRepositoryImplTest.kt` | REQ-F03 | `Given available packs When getAvailablePacksNearby Then emits mapped domain list` | ✅ |
| `PackRepositoryImplTest.kt` | REQ-F03 | `Given no available packs When getAvailablePacksNearby Then emits empty list` | ✅ |
| `PackRepositoryImplTest.kt` | REQ-F03 | `Given existing pack When getPackById called Then returns Success` | ✅ |
| `PackRepositoryImplTest.kt` | REQ-F03 | `Given nonexistent pack When getPackById called Then returns Error` | ✅ |
| `PackRepositoryImplTest.kt` | REQ-F02 | `Given AVAILABLE pack When deletePack called Then returns Success` | ✅ |
| `PackRepositoryImplTest.kt` | REQ-F04 | `Given RESERVED pack When deletePack called Then returns Error` | ✅ |
| `PackRepositoryImplTest.kt` | REQ-F02 | `Given commerce has packs When getPacksByCommerce Then emits commerce packs` | ✅ |
| `PackRepositoryImplTest.kt` | REQ-F02 | `Given valid pack When updatePack called Then returns Success` | ✅ |
| `PackRepositoryImplTest.kt` | REQ-F02 | `Given DAO throws When updatePack called Then returns Result Error` | ✅ |
| `ReservationRepositoryImplTest.kt` | REQ-F04 + REQ-NF01 | `Given available pack When createReservation called Then returns Success with RESERVED status` | ✅ |
| `ReservationRepositoryImplTest.kt` | REQ-NF01 | `Given pack already reserved When createReservation called Then returns Error` | ✅ |
| `ReservationRepositoryImplTest.kt` | REQ-F04 | `Given DAO throws When createReservation called Then returns Result Error` | ✅ |
| `ReservationRepositoryImplTest.kt` | REQ-F04 | `Given successful reservation When verifying DAO calls Then both DAOs called once` | ✅ |
| `ReservationRepositoryImplTest.kt` | REQ-F05 | `Given RESERVED reservation When markAsWithdrawn called Then returns Success with WITHDRAWN` | ✅ |
| `ReservationRepositoryImplTest.kt` | REQ-F05 | `Given nonexistent reservation When markAsWithdrawn called Then returns Error not found` | ✅ |
| `ReservationRepositoryImplTest.kt` | REQ-F05 | `Given CANCELLED reservation When markAsWithdrawn called Then returns Error` | ✅ |
| `ReservationRepositoryImplTest.kt` | REQ-F06 | `Given RESERVED reservation When cancelReservation called Then returns Success with CANCELLED` | ✅ |
| `ReservationRepositoryImplTest.kt` | REQ-F06 | `Given WITHDRAWN reservation When cancelReservation called Then returns Error` | ✅ |
| `ReservationRepositoryImplTest.kt` | REQ-F06 | `Given nonexistent reservation When cancelReservation called Then returns Error not found` | ✅ |
| `ReservationRepositoryImplTest.kt` | REQ-F06 | `Given user has reservations When getReservationsByUser called Then emits mapped list` | ✅ |
| `ReservationRepositoryImplTest.kt` | REQ-F04 | `Given existing reservation When getReservationById called Then returns Success` | ✅ |
| `ReservationRepositoryImplTest.kt` | REQ-F04 | `Given nonexistent reservation When getReservationById called Then returns Error` | ✅ |

### core:data — SessionManager Tests

| Archivo | Requerimiento | Casos de Test | Estado |
|---------|---------------|---------------|--------|
| `SessionManagerTest.kt` | REQ-F01 | `Given no session When getCurrentUser called Then returns null` | ✅ |
| `SessionManagerTest.kt` | REQ-F01 | `Given saved session When getCurrentUser called Then returns same user` | ✅ |
| `SessionManagerTest.kt` | REQ-F01 | `Given saved session When clearSession called Then getCurrentUser returns null` | ✅ |
| `SessionManagerTest.kt` | REQ-F01 | `Given consumer session saved When new session saved Then getCurrentUser returns new user` | ✅ |

### core:data — Favorite Repository Tests

| Archivo | Requerimiento | Casos de Test | Estado |
|---------|---------------|---------------|--------|
| `FavoriteRepositoryImplTest.kt` | REQ-F07 | `Given user has favorites When getFavoritePackIds called Then returns Set of ids` | ✅ |
| `FavoriteRepositoryImplTest.kt` | REQ-F07 | `Given pack not favorited When toggleFavorite called Then addFavorite is invoked` | ✅ |
| `FavoriteRepositoryImplTest.kt` | REQ-F07 | `Given pack already favorited When toggleFavorite called Then removeFavorite is invoked` | ✅ |
| `FavoriteRepositoryImplTest.kt` | REQ-F07 | `Given user has no favorites When getFavoritePackIds called Then returns empty Set` | ✅ |

### feature — ViewModel Tests

| Archivo | Requerimiento | Casos de Test | Estado |
|---------|---------------|---------------|--------|
| `AuthViewModelTest.kt` | REQ-F01 | `Given valid credentials When login called Then uiState emits Success with role` | ✅ |
| `AuthViewModelTest.kt` | REQ-F01 | `Given commerce credentials When login called Then uiState emits Success with COMMERCE role` | ✅ |
| `AuthViewModelTest.kt` | REQ-F01 | `Given blank email When login called Then uiState emits Error immediately` | ✅ |
| `AuthViewModelTest.kt` | REQ-F01 | `Given blank password When login called Then uiState emits Error immediately` | ✅ |
| `AuthViewModelTest.kt` | REQ-F01 | `Given invalid credentials When login called Then uiState emits Error with message` | ✅ |
| `AuthViewModelTest.kt` | REQ-F01 | `Given valid data When register consumer Then uiState emits Success with CONSUMER role` | ✅ |
| `AuthViewModelTest.kt` | REQ-F01 | `Given valid data When register commerce Then uiState emits Success with COMMERCE role` | ✅ |
| `AuthViewModelTest.kt` | REQ-F01 | `Given blank fields When register called Then uiState emits Error` | ✅ |
| `AuthViewModelTest.kt` | REQ-F01 | `Given password too short When register called Then uiState emits Error` | ✅ |
| `AuthViewModelTest.kt` | REQ-F01 | `Given duplicate email When register called Then uiState emits Error` | ✅ |
| `AuthViewModelTest.kt` | REQ-F01 | `Given Success state When resetState called Then uiState returns to Idle` | ✅ |
| `AuthViewModelTest.kt` | REQ-F01 | `Given active session When logout called Then authRepository logout invoked and uiState is Idle` | ✅ |
| `ProfileViewModelTest.kt` | REQ-F01 | `Given active session When ViewModel initializes Then user is loaded` | ✅ |
| `ProfileViewModelTest.kt` | REQ-F01 | `Given no session When ViewModel initializes Then user is null` | ✅ |
| `ProfileViewModelTest.kt` | REQ-F01 | `Given active session When logout called Then repository logout invoked and user is null` | ✅ |
| `ProductsViewModelTest.kt` | REQ-F03 | `Given available packs When ViewModel initializes Then packsState emits Success` | ✅ |
| `ProductsViewModelTest.kt` | REQ-F03 | `Given repository throws When loading packs Then packsState emits Error` | ✅ |
| `ProductsViewModelTest.kt` | REQ-F03 | `Given valid packId When loadPackDetail called Then selectedPack is set` | ✅ |
| `ProductsViewModelTest.kt` | REQ-F03 | `Given invalid packId When loadPackDetail fails Then selectedPack remains null` | ✅ |
| `ProductsViewModelTest.kt` | REQ-F04 | `Given successful reservation When reservePack called Then reserveState emits Success` | ✅ |
| `ProductsViewModelTest.kt` | REQ-F04 | `Given pack unavailable When reservePack fails Then reserveState emits Error` | ✅ |
| `ProductsViewModelTest.kt` | REQ-F04 | `Given Success state When resetReserveState called Then state returns to Idle` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F06 | `Given user has reservations When loadUserReservations called Then state emits Success` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F06 | `Given repository throws When loadUserReservations fails Then state emits Error` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F06 | `Given no session When loadUserReservations called Then state emits Error` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F06 | `Given RESERVED reservation When cancelReservation called Then actionState emits Success` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F06 | `Given WITHDRAWN reservation When cancelReservation called Then actionState emits Error` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F05 | `Given RESERVED reservation When markAsWithdrawn called Then actionState emits Success` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F05 | `Given nonexistent reservation When markAsWithdrawn called Then actionState emits Error` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F02 | `Given valid pack When publishPack called Then actionState emits Success` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F02 | `Given invalid pack When publishPack fails validation Then actionState emits Error` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F02 | `Given no session When publishPack called Then actionState emits Error` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F02 | `Given Success actionState When resetAction called Then returns to Idle` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F02 | `Given commerce has packs When loadCommercePacks called Then commercePacksState emits Success` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F02 | `Given packRepository throws When loadCommercePacks called Then commercePacksState emits Error` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F02 | `Given no session When loadCommercePacks called Then commercePacksState emits Error` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F05 | `Given commerce has RESERVED reservations When loadPendingReservations called Then pendingReservationsState emits Success` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F05 | `Given no session When loadPendingReservations called Then pendingReservationsState emits Error` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F05 | `Given mixed reservations When loadPendingReservations called Then commerceStatsState has correct counts` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F05 | `Given repository throws When loadPendingReservations called Then pendingReservationsState emits Error` | ✅ |

---

## Mapa de Cobertura por Requerimiento

```
REQ-F01 → RegisterUserUseCase / LoginUserUseCase / AuthRepositoryImpl / SessionManager
          → LoginUserUseCaseTest (8) + RegisterUserUseCaseTest (6)
          + AuthRepositoryImplTest (13) + SessionManagerTest (4)
          + AuthViewModelTest (12) + ProfileViewModelTest (3)                    ✅

REQ-F02 → PublishPackUseCase / PackRepositoryImpl / ReservationsViewModel
          → PublishPackUseCaseTest (5) + PackRepositoryImplTest (3 tests REQ-F02)
          + ReservationsViewModelTest (4 tests REQ-F02)                          ✅

REQ-F03 → GetNearbyPacksUseCase / PackRepositoryImpl / ProductsViewModel
          → GetNearbyPacksUseCaseTest (5) + PackRepositoryImplTest (5 tests REQ-F03)
          + ProductsViewModelTest (4 tests REQ-F03)                              ✅

REQ-F04 → ReservePackUseCase / PackDao.reservePackAtomically() / ProductsViewModel
          → ReservePackUseCaseTest (2) + ReservationRepositoryImplTest (3 tests REQ-F04)
          + ProductsViewModelTest (3 tests REQ-F04)                              ✅

REQ-F05 → MarkReservationWithdrawnUseCase / ReservationsViewModel
          → MarkReservationWithdrawnUseCaseTest (2)
          + ReservationRepositoryImplTest (3 tests REQ-F05)
          + ReservationsViewModelTest (6 tests REQ-F05)                          ✅

REQ-F06 → CancelReservationUseCase / ReservationsViewModel
          → CancelReservationUseCaseTest (2)
          + ReservationRepositoryImplTest (4 tests REQ-F06)
          + ReservationsViewModelTest (3 tests REQ-F06)                          ✅

REQ-F07 → FavoriteRepository / FavoriteRepositoryImpl / ProductsViewModel
          → FavoriteRepositoryImplTest (4 tests)                                ✅

REQ-NF01→ PackDao.reservePackAtomically() (Room @Transaction)
          → ReservePackUseCaseTest (1) + ReservationConcurrencyTest (2)
          + ReservationRepositoryImplTest (2 tests REQ-NF01)                    ✅

REQ-NF02→ ProGuard/R8 en release build                                          ⚠️ Parcial
```

**Total de tests implementados: 118**

| Módulo | Tests |
|--------|-------|
| `core:domain` | 32 |
| `core:data` | 46 |
| `feature:auth` | 15 |
| `feature:products` | 7 |
| `feature:reservations` | 18 |
| **Total** | **118** |

**Cobertura JaCoCo (último build):** INSTRUCTION=84.73% / BRANCH=80.41% — supera umbral IEEE 730 §5.3 (70%/60%)

---

*Última actualización: Junio 2026 — Versión 3.0*
*Mantener sincronizado con cada merge a main.*
