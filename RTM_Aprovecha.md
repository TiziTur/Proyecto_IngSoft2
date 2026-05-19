# Matriz de Trazabilidad de Requerimientos (RTM)
## Proyecto: Aprovecha! — Sistema de Gestión de Rescate de Alimentos
### Grupo: Gang of Four | Materia: Ingeniería de Software II — IUA
### Versión: 1.0 | Fecha: Mayo 2025 | Referencia: IEEE 730 §7

---

## Leyenda de Estados

| Estado | Descripción |
|--------|-------------|
| ✅ Implementado | Código escrito, trazabilidad aplicada, test existente |
| 🔄 Planificado | Diseñado en arquitectura, pendiente de implementación |
| ❌ No iniciado | Pendiente |
| ⚠️ Parcial | Implementación incompleta |

---

## RTM Principal — Requerimientos Funcionales

| ID | Descripción del Requerimiento | Módulo | Clase / UseCase | DAO / Entidad | Archivo de Test | Nivel Test | Estado CI | Trazabilidad en Código |
|----|-------------------------------|--------|-----------------|---------------|-----------------|------------|-----------|------------------------|
| **REQ-F01** | El comercio debe poder registrarse en la plataforma | `:feature:auth` | `RegisterUserUseCase` | `UserDao` / `UserEntity` | `RegisterUserUseCaseTest.kt` | Unit + UI | lint + test | `@Requirement("REQ-F01")` en `RegisterUserUseCase.invoke()` |
| **REQ-F01** | Login de usuario registrado | `:feature:auth` | `LoginUserUseCase` | `UserDao` | `LoginUserUseCaseTest.kt` (pendiente) | Unit | lint + test | `@Requirement("REQ-F01")` en `LoginUserUseCase.invoke()` |
| **REQ-F01** | Selección de rol al registrarse (COMMERCE / CONSUMER) | `:feature:auth` | `AuthRepository` | `UserEntity.role` | `RegisterUserUseCaseTest.kt` | Unit | lint + test | `// @REQ-F01` en `AuthRepository.kt` |
| **REQ-F02** | El comercio publica alimentos no vendidos con descuentos | `:feature:products` | `PublishPackUseCase` | `PackDao` / `PackEntity` | `PublishPackUseCaseTest.kt` | Unit | lint + test | `@Requirement("REQ-F02")` en `PublishPackUseCase.invoke()` |
| **REQ-F02** | Validación: precio descuento < precio original | `:core:domain` | `PublishPackUseCase` | — | `PublishPackUseCaseTest.kt` | Unit | lint + test | `// @REQ-F02` en `PublishPackUseCase.kt` |
| **REQ-F02** | Pack con nombre, descripción, foto, cantidad | `:core:domain` | `FoodPack` (model) | `PackEntity` | `PublishPackUseCaseTest.kt` | Unit | lint + test | `// @REQ-F02` en `FoodPack.kt` y `PackEntity.kt` |
| **REQ-F03** | El usuario ve lista de productos en radio cercano (GPS) | `:feature:products` | `GetNearbyPacksUseCase` | `PackDao.getAvailablePacks()` | `GetNearbyPacksUseCaseTest.kt` (pendiente) | Unit | lint + test | `@Requirement("REQ-F03")` en `GetNearbyPacksUseCase.invoke()` |
| **REQ-F03** | Filtrado por radio configurable (default 5 km) | `:core:domain` | `GetNearbyPacksUseCase` | `CommerceEntity.latitud/longitud` | `GetNearbyPacksUseCaseTest.kt` (pendiente) | Unit | lint + test | `DEFAULT_RADIO_KM = 5.0` en `GetNearbyPacksUseCase.kt` |
| **REQ-F04** | El usuario reserva un pack de alimentos para ser retirado | `:feature:reservations` | `ReservePackUseCase` | `PackDao.reservePackAtomically()` / `ReservationDao.insertReservation()` | `ReservePackUseCaseTest.kt` | Unit + Concurrencia | lint + test | `@Requirement("REQ-F04")` en `ReservePackUseCase.invoke()` |
| **REQ-F04** | Estado AVAILABLE → RESERVED al reservar | `:core:data` | `PackDao.reservePackAtomically()` | `PackEntity.status` | `ReservePackUseCaseTest.kt` | Unit | lint + test | `// @REQ-F04` en `PackDao.kt` |
| **REQ-F05** | El encargado del local marca una reserva como "retirada" | `:feature:reservations` | `MarkReservationWithdrawnUseCase` | `ReservationDao.markAsWithdrawn()` | `MarkReservationWithdrawnUseCaseTest.kt` | Unit | lint + test | `@Requirement("REQ-F05")` en `MarkReservationWithdrawnUseCase.invoke()` |
| **REQ-F05** | Estado RESERVED → WITHDRAWN al marcar retirada | `:core:data` | `ReservationDao.markAsWithdrawn()` | `ReservationEntity.status` | `MarkReservationWithdrawnUseCaseTest.kt` | Unit | lint + test | `// @REQ-F05` en `ReservationDao.kt` |
| **REQ-F06** | El usuario cancela una reserva si ya no la desea | `:feature:reservations` | `CancelReservationUseCase` | `ReservationDao.cancelReservation()` | `CancelReservationUseCaseTest.kt` | Unit | lint + test | `@Requirement("REQ-F06")` en `CancelReservationUseCase.invoke()` |
| **REQ-F06** | Política: cancelación libre hasta antes del retiro | `:core:data` | `ReservationDao.cancelReservation()` | `ReservationEntity.status` | `CancelReservationUseCaseTest.kt` | Unit | lint + test | `// @REQ-F06` en `ReservationDao.kt` |

