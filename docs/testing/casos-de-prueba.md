# Casos de Prueba Documentados — Aprovecha App
## Hito 4 · Ingeniería de Software II

**Total documentados:** 16 casos (8 caja negra + 8 caja blanca)  
**Framework:** JUnit 4 + MockK  
**Convención de nombre:** `given[Contexto]_when[Acción]_then[Resultado]`

---

## Caja Negra (Black Box)

> Los tests de caja negra verifican el comportamiento observable del sistema a partir de sus entradas y salidas, sin conocimiento del código interno.

---

### TC-01 · Login exitoso con credenciales válidas (Consumer)

| Campo | Valor |
|-------|-------|
| **ID** | TC-01 |
| **Técnica** | Caja Negra |
| **Módulo** | `LoginUserUseCase` |
| **Requisito** | REQ-F01 |
| **Archivo de test** | `LoginUserUseCaseTest.kt` |
| **Método** | `givenValidConsumerCredentials_whenLogin_thenReturnsSuccessWithUser` |
| **Precondiciones** | El repositorio retorna un usuario con rol CONSUMER |
| **Entradas** | email = `"user@test.com"`, password = `"password123"` |
| **Resultado esperado** | `Result.Success` con el usuario correcto |
| **Resultado obtenido** | `Result.Success` con `User(id=1, email="user@test.com", role=CONSUMER)` |
| **Estado** | ✅ PASA |

---

### TC-02 · Login con email vacío no llama al repositorio

| Campo | Valor |
|-------|-------|
| **ID** | TC-02 |
| **Técnica** | Caja Negra |
| **Módulo** | `LoginUserUseCase` |
| **Requisito** | REQ-F01 |
| **Archivo de test** | `LoginUserUseCaseTest.kt` |
| **Método** | `givenBlankEmail_whenLogin_thenReturnsErrorWithoutCallingRepository` |
| **Precondiciones** | Ninguna |
| **Entradas** | email = `""`, password = `"password123"` |
| **Resultado esperado** | `Result.Error`, repositorio NO es llamado |
| **Resultado obtenido** | `Result.Error`; `coVerify(exactly = 0)` pasa |
| **Estado** | ✅ PASA |

---

### TC-03 · Registro con email duplicado retorna error

| Campo | Valor |
|-------|-------|
| **ID** | TC-03 |
| **Técnica** | Caja Negra |
| **Módulo** | `RegisterUserUseCase` |
| **Requisito** | REQ-F01 |
| **Archivo de test** | `RegisterUserUseCaseTest.kt` |
| **Método** | `givenDuplicateEmail_whenRegister_thenPropagatesRepositoryError` |
| **Precondiciones** | El repositorio lanza error de email duplicado |
| **Entradas** | email = `"dup@test.com"`, password = `"password123"`, name = `"Test"` |
| **Resultado esperado** | `Result.Error` propagado desde el repositorio |
| **Resultado obtenido** | `Result.Error` |
| **Estado** | ✅ PASA |

---

### TC-04 · Publicar pack con nombre vacío retorna error sin llamar al repositorio

| Campo | Valor |
|-------|-------|
| **ID** | TC-04 |
| **Técnica** | Caja Negra |
| **Módulo** | `PublishPackUseCase` |
| **Requisito** | REQ-F02 |
| **Archivo de test** | `PublishPackUseCaseTest.kt` |
| **Método** | `givenEmptyName_whenPublish_thenReturnsError` |
| **Precondiciones** | Ninguna |
| **Entradas** | `FoodPack(name = "", originalPrice = 500.0, discountPrice = 200.0, quantity = 3)` |
| **Resultado esperado** | `Result.Error`, repositorio NO es llamado |
| **Resultado obtenido** | `Result.Error`; `coVerify(exactly = 0)` pasa |
| **Estado** | ✅ PASA |

---

### TC-05 · Publicar pack con precio de descuento ≥ precio original retorna error

