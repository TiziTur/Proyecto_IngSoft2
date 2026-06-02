# Reporte de Defectos — Aprovecha App
## Hito 4 · Ingeniería de Software II

**Fecha:** 2026-06-02  
**Versión analizada:** commit `98853b8`  
**Método de detección:** Revisión de código + análisis de cobertura de ramas  
**Total de defectos reportados:** 6

---

## Tabla de Defectos

| ID | Severidad | Módulo | Descripción breve | Estado |
|----|-----------|--------|-------------------|--------|
| DEF-001 | Alta | `AuthRepositoryImpl` | Password hashing inseguro con `hashCode()` | Abierto |
| DEF-002 | Media | `PackRepositoryImpl` | `getAvailablePacksNearby()` ignora coordenadas geográficas | Abierto |
| DEF-003 | Alta | `AuthRepositoryImpl` | `getCurrentUser()` siempre retorna null | Abierto |
| DEF-004 | Media | `ReservationRepositoryImpl` | `cancelReservation()` permite cancelar reservas ya CANCELLED | Abierto |
| DEF-005 | Baja | `RegisterUserUseCase` | No se valida el formato del email | Abierto |
| DEF-006 | Media | `PublishPackUseCase` | No se validan precios positivos | Abierto |

---

## DEF-001 · Password hashing inseguro con Java `hashCode()`

| Campo | Detalle |
|-------|---------|
| **ID** | DEF-001 |
| **Severidad** | Alta |
| **Módulo** | `core/data` |
| **Archivo** | `core/data/src/main/kotlin/com/aprovecha/app/data/repository/AuthRepositoryImpl.kt` |
| **Líneas** | 32 (register), 47 (login) |
| **Requisito afectado** | REQ-F01 |
| **Descripción** | La contraseña del usuario se almacena usando `password.hashCode().toString()`. El método `hashCode()` de Java/Kotlin no es una función criptográfica: (a) puede producir valores negativos; (b) su implementación puede variar entre versiones de JVM y plataformas; (c) no usa sal (salt), lo que permite ataques de tabla arcoíris. |
| **Código afectado** | `passwordHash = password.hashCode().toString()` |
| **Impacto** | Las contraseñas de los usuarios quedan expuestas si la base de datos es comprometida. Además, puede haber inconsistencias de autenticación entre versiones de la app. |
| **Pasos para reproducir** | 1. Registrar usuario con password `"abc123"`. 2. Ver en DB que el hash es el resultado de `"abc123".hashCode()` (un entero, no un hash criptográfico). |
| **Corrección sugerida** | Usar `BCrypt` o `PBKDF2` con sal. En Android/Kotlin: librería `bcrypt` de `at.favre.lib:bcrypt` o el módulo `Crypto` de Android Jetpack Security. |
| **Estado** | Abierto |

---

## DEF-002 · `getAvailablePacksNearby()` ignora completamente las coordenadas geográficas

| Campo | Detalle |
|-------|---------|
| **ID** | DEF-002 |
| **Severidad** | Media |
| **Módulo** | `core/data` |
| **Archivo** | `core/data/src/main/kotlin/com/aprovecha/app/data/repository/PackRepositoryImpl.kt` |
| **Líneas** | 30-35 |
| **Requisito afectado** | REQ-F03 |
| **Descripción** | La función `getAvailablePacksNearby(latitud, longitud, radioKm)` recibe parámetros geográficos pero los ignora por completo, llamando directamente a `packDao.getAvailablePacks()` sin ningún filtro. Retorna TODOS los packs disponibles sin importar la distancia. |
| **Código afectado** | `packDao.getAvailablePacks().map { list -> list.map { it.toDomain() } }` |
| **Impacto** | El requisito REQ-F03 (mostrar packs en un radio cercano) no se cumple. Un usuario en Córdoba capital vería packs de cualquier otra ciudad. |
| **Pasos para reproducir** | 1. Publicar packs en distintas coordenadas. 2. Llamar `getAvailablePacksNearby(-34.6, -58.4, 1.0)` (Buenos Aires, radio 1km). 3. Verificar que retorna también packs de Córdoba (-31.4, -64.18). |
| **Corrección sugerida** | Implementar filtro por fórmula de Haversine en memoria, o agregar una consulta SQL en Room que filtre por rango de coordenadas. |
| **Estado** | Abierto (documentado como MVP en el código, pero constituye una brecha funcional con REQ-F03) |

---

## DEF-003 · `getCurrentUser()` siempre retorna null — sin gestión de sesión

| Campo | Detalle |
|-------|---------|
| **ID** | DEF-003 |
| **Severidad** | Alta |
| **Módulo** | `core/data` |
| **Archivo** | `core/data/src/main/kotlin/com/aprovecha/app/data/repository/AuthRepositoryImpl.kt` |
| **Líneas** | 55-57 |
| **Requisito afectado** | REQ-F01 (sesión), REQ-F03 (usuario debe estar logueado para ver packs), REQ-F04 |
| **Descripción** | `getCurrentUser()` siempre retorna `null` y `logout()` es un no-op. No existe ningún mecanismo de persistencia de sesión: la app no puede conocer qué usuario está logueado entre pantallas o reinicios. |
| **Código afectado** | `override suspend fun getCurrentUser(): User? = null` |
| **Impacto** | Cualquier función que intente obtener el usuario logueado recibirá null, forzando al usuario a re-loguearse o provocando crashes/comportamiento indefinido. La funcionalidad de "mis reservas" (REQ-F04) y "publicar pack" (REQ-F02) dependen de conocer el userId actual. |
| **Pasos para reproducir** | 1. Hacer login con credenciales válidas. 2. Navegar a "Mis Reservas". 3. Verificar que el userId para filtrar reservas no puede obtenerse de `getCurrentUser()`. |
| **Corrección sugerida** | Implementar sesión en memoria con `companion object` o usar `DataStore` para persistir el userId entre reinicios. |
| **Estado** | Abierto |

