# Matriz de Trazabilidad de Requerimientos (RTM)
## Proyecto: Aprovecha! — Sistema de Gestión de Rescate de Alimentos
### Grupo: Gang of Four | Materia: Ingeniería de Software II — IUA
### Versión: 2.0 | Fecha: Mayo 2026 | Referencia: IEEE 730 §7

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

---

## RTM — Requerimientos No Funcionales

| ID | Descripción | Mecanismo de verificación | Herramienta | Archivo / Config | Estado CI | Estado |
|----|-------------|--------------------------|-------------|------------------|-----------|--------|
| **REQ-NF01** | Exclusividad de reservas bajo concurrencia | Transacción atómica Room (`@Transaction`) + test coroutines concurrentes | `PackDao.reservePackAtomically()` | `ReservationConcurrencyTest.kt` | detekt + test | ✅ Implementado |
| **REQ-NF01** | 10 usuarios concurrentes → solo 1 reserva exitosa | Coroutines `async/awaitAll` + `AtomicInteger` | JUnit4 + `kotlinx-coroutines-test` | `ReservationConcurrencyTest.kt` | test | ✅ Implementado |
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

---

## RTM — Inventario de Tests Existentes

### core:domain — Use Case Tests

| Archivo | Requerimiento | Casos de Test | Estado |
|---------|---------------|---------------|--------|
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenValidCommerceData_whenRegister_thenReturnsSuccessWithUser` | ✅ |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenValidConsumerData_whenRegister_thenReturnsSuccessWithUser` | ✅ |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenEmptyEmail_whenRegister_thenReturnsError` | ✅ |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenPasswordTooShort_whenRegister_thenReturnsError` | ✅ |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenEmptyNombre_whenRegister_thenReturnsError` | ✅ |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenDuplicateEmail_whenRegister_thenPropagatesRepositoryError` | ✅ |
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
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given valid credentials When login called Then returns Success with user` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given unknown email When login called Then returns Error user not found` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given wrong password When login called Then returns Error wrong password` | ✅ |
| `AuthRepositoryImplTest.kt` | REQ-F01 | `Given successful register When insert called Then DAO insertUser called once` | ✅ |
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

### feature — ViewModel Tests

| Archivo | Requerimiento | Casos de Test | Estado |
|---------|---------------|---------------|--------|
| `ProductsViewModelTest.kt` | REQ-F03 | `Given available packs When ViewModel initializes Then packsState emits Success` | ✅ |
| `ProductsViewModelTest.kt` | REQ-F03 | `Given repository throws When loading packs Then packsState emits Error` | ✅ |
| `ProductsViewModelTest.kt` | REQ-F03 | `Given valid packId When loadPackDetail called Then selectedPack is set` | ✅ |
| `ProductsViewModelTest.kt` | REQ-F03 | `Given invalid packId When loadPackDetail fails Then selectedPack remains null` | ✅ |
| `ProductsViewModelTest.kt` | REQ-F04 | `Given successful reservation When reservePack called Then reserveState emits Success` | ✅ |
| `ProductsViewModelTest.kt` | REQ-F04 | `Given pack unavailable When reservePack fails Then reserveState emits Error` | ✅ |
| `ProductsViewModelTest.kt` | REQ-F04 | `Given Success state When resetReserveState called Then state returns to Idle` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F06 | `Given user has reservations When loadUserReservations called Then state emits Success` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F06 | `Given repository throws When loadUserReservations fails Then state emits Error` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F06 | `Given RESERVED reservation When cancelReservation called Then actionState emits Success` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F06 | `Given WITHDRAWN reservation When cancelReservation called Then actionState emits Error` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F05 | `Given RESERVED reservation When markAsWithdrawn called Then actionState emits Success` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F05 | `Given nonexistent reservation When markAsWithdrawn called Then actionState emits Error` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F02 | `Given valid pack When publishPack called Then actionState emits Success` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F02 | `Given invalid pack When publishPack fails validation Then actionState emits Error` | ✅ |
| `ReservationsViewModelTest.kt` | REQ-F02 | `Given Success actionState When resetAction called Then returns to Idle` | ✅ |

---

## Mapa de Cobertura por Requerimiento

```
REQ-F01 → RegisterUserUseCase / LoginUserUseCase / AuthRepositoryImpl
          → RegisterUserUseCaseTest (6 tests) + AuthRepositoryImplTest (8 tests) ✅

REQ-F02 → PublishPackUseCase / PackRepositoryImpl / ReservationsViewModel
          → PublishPackUseCaseTest (5 tests) + PackRepositoryImplTest (4 tests)
          + ReservationsViewModelTest (3 tests)                                  ✅

REQ-F03 → GetNearbyPacksUseCase / PackRepositoryImpl / ProductsViewModel
          → PackRepositoryImplTest (5 tests) + ProductsViewModelTest (4 tests)   ✅

REQ-F04 → ReservePackUseCase / PackDao.reservePackAtomically() / ProductsViewModel
          → ReservePackUseCaseTest (2 tests) + ProductsViewModelTest (3 tests)   ✅

REQ-F05 → MarkReservationWithdrawnUseCase / ReservationsViewModel
          → MarkReservationWithdrawnUseCaseTest (2 tests)
          + ReservationsViewModelTest (2 tests)                                   ✅

REQ-F06 → CancelReservationUseCase / ReservationsViewModel
          → CancelReservationUseCaseTest (2 tests)
          + ReservationsViewModelTest (4 tests)                                   ✅

REQ-NF01→ PackDao.reservePackAtomically() (Room @Transaction)
          → ReservePackUseCaseTest (1 test) + ReservationConcurrencyTest (2 tests) ✅

REQ-NF02→ ProGuard/R8 en release build                                           ⚠️ Parcial
```

**Total de tests implementados: 53**

---

*Última actualización: Mayo 2026 — Versión 2.0*
*Mantener sincronizado con cada merge a main.*