| Campo | Valor |
|-------|-------|
| **ID** | TC-05 |
| **Técnica** | Caja Negra |
| **Módulo** | `PublishPackUseCase` |
| **Requisito** | REQ-F02 |
| **Archivo de test** | `PublishPackUseCaseTest.kt` |
| **Método** | `givenDiscountPriceHigherThanOriginal_whenPublish_thenReturnsError` |
| **Precondiciones** | Ninguna |
| **Entradas** | `originalPrice = 200.0`, `discountPrice = 200.0` (igual al original) |
| **Resultado esperado** | `Result.Error` (sin descuento real) |
| **Resultado obtenido** | `Result.Error` |
| **Estado** | ✅ PASA |

---

### TC-06 · Ver packs disponibles cercanos retorna lista con todos los packs

| Campo | Valor |
|-------|-------|
| **ID** | TC-06 |
| **Técnica** | Caja Negra |
| **Módulo** | `GetNearbyPacksUseCase` |
| **Requisito** | REQ-F03 |
| **Archivo de test** | `GetNearbyPacksUseCaseTest.kt` |
| **Método** | `givenAvailablePacks_whenInvokedWithCoordinates_thenEmitsList` |
| **Precondiciones** | Repositorio retorna 3 packs con estado AVAILABLE |
| **Entradas** | latitud = `-31.4`, longitud = `-64.18`, radioKm = `5.0` |
| **Resultado esperado** | Flow emite lista con 3 packs, todos con status AVAILABLE |
| **Resultado obtenido** | Lista de 3 packs, `result.all { it.status == PackStatus.AVAILABLE }` = true |
| **Estado** | ✅ PASA |

---

### TC-07 · Reservar pack disponible crea reserva en estado RESERVED

| Campo | Valor |
|-------|-------|
| **ID** | TC-07 |
| **Técnica** | Caja Negra |
| **Módulo** | `ReservePackUseCase` |
| **Requisito** | REQ-F04 |
| **Archivo de test** | `ReservePackUseCaseTest.kt` |
| **Método** | `givenAvailablePack_whenUserReserves_thenReservationIsCreated` |
| **Precondiciones** | Pack con id=1 está disponible |
| **Entradas** | packId = `1L`, userId = `42L` |
| **Resultado esperado** | `Result.Success` con `Reservation(status=RESERVED, packId=1, userId=42)` |
| **Resultado obtenido** | `Result.Success` con estado RESERVED |
| **Estado** | ✅ PASA |

---

### TC-08 · Cancelar reserva RESERVED cambia estado a CANCELLED

| Campo | Valor |
|-------|-------|
| **ID** | TC-08 |
| **Técnica** | Caja Negra |
| **Módulo** | `CancelReservationUseCase` |
| **Requisito** | REQ-F06 |
| **Archivo de test** | `CancelReservationUseCaseTest.kt` |
| **Método** | `givenActiveReservation_whenUserCancels_thenStatusIsCancelled` |
| **Precondiciones** | Reserva con id=1 en estado RESERVED existe |
| **Entradas** | reservationId = `1L` |
| **Resultado esperado** | `Result.Success` con `Reservation(status=CANCELLED)` |
| **Resultado obtenido** | `Result.Success` con status CANCELLED |
| **Estado** | ✅ PASA |

---

## Caja Blanca (White Box)

> Los tests de caja blanca verifican caminos internos específicos del código: ramas condicionales, llamadas a colaboradores y comportamiento de transacciones.

---

### TC-09 · Login verifica rama de contraseña incorrecta (hash mismatch)

| Campo | Valor |
|-------|-------|
| **ID** | TC-09 |
| **Técnica** | Caja Blanca |
| **Módulo** | `AuthRepositoryImpl` |
| **Requisito** | REQ-F01 |
| **Archivo de test** | `AuthRepositoryImplTest.kt` |
| **Método** | `` `Given wrong password When login called Then returns Error wrong password` `` |
| **Camino cubierto** | Rama: `entity.passwordHash != password.hashCode().toString()` → true |
| **Precondiciones** | Usuario existe con `passwordHash = "correctPass".hashCode().toString()` |
| **Entradas** | email del usuario, password = `"wrongPass"` |
| **Resultado esperado** | `Result.Error` con mensaje que contiene `"Contraseña"` |
| **Resultado obtenido** | `Result.Error`; mensaje verifica con `contains("Contraseña")` |
| **Estado** | ✅ PASA |