---

## RTM — Requerimientos No Funcionales

| ID | Descripción del Requerimiento | Mecanismo de verificación | Herramienta | Archivo / Config | Estado CI | Estado |
|----|-------------------------------|--------------------------|-------------|------------------|-----------|--------|
| **REQ-NF01** | Consistencia de reservas bajo concurrencia (no duplicar reservas) | Transacción atómica en Room + test de concurrencia con coroutines | `PackDao.reservePackAtomically()` + `kotlinx-coroutines-test` | `ReservationConcurrencyTest.kt` | lint + test | 🔄 Planificado |
| **REQ-NF01** | Test: 10 usuarios concurrentes → solo 1 reserva exitosa | `ReservationConcurrencyTest` | JUnit4 + Dispatchers.Default | `ReservationConcurrencyTest.kt` | test | 🔄 Planificado |
| **REQ-NF02** | APK no supera 50 MB para descarga por datos móviles | Medición del tamaño de APK release | Gradle build + CI artifact | `app/build.gradle.kts` (minify, ProGuard) | build job (futuro) | 🔄 Planificado |

---

## RTM — Trazabilidad a Nivel de Código (Anotación @Requirement)

| Archivo | Clase / Función | Requerimiento | Tipo de trazabilidad |
|---------|-----------------|---------------|----------------------|
| `RegisterUserUseCase.kt` | `invoke()` | REQ-F01 | `@Requirement` + `// @REQ-F01` |
| `LoginUserUseCase.kt` | `invoke()` | REQ-F01 | `@Requirement` + `// @REQ-F01` |
| `AuthRepository.kt` | Interface | REQ-F01 | `// @REQ-F01` en KDoc |
| `UserEntity.kt` | `UserEntity` | REQ-F01 | `// @REQ-F01` en KDoc |
| `UserDao.kt` | `insertUser()`, `getUserByEmail()` | REQ-F01 | `// @REQ-F01` en comentarios |
| `PublishPackUseCase.kt` | `invoke()` | REQ-F02 | `@Requirement` + `// @REQ-F02` |
| `PackRepository.kt` | `publishPack()` | REQ-F02 | `// @REQ-F02` en KDoc |
| `PackEntity.kt` | `PackEntity` | REQ-F02, REQ-F03, REQ-NF01 | `// @REQ-FXX` en KDoc de campos |
| `PackDao.kt` | `insertPack()`, `getAvailablePacks()` | REQ-F02, REQ-F03 | `// @REQ-F02`, `// @REQ-F03` |
| `GetNearbyPacksUseCase.kt` | `invoke()` | REQ-F03 | `@Requirement` + `// @REQ-F03` |
| `CommerceEntity.kt` | `latitud`, `longitud` | REQ-F03 | `// @REQ-F03` en campos |
| `ReservePackUseCase.kt` | `invoke()` | REQ-F04, REQ-NF01 | `@Requirement` + `// @REQ-F04`, `// @REQ-NF01` |
| `PackDao.kt` | `reservePackAtomically()` | REQ-NF01 | `// @REQ-NF01` en KDoc detallado |
| `ReservationEntity.kt` | `ReservationEntity` | REQ-F04, REQ-F05, REQ-F06, REQ-NF01 | `// @REQ-FXX` en KDoc de clase |
| `ReservationDao.kt` | `insertReservation()` | REQ-F04 | `// @REQ-F04` |
| `ReservationDao.kt` | `markAsWithdrawn()` | REQ-F05 | `// @REQ-F05` |
| `ReservationDao.kt` | `cancelReservation()` | REQ-F06 | `// @REQ-F06` |
| `MarkReservationWithdrawnUseCase.kt` | `invoke()` | REQ-F05 | `@Requirement` + `// @REQ-F05` |
| `CancelReservationUseCase.kt` | `invoke()` | REQ-F06 | `@Requirement` + `// @REQ-F06` |
| `PackStatus.kt` | `PackStatus enum` | REQ-F02, REQ-F04, REQ-F05, REQ-F06, REQ-NF01 | `// @REQ-FXX` en cada valor del enum |
| `ReservationStatus.kt` | `ReservationStatus enum` | REQ-F04, REQ-F05, REQ-F06 | `// @REQ-FXX` en cada valor del enum |