---

## DEF-004 · `cancelReservation()` permite cancelar reservas ya en estado CANCELLED

| Campo | Detalle |
|-------|---------|
| **ID** | DEF-004 |
| **Severidad** | Media |
| **Módulo** | `core/data` |
| **Archivo** | `core/data/src/main/kotlin/com/aprovecha/app/data/repository/ReservationRepositoryImpl.kt` |
| **Líneas** | 71-76 |
| **Requisito afectado** | REQ-F06 |
| **Descripción** | La función `cancelReservation()` solo verifica que el estado NO sea `WITHDRAWN` para permitir la cancelación. Sin embargo, no verifica que el estado sea `RESERVED`. Esto significa que una reserva ya `CANCELLED` puede volver a "cancelarse", generando una actualización innecesaria en la BD y un posible estado inconsistente en `fechaActualizacion`. |
| **Código afectado** | `if (entity.status == ReservationStatus.WITHDRAWN.name)` (solo bloquea WITHDRAWN, no CANCELLED) |
| **Impacto** | Una reserva CANCELLED puede actualizarse innecesariamente. Si se agregan efectos secundarios a la cancelación (como liberar slots, enviar notificaciones), estos se ejecutarían incorrectamente dos veces. |
| **Pasos para reproducir** | 1. Cancelar una reserva (queda en CANCELLED). 2. Volver a llamar `cancelReservation()` con el mismo id. 3. Verificar que retorna `Result.Success` en lugar de `Result.Error`. |
| **Corrección sugerida** | Cambiar la condición a: `if (entity.status != ReservationStatus.RESERVED.name)` → retornar error. |
| **Estado** | Abierto |

---

## DEF-005 · `RegisterUserUseCase` no valida el formato del email

| Campo | Detalle |
|-------|---------|
| **ID** | DEF-005 |
| **Severidad** | Baja |
| **Módulo** | `core/domain` |
| **Archivo** | `core/domain/src/main/kotlin/com/aprovecha/app/domain/usecase/auth/RegisterUserUseCase.kt` |
| **Líneas** | 39-40 |
| **Requisito afectado** | REQ-F01 |
| **Descripción** | La validación del email solo verifica que el campo no esté en blanco (`isBlank()`). No se valida que el string tenga el formato de un email válido (ej: contiene `@`, tiene dominio, etc.). Esto permite registrar usuarios con emails inválidos como `"noesunEmail"`, `"@"` o `"a@"`. |
| **Código afectado** | `if (email.isBlank() || password.isBlank() || name.isBlank())` |
| **Impacto** | Los usuarios pueden registrarse con emails inválidos, lo que impide el envío de notificaciones/recuperación de contraseña en el futuro y genera datos inconsistentes. |
| **Pasos para reproducir** | 1. Llamar `RegisterUserUseCase("noesunEmail", "pass123", "Test", UserRole.CONSUMER)`. 2. Verificar que retorna `Result.Success` (no valida formato). |
| **Corrección sugerida** | Agregar validación con regex: `android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()` o `Patterns.EMAIL_ADDRESS` de Android. |
| **Estado** | Abierto |

---

## DEF-006 · `PublishPackUseCase` no valida que los precios sean positivos

| Campo | Detalle |
|-------|---------|
| **ID** | DEF-006 |
| **Severidad** | Media |
| **Módulo** | `core/domain` |
| **Archivo** | `core/domain/src/main/kotlin/com/aprovecha/app/domain/usecase/pack/PublishPackUseCase.kt` |
| **Líneas** | 25-33 |
| **Requisito afectado** | REQ-F02 |
| **Descripción** | La validación de precios solo verifica que `discountPrice >= originalPrice` (la condición de descuento real). No verifica que ambos precios sean positivos. Esto permite publicar packs con precios negativos (ej: `originalPrice = -100.0`, `discountPrice = -200.0`), lo que pasaría la validación porque `-200 < -100`. |
| **Código afectado** | Solo valida: `if (pack.discountPrice >= pack.originalPrice)` |
| **Impacto** | Un pack con precio negativo podría mostrarse en la UI con valores absurdos (ej: "Precio: -$200"). El cálculo `discountPercentage` en `FoodPack` también produciría valores mayores al 100% para precios negativos. |
| **Pasos para reproducir** | 1. Llamar `PublishPackUseCase` con `FoodPack(originalPrice = -100.0, discountPrice = -200.0, quantity = 1, name = "Test")`. 2. Verificar que retorna `Result.Success` porque `-200 < -100`. |
| **Corrección sugerida** | Agregar antes de las validaciones actuales: `if (pack.originalPrice <= 0 || pack.discountPrice <= 0) return Result.Error(...)` |
| **Estado** | Abierto |

---

## Resumen Ejecutivo

| Severidad | Cantidad |
|-----------|----------|
| Alta | 2 (DEF-001, DEF-003) |
| Media | 3 (DEF-002, DEF-004, DEF-006) |
| Baja | 1 (DEF-005) |
| **Total** | **6** |

Los defectos de severidad **Alta** (DEF-001 y DEF-003) representan problemas de seguridad y funcionalidad core que deben priorizarse. Los defectos de severidad **Media** afectan requisitos funcionales especificados (REQ-F03, REQ-F06, REQ-F02). El defecto de severidad **Baja** (DEF-005) representa una brecha de validación de datos que debería corregirse antes de producción.