---

### TC-10 · Registro consulta DAO getUserByEmail antes de insertar

| Campo | Valor |
|-------|-------|
| **ID** | TC-10 |
| **Técnica** | Caja Blanca |
| **Módulo** | `AuthRepositoryImpl` |
| **Requisito** | REQ-F01 |
| **Archivo de test** | `AuthRepositoryImplTest.kt` |
| **Método** | `` `Given successful register When insert called Then DAO insertUser called once` `` |
| **Camino cubierto** | `getUserByEmail` → null → `insertUser` llamado exactamente 1 vez |
| **Precondiciones** | Email no existe en la BD (mock retorna null) |
| **Entradas** | email = `"test@test.com"`, password = `"pass"`, name = `"User"` |
| **Resultado esperado** | `coVerify(exactly = 1) { userDao.insertUser(any()) }` pasa |
| **Resultado obtenido** | Verificación de mock satisfecha |
| **Estado** | ✅ PASA |

---

### TC-11 · createReservation usa reservePackAtomically() — rama pack ya reservado (rows=0)

| Campo | Valor |
|-------|-------|
| **ID** | TC-11 |
| **Técnica** | Caja Blanca |
| **Módulo** | `ReservationRepositoryImpl` |
| **Requisito** | REQ-NF01 |
| **Archivo de test** | `ReservationRepositoryImplTest.kt` |
| **Método** | `` `Given pack already reserved When createReservation called Then returns Error` `` |
| **Camino cubierto** | `reservePackAtomically(packId)` retorna 0 → rama `rowsAffected == 0` → `Result.Error` con `"REQ-NF01"` |
| **Precondiciones** | `packDao.reservePackAtomically(1L)` mockea retorno `0` |
| **Entradas** | packId = `1L`, userId = `99L` |
| **Resultado esperado** | `Result.Error` con mensaje que contiene `"REQ-NF01"` |
| **Resultado obtenido** | `Result.Error`; mensaje verifica con `contains("REQ-NF01")` |
| **Estado** | ✅ PASA |

---

### TC-12 · getAvailablePacksNearby() ignora coordenadas en MVP y retorna todos los packs

| Campo | Valor |
|-------|-------|
| **ID** | TC-12 |
| **Técnica** | Caja Blanca |
| **Módulo** | `PackRepositoryImpl` |
| **Requisito** | REQ-F03 |
| **Archivo de test** | `PackRepositoryImplTest.kt` |
| **Método** | `` `Given available packs When getAvailablePacksNearby Then emits mapped domain list` `` |
| **Camino cubierto** | `packDao.getAvailablePacks()` es llamado directamente (sin filtro por coords) → mapeo a dominio |
| **Precondiciones** | DAO retorna 3 PackEntity con status AVAILABLE |
| **Entradas** | latitud = `-31.4`, longitud = `-64.18`, radioKm = `5.0` |
| **Resultado esperado** | Lista de 3 FoodPack con status AVAILABLE, nombre y precio mapeados correctamente |
| **Resultado obtenido** | 3 items; `result[0].name == "Pack Panadería"` y `result[0].status == PackStatus.AVAILABLE` |
| **Estado** | ✅ PASA |

---

### TC-13 · markAsWithdrawn valida estado RESERVED antes de cambiar a WITHDRAWN

| Campo | Valor |
|-------|-------|
| **ID** | TC-13 |
| **Técnica** | Caja Blanca |
| **Módulo** | `ReservationRepositoryImpl` |
| **Requisito** | REQ-F05 |
| **Archivo de test** | `ReservationRepositoryImplTest.kt` |
| **Método** | `` `Given CANCELLED reservation When markAsWithdrawn called Then returns Error` `` |
| **Camino cubierto** | `entity.status != ReservationStatus.RESERVED.name` → true → `Result.Error` con mensaje `"RESERVED"` |
| **Precondiciones** | Reserva existe con status = "CANCELLED" |
| **Entradas** | reservationId = `1L` |
| **Resultado esperado** | `Result.Error` con mensaje que contiene `"RESERVED"` |
| **Resultado obtenido** | `Result.Error`; `msg.contains("RESERVED")` = true |
| **Estado** | ✅ PASA |

