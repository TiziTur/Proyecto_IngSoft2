# Plan de Pruebas — Aprovecha App
## Hito 4 · Ingeniería de Software II

---

## 1. Introducción

Este documento describe el plan de pruebas para el sistema **Aprovecha**, una aplicación Android que conecta comercios con excedente de alimentos y consumidores que desean adquirirlos a precio reducido. El plan sigue lineamientos del estándar IEEE 829 y la norma IEEE 730 (SQA).

---

## 2. Alcance

### 2.1 Módulos bajo prueba

| Módulo | Paquete | Descripción |
|--------|---------|-------------|
| `core/domain` | `com.aprovecha.app.domain` | Casos de uso de negocio (auth, packs, reservas) |
| `core/data` | `com.aprovecha.app.data` | Repositorios e implementaciones de acceso a datos |
| `feature/products` | `com.aprovecha.app.feature.products` | ViewModel y estado UI del consumidor |
| `feature/reservations` | `com.aprovecha.app.feature.reservations` | ViewModel y estado UI del comercio/reservas |

### 2.2 Módulos excluidos

- `app/` — punto de entrada Android, Hilt, navegación (sin lógica testeable con unit tests)
- `core/ui` — solo tema y colores (sin lógica)
- Composables de Jetpack Compose (excluidos explícitamente en JaCoCo)
- Código generado por Room, Hilt y Kotlin compiler

### 2.3 Requisitos cubiertos

| Requisito | Descripción |
|-----------|-------------|
| REQ-F01 | Registro, login y gestión de sesión persistente (SessionManager + DataStore) |
| REQ-F02 | Publicación de packs de alimentos con descuento |
| REQ-F03 | Listado de packs disponibles cercanos al usuario |
| REQ-F04 | Reserva de un pack por parte de un consumidor |
| REQ-F05 | Marcado de reserva como retirada por el comercio |
| REQ-F06 | Cancelación de reserva por el consumidor |
| REQ-F07 | Favoritos: marcar/desmarcar packs y ver lista de favoritos |
| REQ-NF01 | Garantía de exclusividad: un pack solo puede reservarse una vez |

---

## 3. Estrategia de Pruebas

### 3.1 Niveles de prueba

| Nivel | Descripción | Herramientas |
|-------|-------------|--------------|
| **Unitaria** | Prueba de clases individuales con dependencias mockeadas | JUnit 4 + MockK |
| **Integración liviana** | Verificación de colaboración entre capas (use case ↔ repositorio) | JUnit 4 + MockK |
| **Concurrencia** | Simulación de múltiples coroutines sobre el mismo recurso | kotlinx-coroutines-test |

### 3.2 Técnicas de diseño

| Técnica | Descripción | Aplicación |
|---------|-------------|------------|
| **Caja Negra** | Prueba de comportamiento externo según especificación; no requiere conocimiento de la implementación | Use cases, ViewModels |
| **Caja Blanca** | Prueba de caminos internos, ramas de código, llamadas a colaboradores | Repositorios, validaciones internas |

### 3.3 Herramientas

| Herramienta | Versión | Rol |
|-------------|---------|-----|
| JUnit 4 | 4.13.2 | Framework principal de testing |
| MockK | 1.13.x | Mocking de dependencias en Kotlin |
| kotlinx-coroutines-test | 1.7.x | Testing de coroutines (runTest, advanceUntilIdle) |
| JaCoCo | Gradle plugin | Medición de cobertura de código |
| Android Gradle Plugin | 8.x | Ejecución de `testDebugUnitTest` |

---

## 4. Criterios de Prueba

### 4.1 Criterios de entrada (para iniciar las pruebas)

- El código compila sin errores
- Los módulos `core/domain` y `core/data` tienen sus tests escritos
- Las dependencias de MockK y coroutines-test están declaradas en `build.gradle.kts`

### 4.2 Criterios de salida (para considerar las pruebas completas)