---

## RTM — Trazabilidad de Tests

| Archivo de Test | Requerimiento | Casos de Test | Tipo | Estado |
|-----------------|---------------|---------------|------|--------|
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenValidCommerceData_whenRegister_thenReturnsSuccessWithUser` | Unit | 🔄 Planificado |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenValidConsumerData_whenRegister_thenReturnsSuccessWithUser` | Unit | 🔄 Planificado |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenEmptyEmail_whenRegister_thenReturnsError` | Unit | 🔄 Planificado |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenPasswordTooShort_whenRegister_thenReturnsError` | Unit | 🔄 Planificado |
| `RegisterUserUseCaseTest.kt` | REQ-F01 | `givenDuplicateEmail_whenRegister_thenPropagatesRepositoryError` | Unit | 🔄 Planificado |
| `PublishPackUseCaseTest.kt` | REQ-F02 | `givenValidPack_whenPublish_thenReturnsSuccessWithStatusAvailable` | Unit | 🔄 Planificado |
| `PublishPackUseCaseTest.kt` | REQ-F02 | `givenPackWithPrices_whenPublish_thenDiscountPercentageIsCorrect` | Unit | 🔄 Planificado |
| `PublishPackUseCaseTest.kt` | REQ-F02 | `givenEmptyName_whenPublish_thenReturnsError` | Unit | 🔄 Planificado |
| `PublishPackUseCaseTest.kt` | REQ-F02 | `givenDiscountPriceHigherThanOriginal_whenPublish_thenReturnsError` | Unit | 🔄 Planificado |
| `GetNearbyPacksUseCaseTest.kt` | REQ-F03 | `givenUserLocation_whenGetNearbyPacks_thenReturnsFilteredList` | Unit | ❌ No iniciado |
| `ReservePackUseCaseTest.kt` | REQ-F04 | `givenAvailablePack_whenUserReserves_thenReservationIsCreated` | Unit | 🔄 Planificado |
| `ReservePackUseCaseTest.kt` | REQ-NF01 | `givenAlreadyReservedPack_whenSecondUserTries_thenReturnsError` | Unit | 🔄 Planificado |
| `ReservationConcurrencyTest.kt` | REQ-NF01 | `givenSingleAvailablePack_whenMultipleUsersReserveConcurrently_thenOnlyOneSucceeds` | Concurrencia | 🔄 Planificado |
| `MarkReservationWithdrawnUseCaseTest.kt` | REQ-F05 | `givenReservedReservation_whenCommerceMarksWithdrawn_thenStatusIsWithdrawn` | Unit | 🔄 Planificado |
| `MarkReservationWithdrawnUseCaseTest.kt` | REQ-F05 | `givenCancelledReservation_whenCommerceMarksWithdrawn_thenReturnsError` | Unit | 🔄 Planificado |
| `CancelReservationUseCaseTest.kt` | REQ-F06 | `givenActiveReservation_whenUserCancels_thenStatusIsCancelled` | Unit | 🔄 Planificado |
| `CancelReservationUseCaseTest.kt` | REQ-F06 | `givenWithdrawnReservation_whenUserCancels_thenReturnsError` | Unit | 🔄 Planificado |

---

## Mapa de Cobertura por Requerimiento

```
REQ-F01 → RegisterUserUseCase     → UserDao         → RegisterUserUseCaseTest (5 tests)  ✓ Trazado
REQ-F02 → PublishPackUseCase      → PackDao         → PublishPackUseCaseTest (4 tests)   ✓ Trazado
REQ-F03 → GetNearbyPacksUseCase   → PackDao         → GetNearbyPacksUseCaseTest (1 test) ⚠ Parcial
REQ-F04 → ReservePackUseCase      → PackDao+ResDao  → ReservePackUseCaseTest (2 tests)   ✓ Trazado
REQ-F05 → MarkWithdrawnUseCase    → ReservationDao  → MarkWithdrawnUseCaseTest (2 tests) ✓ Trazado
REQ-F06 → CancelReservationUC     → ReservationDao  → CancelReservationUseCaseTest (2t)  ✓ Trazado
REQ-NF01→ PackDao.atomically()    → Room txn        → ReservationConcurrencyTest (2 t)   ✓ Trazado
REQ-NF02→ Build config (APK size) → ProGuard/R8     → CI build artifact                 🔄 Planificado
```

---

*Generado automáticamente — última actualización: Mayo 2025*
*Mantener este archivo actualizado con cada merge a develop.*
