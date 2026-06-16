# PLAN DE ASEGURAMIENTO DE CALIDAD DE SOFTWARE
## IEEE Std 730-2014

| Campo | Valor |
|-------|-------|
| **Proyecto** | Aprovecha! |
| **Versión** | 2.0 |
| **Fecha** | Junio 2026 |
| **Equipo** | Gang of Four |
| **Materia** | Ingeniería de Software II — IUA |
| **Carrera** | Ingeniería en Sistemas de Información |
| **Sistema** | Gestión de Rescate de Alimentos — App Android |

---

## Tabla de Contenidos

1. [Propósito y Alcance](#1-propósito-y-alcance)
2. [Descripción del Producto](#2-descripción-del-producto)
3. [Gestión de Calidad del Software](#3-gestión-de-calidad-del-software)
4. [Análisis Estático de Código](#4-análisis-estático-de-código)
5. [Plan de Verificación y Validación](#5-plan-de-verificación-y-validación)
6. [Pipeline de Integración Continua (CI/CD)](#6-pipeline-de-integración-continua-cicd)
7. [Gestión de la Trazabilidad](#7-gestión-de-la-trazabilidad)
8. [Gestión de Riesgos de Calidad](#8-gestión-de-riesgos-de-calidad)
9. [Métricas de Calidad y Reporte](#9-métricas-de-calidad-y-reporte)
10. [Glosario](#10-glosario)
11. [Historial de Revisiones](#11-historial-de-revisiones)

---

## 1. Propósito y Alcance

### 1.1 Propósito

Este documento define el Plan de Aseguramiento de la Calidad del Software (Software Quality Assurance Plan — SQAP) para el proyecto **Aprovecha!**, elaborado conforme al estándar **IEEE Std 730-2014**. Su finalidad es establecer los procesos, actividades, herramientas y métricas que garantizarán la calidad del producto a lo largo de su ciclo de vida de desarrollo.

El proyecto Aprovecha! es un sistema de gestión de rescate de alimentos desarrollado como aplicación Android nativa (Kotlin + Jetpack Compose), cuyo objetivo es conectar comercios locales con consumidores interesados en adquirir alimentos no vendidos a precio reducido, contribuyendo a la reducción del desperdicio alimentario.

### 1.2 Alcance del Plan

Este SQAP aplica a todas las actividades de ingeniería de software del proyecto Aprovecha!, incluyendo:

- Diseño y arquitectura del sistema (MVVM + Clean Architecture)
- Implementación de los **7 requerimientos funcionales** (REQ-F01 a REQ-F07)
- Implementación de los **2 requerimientos no funcionales** (REQ-NF01, REQ-NF02)
- Actividades de verificación y validación (**121 tests** automatizados en 4 niveles)
- Análisis estático de código con Detekt
- Integración continua mediante GitHub Actions
- Gestión de la trazabilidad mediante RTM y anotaciones en código

### 1.3 Documentos de Referencia

- IEEE Std 730-2014 — Software Quality Assurance Processes
- IEEE Std 830-1998 — Software Requirements Specifications
- IEEE Std 1012-2016 — Software Verification and Validation
- IEEE Std 829-1998 — Software Test Documentation
- [`RTM_Aprovecha.md`](./RTM_Aprovecha.md) — Matriz de Trazabilidad v3.1
- [`docs/testing/plan-de-pruebas.md`](./docs/testing/plan-de-pruebas.md) — Plan de Pruebas (IEEE 829)
- [`docs/testing/reporte-defectos.md`](./docs/testing/reporte-defectos.md) — Reporte de Defectos

---

## 2. Descripción del Producto

### 2.1 Problema que Resuelve

El desperdicio de alimentos en comercios locales (panaderías, verdulerías, etc.) que terminan en la basura al final del día representa un problema social, económico y ambiental significativo. Aprovecha! actúa como puente entre comercios con excedentes y consumidores dispuestos a adquirirlos a precio reducido.

### 2.2 Usuarios Objetivo

- **Comercios gastronómicos**: panaderías, verdulerías, restaurantes, cafeterías
- **Consumidores finales**: usuarios interesados en adquirir alimentos sustentables a menor precio

### 2.3 Stack Tecnológico

| Componente | Tecnología |
|-----------|-----------|
| Lenguaje | Kotlin 2.0.21 |
| UI Framework | Jetpack Compose + Material Design 3 |
| Arquitectura | MVVM + Clean Architecture (multi-módulo) |
| Persistencia local | Room 2.6.1 (SQLite) |
| Sesión | Jetpack DataStore (SessionManager) |
| Backend | Room/mock (MVP) — Node.js + PostgreSQL (futuro) |
| Inyección de deps. | Hilt 2.51.1 |
| Navegación | Navigation Compose 2.8.5 |
| Control de versiones | Git + GitHub (GitFlow) |
| IDE | Android Studio Hedgehog+ |

### 2.4 Módulos del Proyecto

| Módulo | Responsabilidad |
|--------|----------------|
| `:app` | Entry point, NavGraph, Application Hilt |
| `:core:common` | Anotación `@Requirement`, `Result<T>` sealed class, utilidades |
| `:core:domain` | Modelos de dominio, interfaces de repositorios, UseCases, `FavoriteRepository` |
| `:core:data` | Room entities, DAOs, Database, implementaciones de repos, `SessionManager` (DataStore), `FavoriteRepositoryImpl` |
| `:core:ui` | Tema Material 3, colores (verde/naranja), tipografías, componentes compartidos |
| `:feature:auth` | Pantallas y ViewModel de Login/Registro/Perfil (REQ-F01 + sesión persistente) |
| `:feature:products` | Listado de packs disponibles, detalle, favoritos (REQ-F03, REQ-F07) |
| `:feature:reservations` | Publicación de packs, gestión de reservas, reservas pendientes (REQ-F02, REQ-F04, REQ-F05, REQ-F06) |

---

## 3. Gestión de Calidad del Software

### 3.1 Organización del Equipo

El equipo de desarrollo está compuesto por los integrantes del grupo **Gang of Four**. Todos los miembros tienen responsabilidades de desarrollo y calidad. Las actividades de QA son transversales a todos los roles.

### 3.2 Requerimientos del Producto

| ID | Descripción | Tipo | Prioridad | Estado |
|----|-------------|------|-----------|--------|
| **REQ-F01** | Registro y login en la plataforma (comercio y consumidor); sesión persistente con DataStore | Funcional | Alta | ✅ Implementado |
| **REQ-F02** | El comercio publica packs de alimentos con descuento | Funcional | Alta | ✅ Implementado |
| **REQ-F03** | El consumidor ve packs disponibles en radio cercano | Funcional | Alta | ✅ Implementado |
| **REQ-F04** | El usuario reserva un pack de alimentos para retirar | Funcional | Alta | ✅ Implementado |
| **REQ-F05** | El encargado marca una reserva como retirada | Funcional | Alta | ✅ Implementado |
| **REQ-F06** | El usuario cancela una reserva si no la desea | Funcional | Media | ✅ Implementado |
| **REQ-F07** | El consumidor marca/desmarca packs como favoritos y ve su lista | Funcional | Media | ✅ Implementado |
| **REQ-NF01** | Consistencia de reservas bajo concurrencia simultánea (transacción atómica Room) | No Funcional | Alta | ✅ Implementado |
| **REQ-NF02** | APK no debe superar 50 MB para descarga por datos móviles | No Funcional | Media | ⚠️ Parcial |

### 3.3 Estrategia de Branching (GitFlow)

El proyecto utiliza GitFlow para garantizar la trazabilidad entre branches y requerimientos:

- **`main`**: rama de producción estable — solo recibe merges de develop o hotfix
- **`develop`**: rama de integración continua — objetivo de todos los PRs
- **`feature/REQ-FXX-descripcion`**: una rama por requerimiento funcional
- **`hotfix/descripcion`**: correcciones urgentes sobre main

**Convención de commits**: `feat(REQ-F01): implement commerce registration screen`

Cada Pull Request hacia develop dispara el pipeline de CI (Lint + Tests).

---

## 4. Análisis Estático de Código

### 4.1 Herramientas

El análisis estático en Kotlin/Compose es el equivalente funcional de la combinación Radon (complejidad ciclomática) + linter (estilo) usada en proyectos Python. Las herramientas seleccionadas son:

| Herramienta | Equivalente Python | Función |
|------------|-------------------|---------|
| Detekt 1.23.8 | Radon + pylint | Complejidad ciclomática, cognitive complexity, smells, naming |
| Detekt (reglas de estilo) | flake8 / black | Formato de código: indentación, imports, longitud de línea |
| JaCoCo 0.8.12 | pytest-cov | Cobertura de código: instrucciones, branches, líneas |

### 4.2 Métricas de Complejidad (Detekt)

Configuradas en `config/detekt/detekt.yml`. El pipeline falla si se superan estos umbrales (`maxIssues: 0`):

| Regla | Equivalente | Umbral | Justificación |
|-------|------------|--------|--------------|
| `CyclomaticComplexMethod` | Radon A/B | ≤ 10 | McCabe standard |
| `CognitiveComplexity` | Radon cognitive | ≤ 15 | Legibilidad humana |
| `LongMethod` | — | ≤ 60 líneas | Principio SRP |
| `LongParameterList` | — | ≤ 5 params | Acoplamiento |
| `NestedBlockDepth` | — | ≤ 4 niveles | Legibilidad |
| `MagicNumber` | — | Solo -1,0,1,2 | Mantenibilidad |
| `GlobalCoroutineUsage` | — | Prohibido | Evitar memory leaks |

### 4.3 Métricas Reales (último build)

Resultados de `./gradlew detekt` — **0 issues críticos en todos los módulos**:

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

- **CC máxima por función**: ≤ 10 (umbral Detekt — 0 violaciones detectadas)
- **Complejidad cognitiva global**: 190

### 4.4 Integración en el IDE

El archivo `.editorconfig` en la raíz del proyecto configura Android Studio para aplicar las mismas reglas de formato en tiempo real (indentación 4 espacios, longitud máxima 120 caracteres, charset UTF-8, LF line endings).

---

## 5. Plan de Verificación y Validación

### 5.1 Niveles de Testing

| Nivel | Herramienta | Equivalente Python | Qué cubre |
|-------|------------|-------------------|---------|
| Unitario | JUnit4 + MockK 1.13.13 | pytest + unittest.mock | ViewModels, UseCases, Repositorios en aislamiento |
| Integración liviana | JUnit4 + MockK | pytest | Colaboración entre capas (use case ↔ repositorio) |
| UI / E2E | Compose Testing (excluido MVP) | pytest-selenium | Flujos completos: registro, reserva, cancelación |
| Concurrencia | kotlinx-coroutines-test | pytest-asyncio | REQ-NF01: múltiples usuarios reservando simultáneamente |

### 5.2 Convención de Naming para Tests

Todos los tests siguen la convención **Given/When/Then (BDD)**:

```
given[Contexto]_when[Acción]_then[ResultadoEsperado]
```

Ejemplos:
- `givenValidCommerceData_whenRegister_thenReturnsSuccessWithUser` — REQ-F01
- `givenAvailablePack_whenUserReserves_thenReservationIsCreated` — REQ-F04
- `givenSingleAvailablePack_whenMultipleUsersReserveConcurrently_thenOnlyOneSucceeds` — REQ-NF01
- `givenActiveReservation_whenUserCancels_thenStatusIsCancelled` — REQ-F06

### 5.3 Métrica de Cobertura

Herramienta: **JaCoCo 0.8.12** (equivalente a `pytest-cov` en Python)

| Métrica | Mínimo | Objetivo | Resultado Real | Estado |
|---------|--------|----------|---------------|--------|
| Cobertura de instrucciones | 70% | 80% | **84.73%** | ✅ Supera |
| Cobertura de branches | 60% | 70% | **80.41%** | ✅ Supera |
| Complejidad ciclomática | ≤ 10/función | ≤ 7 | Max ≤ 10 (0 violaciones) | ✅ Cumple |

El pipeline de GitHub Actions falla automáticamente si la cobertura de instrucciones cae por debajo del 70%.

### 5.4 Inventario de Tests por Requerimiento

**Total: 121 tests automatizados — 121 pasan, 0 fallan**

| REQ | Archivo de Test | Tests | Nivel | Estado CI |
|-----|----------------|-------|-------|-----------|
| REQ-F01 | `LoginUserUseCaseTest.kt` | 8 | Unitario | ✅ |
| REQ-F01 | `RegisterUserUseCaseTest.kt` | 6 | Unitario | ✅ |
| REQ-F01 | `AuthRepositoryImplTest.kt` | 13 | Unitario | ✅ |
| REQ-F01 | `SessionManagerTest.kt` | 4 | Unitario | ✅ |
| REQ-F01 | `AuthViewModelTest.kt` | 12 | Unitario | ✅ |
| REQ-F01 | `ProfileViewModelTest.kt` | 3 | Unitario | ✅ |
| REQ-F02 | `PublishPackUseCaseTest.kt` | 5 | Unitario | ✅ |
| REQ-F02 | `PackRepositoryImplTest.kt` | 9 | Unitario | ✅ |
| REQ-F02 | `ReservationsViewModelTest.kt` | 7 | Unitario | ✅ |
| REQ-F03 | `GetNearbyPacksUseCaseTest.kt` | 5 | Unitario | ✅ |
| REQ-F03 | `PackRepositoryImplTest.kt` | 4 | Unitario | ✅ |
| REQ-F03 | `ProductsViewModelTest.kt` | 4 | Unitario | ✅ |
| REQ-F04 | `ReservePackUseCaseTest.kt` | 2 | Unitario | ✅ |
| REQ-F04 | `ReservationRepositoryImplTest.kt` | 5 | Unitario | ✅ |
| REQ-F04 | `ProductsViewModelTest.kt` | 4 | Unitario | ✅ |
| REQ-F05 | `MarkReservationWithdrawnUseCaseTest.kt` | 2 | Unitario | ✅ |
| REQ-F05 | `ReservationRepositoryImplTest.kt` | 3 | Unitario | ✅ |
| REQ-F05 | `ReservationsViewModelTest.kt` | 6 | Unitario | ✅ |
| REQ-F06 | `CancelReservationUseCaseTest.kt` | 2 | Unitario | ✅ |
| REQ-F06 | `ReservationRepositoryImplTest.kt` | 4 | Unitario | ✅ |
| REQ-F06 | `ReservationsViewModelTest.kt` | 3 | Unitario | ✅ |
| REQ-F07 | `FavoriteRepositoryImplTest.kt` | 4 | Unitario | ✅ |
| REQ-F07 | `ProductsViewModelTest.kt` | 2 | Unitario | ✅ |
| REQ-NF01 | `ReservationConcurrencyTest.kt` | 2 | Concurrencia | ✅ |
| REQ-NF01 | `ReservationRepositoryImplTest.kt` | 2 | Unitario | ✅ |
| REQ-NF01 | `ReservePackUseCaseTest.kt` | 1 | Unitario | ✅ |

### 5.5 Distribución por Módulo

| Módulo | Tests |
|--------|-------|
| `core:domain` | 32 |
| `core:data` | 46 |
| `feature:auth` | 15 |
| `feature:products` | 10 |
| `feature:reservations` | 18 |
| **Total** | **121** |

---

## 6. Pipeline de Integración Continua (CI/CD)

### 6.1 Configuración

- **Archivo**: `.github/workflows/ci.yml`
- **Triggers**: push a `main`, Pull Requests hacia `develop`
- **Entorno**: Ubuntu Latest, JDK 17 (Temurin), Gradle con cache

### 6.2 Jobs del Pipeline

| Job | Herramientas | Condición de falla | Artifact generado |
|-----|-----------|--------------------|------------------|
| 1. lint | Detekt | Cualquier issue crítico (`maxIssues: 0`) | detekt-report (30 días) |
| 2. test | JUnit4 + JaCoCo | Cobertura instrucciones < 70% | jacoco-coverage-report (30 días) |

El job `test` depende del job `lint` — si lint falla, test no se ejecuta.

### 6.3 Flujo del Pipeline

```
Developer hace push a rama feature/REQ-FXX
    │
    ▼
Developer abre Pull Request hacia develop
    │
    ▼
GitHub Actions ejecuta ci.yml
    │
    ├─ Job 1: lint (./gradlew detekt)
    │         → Complejidad ciclomática ≤ 10
    │         → maxIssues: 0
    │         → Si pasa ↓
    │
    └─ Job 2: test (./gradlew testDebugUnitTest jacocoTestReport jacocoTestCoverageVerification)
              → 121 tests pasan
              → Cobertura ≥ 70% instrucciones / ≥ 60% ramas
              → Si ambos pasan: PR habilitado para merge
```

---

## 7. Gestión de la Trazabilidad

### 7.1 Estrategia de Trazabilidad

La trazabilidad es el mecanismo que permite vincular cada requerimiento con su implementación, sus casos de test y su verificación en el pipeline. En Aprovecha! se implementa en 4 capas complementarias:

| Capa | Mecanismo | Ejemplo |
|------|-----------|---------|
| 1. Código fuente | Comentario `// @REQ-FXX` | `// @REQ-F04: El usuario debe poder reservar un pack` |
| 2. Anotación Kotlin | `@Requirement(id, description)` | `@Requirement("REQ-F04", "Usuario reserva pack")` |
| 3. KDoc | `@see REQ-FXX` en clases/funciones | `/** Caso de uso que implementa [REQ-F04]. */` |
| 4. Commits/Branches | Convención prefijada | `feat(REQ-F04): implement reserve pack use case` |

### 7.2 Anotación @Requirement

La anotación custom `@Requirement` (definida en `:core:common`) permite la búsqueda programática de implementaciones por requerimiento:

- **Archivo**: `core/common/src/main/kotlin/com/aprovecha/app/common/annotation/Requirement.kt`
- **Búsqueda**: `grep -r '@Requirement.*REQ-F04' src/`

### 7.3 Matriz de Trazabilidad (RTM)

La Matriz de Trazabilidad de Requerimientos completa (RTM) se encuentra en el archivo **[`RTM_Aprovecha.md`](./RTM_Aprovecha.md)** (v3.1) en la raíz del repositorio. Mapea cada requerimiento con: módulo de implementación, clase/UseCase, DAO (si aplica), archivos de test y estado de verificación en CI.

### 7.4 Cobertura de Trazabilidad — Estado Final

| REQ | UseCase / Clase | DAO afectado | Tests implementados | CI Gate | Estado |
|-----|----------------|-------------|---------------------|---------|--------|
| REQ-F01 | `RegisterUserUseCase`, `LoginUserUseCase`, `SessionManager` | `UserDao` | 46 | lint + test | ✅ |
| REQ-F02 | `PublishPackUseCase` | `PackDao` | 21 | lint + test | ✅ |
| REQ-F03 | `GetNearbyPacksUseCase` | `PackDao` | 13 | lint + test | ✅ |
| REQ-F04 | `ReservePackUseCase` | `PackDao`, `ReservationDao` | 12 | lint + test | ✅ |
| REQ-F05 | `MarkReservationWithdrawnUseCase` | `ReservationDao` | 11 | lint + test | ✅ |
| REQ-F06 | `CancelReservationUseCase` | `ReservationDao` | 9 | lint + test | ✅ |
| REQ-F07 | `FavoriteRepositoryImpl`, `ProductsViewModel` | `FavoriteDao` | 6 | lint + test | ✅ |
| REQ-NF01 | `PackDao.reservePackAtomically()` | `PackDao` (atómico) | 5 | lint + test | ✅ |
| REQ-NF02 | ProGuard/R8 release build | — | Build CI (APK size) | build job | ⚠️ Parcial |

---

## 8. Gestión de Riesgos de Calidad

| ID | Riesgo | Prob. | Impacto | Mitigación SQA | Estado |
|----|--------|-------|---------|----------------|--------|
| R01 | Pérdida de trazabilidad REQ-implementación | Media | Alto | Anotación `@Requirement` + RTM + convención de commits | ✅ Mitigado |
| R02 | Concurrencia: dos usuarios reservan el mismo pack (REQ-NF01) | Alta | Alto | `PackDao.reservePackAtomically()` + `ReservationConcurrencyTest.kt` | ✅ Mitigado |
| R03 | Degradación de cobertura de tests bajo el 70% | Media | Medio | JaCoCo gate en CI: falla el pipeline si < 70% | ✅ Mitigado (84.73%) |
| R04 | Complejidad ciclomática alta (código difícil de mantener) | Media | Medio | Detekt `CyclomaticComplexMethod` ≤ 10 como gate de CI | ✅ Mitigado (0 violaciones) |
| R05 | APK supera 50 MB (REQ-NF02) | Baja | Medio | `isMinifyEnabled` activo en release; ProGuard/R8 configurado | ⚠️ Pendiente verificación |
| R06 | Sesión null tras reinicio de la app (DEF-003) | Alta | Alto | `SessionManager` con DataStore implementado | ✅ Resuelto (DEF-003) |

---

## 9. Métricas de Calidad y Reporte

### 9.1 Métricas Finales

| Métrica | Umbral mínimo | Resultado Real | Herramienta | Estado |
|---------|--------------|----------------|-------------|--------|
| Cobertura de instrucciones | ≥ 70% | **84.73%** | JaCoCo | ✅ Supera |
| Cobertura de branches | ≥ 60% | **80.41%** | JaCoCo | ✅ Supera |
| Issues Detekt críticos | 0 | **0** | Detekt | ✅ Cumple |
| Complejidad ciclomática máx./función | ≤ 10 | **≤ 10** (0 violaciones) | Detekt | ✅ Cumple |
| Tests fallados en CI | 0 | **0** | JUnit4 | ✅ Cumple |
| Tests totales | — | **121** | JUnit4 | — |
| LOC total | — | **5,960** | Detekt | — |
| SLOC total | — | **4,159** | Detekt | — |
| CC total del proyecto | — | **382** | Detekt | — |
| Defectos detectados | ≥ 5 (Hito 4) | **6** | Manual | ✅ Cumple |
| Defectos resueltos | ≥ 1 | **1** (DEF-003) | Manual | ✅ Cumple |
| Densidad de defectos | — | **1.44 / KLOC** | Manual | — |
| Tamaño APK release | < 50 MB | Sin medir (MVP) | Gradle | ⚠️ Pendiente |

### 9.2 Artefactos de Calidad Generados

| Artefacto | Ubicación | Generado por |
|-----------|-----------|-------------|
| Reporte HTML Detekt | `build/reports/detekt/` | `./gradlew detekt` |
| Reporte HTML JaCoCo | `build/reports/jacoco/` | `./gradlew jacocoTestReport` |
| Resumen JaCoCo MD | `build/reports/jacoco/jacoco-summary.md` | `./gradlew jacocoTestReport` |
| Resumen Tests MD | `build/reports/tests/test-summary.md` | `./gradlew testDebugUnitTest` |
| RTM v3.1 | [`RTM_Aprovecha.md`](./RTM_Aprovecha.md) | Manual (sincronizado) |
| Plan de Pruebas | [`docs/testing/plan-de-pruebas.md`](./docs/testing/plan-de-pruebas.md) | Manual |
| Reporte de Defectos | [`docs/testing/reporte-defectos.md`](./docs/testing/reporte-defectos.md) | Manual |
| Plan SQA (este doc) | [`SQA_Plan_Aprovecha.md`](./SQA_Plan_Aprovecha.md) | Manual — IEEE 730 |

---

## 10. Glosario

| Término | Definición |
|---------|-----------|
| SQA | Software Quality Assurance — Aseguramiento de la Calidad del Software |
| RTM | Requirements Traceability Matrix — Matriz de Trazabilidad de Requerimientos |
| MVVM | Model-View-ViewModel — Patrón arquitectural usado en el proyecto |
| Clean Architecture | Arquitectura en capas: domain, data, presentation con dependencias hacia adentro |
| Detekt | Herramienta de análisis estático para Kotlin — equivalente a Radon+pylint en Python |
| JaCoCo | Java Code Coverage — herramienta de medición de cobertura de tests en Kotlin/JVM |
| GitFlow | Estrategia de branching con ramas main, develop, feature, hotfix y release |
| CI/CD | Continuous Integration/Continuous Deployment — automatización del build y deploy |
| MockK | Librería de mocking para Kotlin — equivalente a unittest.mock de Python |
| Hilt | Framework de inyección de dependencias para Android basado en Dagger |
| Room | ORM para Android que envuelve SQLite — capa de abstracción de base de datos local |
| DataStore | Solución de persistencia de Jetpack usada para sesión del usuario (SessionManager) |
| Pack | Unidad de alimentos no vendidos publicada por un comercio en la plataforma |
| REQ-FXX | Identificador de requerimiento funcional (F01-F07) usado para trazabilidad |
| REQ-NFXX | Identificador de requerimiento no funcional (NF01-NF02) usado para trazabilidad |
| LOC | Lines of Code — líneas totales de código (incluye comentarios y espacios) |
| SLOC | Source Lines of Code — líneas de código fuente (excluye comentarios y blancos) |
| CC | Cyclomatic Complexity — complejidad ciclomática, medida de complejidad del flujo de control |
| MI | Maintainability Index — índice de mantenibilidad del código |

---

## 11. Historial de Revisiones

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| 1.0 | Mayo 2026 | Gang of Four | Versión inicial del Plan SQA — IEEE 730 (Hito 3) |
| 2.0 | 16 Junio 2026 | Gang of Four | Actualización final Hito 5: agregado REQ-F07 (favoritos), ProfileScreen/SessionManager, estado "Implementado" en todos los REQs, métricas reales LOC/CC/cobertura, 121 tests documentados, transcripción a Markdown para trazabilidad en repo |

---

*Referencia: IEEE Std 730-2014 — Software Quality Assurance Processes*  
*Grupo: Gang of Four | IUA — Ingeniería de Software II — 2026*