- Todos los tests automatizados pasan (BUILD SUCCESSFUL) ✅
- Cobertura de instrucciones ≥ 70% en módulos principales ✅ (84.73% global)
- Cobertura de ramas ≥ 60% en módulos principales ✅ (80.41% global)
- Al menos 15 casos de prueba documentados (caja negra + caja blanca) ✅ (16 casos en casos-de-prueba.md, 121 tests automatizados)
- Al menos 5 defectos formalmente reportados ✅ (6 defectos, 1 resuelto)

### 4.3 Criterios de cobertura (IEEE 730 §5.3)

```
Instrucciones (INSTRUCTION): mínimo 70%
Ramas (BRANCH):              mínimo 60%
```

Configurado en `jacoco.gradle.kts` y verificado con `./gradlew jacocoTestCoverageVerification`.

---

## 5. Estructura de Archivos de Prueba

```
core/domain/src/test/
  ├── LoginUserUseCaseTest.kt               (8 tests  — REQ-F01)
  ├── RegisterUserUseCaseTest.kt            (6 tests  — REQ-F01)
  ├── PublishPackUseCaseTest.kt             (5 tests  — REQ-F02)
  ├── GetNearbyPacksUseCaseTest.kt          (5 tests  — REQ-F03)
  ├── ReservePackUseCaseTest.kt             (2 tests  — REQ-F04)
  ├── MarkReservationWithdrawnUseCaseTest.kt (2 tests — REQ-F05)
  ├── CancelReservationUseCaseTest.kt       (2 tests  — REQ-F06)
  └── ReservationConcurrencyTest.kt         (2 tests  — REQ-NF01)
                                             Subtotal: 32 tests

core/data/src/test/
  ├── AuthRepositoryImplTest.kt             (13 tests — REQ-F01, sesión)
  ├── PackRepositoryImplTest.kt             (12 tests — REQ-F02, REQ-F03)
  ├── ReservationRepositoryImplTest.kt      (13 tests — REQ-F04, REQ-F05, REQ-F06, REQ-NF01)
  ├── FavoriteRepositoryImplTest.kt         (4 tests  — REQ-F07)
  └── SessionManagerTest.kt                 (4 tests  — REQ-F01 sesión)
                                             Subtotal: 46 tests

feature/auth/src/test/
  ├── AuthViewModelTest.kt                  (12 tests — REQ-F01)
  └── ProfileViewModelTest.kt              (3 tests  — REQ-F01 sesión/logout)
                                             Subtotal: 15 tests

feature/products/src/test/
  └── ProductsViewModelTest.kt              (10 tests — REQ-F03, REQ-F04, REQ-F07)
                                             Subtotal: 10 tests

feature/reservations/src/test/
  └── ReservationsViewModelTest.kt          (18 tests — REQ-F02, REQ-F05, REQ-F06, sesión)
                                             Subtotal: 18 tests
```

**Total: 121 tests automatizados**

---

## 6. Comandos de Ejecución

```bash
# Ejecutar todos los tests
./gradlew test testDebugUnitTest

# Tests por módulo
./gradlew :core:domain:test
./gradlew :core:data:testDebugUnitTest
./gradlew :feature:products:testDebugUnitTest
./gradlew :feature:reservations:testDebugUnitTest

# Generar reporte de cobertura
./gradlew jacocoTestReport

# Verificar umbral de cobertura
./gradlew jacocoTestCoverageVerification
```

---

## 7. Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|---------|------------|
| Cobertura insuficiente en capa de datos | Baja | Alto | JaCoCo configurado con umbral 70%; CI falla si no se cumple |
| Tests dependientes de tiempo (LocalDateTime.now()) | Media | Medio | Se usa MockK para aislar; tiempo solo en comparaciones de estado |
| Flakiness en tests de concurrencia | Baja | Medio | Se usa AtomicInteger determinista en lugar de timing real |
| Cambios de API rompen tests | Media | Alto | Tests aislados con mocks; solo cambian cuando cambia el contrato |

---

## 8. Cronograma