---

### TC-14 · cancelReservation valida que el estado no sea WITHDRAWN antes de cancelar

| Campo | Valor |
|-------|-------|
| **ID** | TC-14 |
| **Técnica** | Caja Blanca |
| **Módulo** | `ReservationRepositoryImpl` |
| **Requisito** | REQ-F06 |
| **Archivo de test** | `ReservationRepositoryImplTest.kt` |
| **Método** | `` `Given WITHDRAWN reservation When cancelReservation called Then returns Error` `` |
| **Camino cubierto** | `entity.status == ReservationStatus.WITHDRAWN.name` → true → `Result.Error` con `"cancelar"` |
| **Precondiciones** | Reserva existe con status = "WITHDRAWN" |
| **Entradas** | reservationId = `1L` |
| **Resultado esperado** | `Result.Error` con mensaje que contiene `"cancelar"` |
| **Resultado obtenido** | `Result.Error`; `msg.contains("cancelar")` = true |
| **Estado** | ✅ PASA |

---

### TC-15 · publishPack fuerza status = AVAILABLE independientemente del input

| Campo | Valor |
|-------|-------|
| **ID** | TC-15 |
| **Técnica** | Caja Blanca |
| **Módulo** | `PublishPackUseCase` |
| **Requisito** | REQ-F02 |
| **Archivo de test** | `PublishPackUseCaseTest.kt` |
| **Método** | `givenValidPack_whenPublish_thenReturnsSuccessWithStatusAvailable` |
| **Camino cubierto** | `packRepository.publishPack(pack.copy(status = PackStatus.AVAILABLE))` — siempre fuerza AVAILABLE |
| **Precondiciones** | Pack válido con nombre, precios y cantidad correctos |
| **Entradas** | `FoodPack(name = "Pack Panadería Tarde", originalPrice = 500.0, discountPrice = 200.0, quantity = 3)` |
| **Resultado esperado** | `coVerify { packRepository.publishPack(match { it.status == PackStatus.AVAILABLE }) }` pasa |
| **Resultado obtenido** | Verificación de mock satisfecha |
| **Estado** | ✅ PASA |

---

### TC-16 · Concurrencia: 10 usuarios intentan el mismo pack, solo 1 tiene éxito (REQ-NF01)

| Campo | Valor |
|-------|-------|
| **ID** | TC-16 |
| **Técnica** | Caja Blanca |
| **Módulo** | `ReservePackUseCase` + `ReservationRepository` |
| **Requisito** | REQ-NF01 |
| **Archivo de test** | `ReservationConcurrencyTest.kt` |
| **Método** | `givenSingleAvailablePack_whenMultipleUsersReserveConcurrently_thenOnlyOneSucceeds` |
| **Camino cubierto** | `AtomicInteger.compareAndSet(0, 1)` — solo el primer caller tiene éxito; resto reciben Error |
| **Precondiciones** | Pack id=1 disponible; 10 coroutines en `Dispatchers.Default` |
| **Entradas** | 10 llamadas concurrentes a `reservePackUseCase(packId=1L, userId=1L..10L)` |
| **Resultado esperado** | Exactamente 1 `Result.Success` y 9 `Result.Error` |
| **Resultado obtenido** | `successResults.size == 1`; `errorResults.size == 9` |
| **Estado** | ✅ PASA |

---

## Resumen de Cobertura de Requisitos

| Requisito | Técnica BB | Técnica BW | Total |
|-----------|-----------|-----------|-------|
| REQ-F01 | TC-01, TC-02, TC-03 | TC-09, TC-10 | 5 |
| REQ-F02 | TC-04, TC-05 | TC-15 | 3 |
| REQ-F03 | TC-06 | TC-12 | 2 |
| REQ-F04 | TC-07 | TC-11 | 2 |
| REQ-F05 | — | TC-13 | 1 |
| REQ-F06 | TC-08 | TC-14 | 2 |
| REQ-NF01 | — | TC-16 | 1 |
| **Total** | **8** | **8** | **16** |