| Actividad | Estado |
|-----------|--------|
| Implementación de casos de prueba unitarios | ✅ Completado |
| Configuración de JaCoCo | ✅ Completado |
| Verificación de cobertura ≥ 70% | ✅ Completado (84.73% instrucciones / 80.41% ramas) |
| Documentación de plan de pruebas | ✅ Completado |
| Documentación de casos de prueba | ✅ Completado (16 casos) |
| Reporte de defectos | ✅ Completado (6 defectos, 1 resuelto) |
| Tests de SessionManager (REQ-F01 sesión) | ✅ Completado (SessionManagerTest + AuthRepositoryImplTest) |
| Tests de Favoritos (REQ-F07) | ✅ Completado (FavoriteRepositoryImplTest + ProductsViewModelTest) |
| Tests de Perfil/Logout | ✅ Completado (ProfileViewModelTest) |
| Tests de Reservas Pendientes | ✅ Completado (ReservationsViewModelTest expandido) |

---

## 9. Métricas Finales (IEEE 730 §5.3)

### 9.1 Líneas de Código (LOC) y Complejidad Ciclomática (CC)

Obtenidas de los reportes Detekt (`build/reports/detekt/`) del último build (`./gradlew detekt`):

| Módulo | LOC | SLOC | CC Total | Code Smells |
|--------|-----|------|----------|-------------|
| `:app` | 241 | 199 | 7 | 0 |
| `:core:common` | 53 | 21 | 1 | 0 |
| `:core:domain` | 1,455 | 750 | 77 | 0 |
| `:core:data` | 1,447 | 905 | 115 | 0 |
| `:feature:auth` | 876 | 740 | 52 | 0 |
| `:feature:products` | 848 | 699 | 47 | 0 |
| `:feature:reservations` | 1,040 | 845 | 83 | 0 |
| **TOTAL** | **5,960** | **4,159** | **382** | **0** |

- **LOC**: líneas totales (incluye comentarios y espacios en blanco).
- **SLOC**: líneas de código fuente (sin comentarios ni líneas en blanco).
- **CC (mcc)**: complejidad ciclomática total del módulo (suma de todas las funciones). Umbral por función: ≤ 10 (configurado en Detekt, el CI falla si se supera).
- **Code Smells**: 0 — ningún issue crítico detectado por Detekt 1.23.8.

### 9.2 Maintainability Index (MI)

Detekt no provee MI directamente. Los indicadores equivalentes disponibles son:

| Indicador | Valor | Estado |
|-----------|-------|--------|
| CC máxima por función | ≤ 10 (umbral Detekt) | ✅ 0 violaciones |
| Code smells (todos los módulos) | 0 | ✅ Limpio |
| Complejidad cognitiva global | 190 | ✅ Bajo por función |

El MI es **alto** según los criterios IEEE 730 §5.3: CC ≤ 10 por función, sin deuda técnica activa detectada por análisis estático, 0 code smells en todos los módulos.

### 9.3 Cobertura de Código (JaCoCo)

| Métrica | Resultado | Umbral IEEE 730 §5.3 | Estado |
|---------|-----------|----------------------|--------|
| Instrucciones (INSTRUCTION) | **84.73%** | ≥ 70% | ✅ Supera |
| Ramas (BRANCH) | **80.41%** | ≥ 60% | ✅ Supera |

Ver `build/reports/jacoco/jacoco-summary.md` para el detalle por módulo.

### 9.4 Resumen de Defectos

| Severidad | Total | Resueltos | Abiertos |
|-----------|-------|-----------|----------|
| Alta | 2 | 1 (DEF-003) | 1 (DEF-001) |
| Media | 3 | 0 | 3 (DEF-002, DEF-004, DEF-006) |
| Baja | 1 | 0 | 1 (DEF-005) |
| **Total** | **6** | **1** | **5** |

Densidad de defectos: 6 defectos / 4.159 KLOC ≈ **1.44 defectos/KLOC** (dentro del umbral aceptable para MVP académico).
