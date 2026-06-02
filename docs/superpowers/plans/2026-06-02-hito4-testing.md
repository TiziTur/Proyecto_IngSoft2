# Hito 4 - Plan de Pruebas Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Crear los tres documentos requeridos por el Hito 4: plan de pruebas, tabla de 15+ casos documentados (caja negra + caja blanca), y reporte de 5+ defectos detectados.

**Architecture:** Los tests automatizados ya existen (79+ tests en JUnit4 + MockK con JaCoCo al 70%+). El trabajo pendiente es crear la documentación formal: plan de pruebas, casos de prueba tabulados y reporte de defectos, todo en `docs/testing/`.

**Tech Stack:** Kotlin / JUnit4 / MockK / JaCoCo / Android Gradle (KTS) / Markdown para documentos

---

## File Structure

- Create: `docs/testing/plan-de-pruebas.md` — Documento de plan de pruebas (alcance, estrategia, criterios)
- Create: `docs/testing/casos-de-prueba.md` — Tabla con 15+ casos documentados (caja negra + caja blanca)
- Create: `docs/testing/reporte-defectos.md` — Reporte formal de 5+ defectos detectados

---

### Task 1: Crear plan de pruebas (plan-de-pruebas.md)

**Files:**
- Create: `docs/testing/plan-de-pruebas.md`

- [ ] **Step 1: Crear el documento plan-de-pruebas.md**

Crear `docs/testing/plan-de-pruebas.md` con el contenido completo del plan de pruebas IEEE 829-style:
- Alcance (módulos bajo prueba: core/domain, core/data, feature/products, feature/reservations)
- Estrategia (niveles: unitaria, integración; técnicas: caja negra, caja blanca; herramientas: JUnit4, MockK, JaCoCo)
- Criterios de entrada/salida
- Criterios de cobertura (>60% instrucciones, >60% ramas)
- Requisitos cubiertos (REQ-F01 a REQ-F06, REQ-NF01)

El documento debe referenciar los archivos de test existentes.

- [ ] **Step 2: Commit**

```bash
git add docs/testing/plan-de-pruebas.md
git commit -m "docs: agregar plan de pruebas hito 4 (alcance, estrategia, criterios)"
```

---

### Task 2: Crear tabla de casos de prueba documentados (casos-de-prueba.md)

**Files:**
- Create: `docs/testing/casos-de-prueba.md`

- [ ] **Step 1: Crear el documento casos-de-prueba.md**

Crear `docs/testing/casos-de-prueba.md` con tabla de 16+ casos (8 caja negra + 8 caja blanca):

Columnas: ID | Técnica | Módulo | Descripción | Precondiciones | Entrada | Resultado Esperado | Resultado Obtenido | Estado | REQ

**Caja Negra (8 casos):**
- TC-01: Login exitoso Consumer
- TC-02: Login email vacío → Error sin llamar al repo
- TC-03: Registro email duplicado → Error
- TC-04: Publicar pack nombre vacío → Error sin llamar repo
- TC-05: Publicar pack descuento >= original → Error
- TC-06: Ver packs cercanos retorna lista
- TC-07: Reservar pack disponible → RESERVED
- TC-08: Cancelar reserva RESERVED → CANCELLED

**Caja Blanca (8 casos):**
- TC-09: Login verifica rama password incorrecto (hash)
- TC-10: Registro verifica consulta DAO getUserByEmail antes de insertar
- TC-11: createReservation usa reservePackAtomically() — rama rows==0
- TC-12: getAvailablePacksNearby() ignora coords MVP, retorna todos
- TC-13: markAsWithdrawn valida status == RESERVED antes de continuar
- TC-14: cancelReservation valida status != WITHDRAWN
- TC-15: publishPack fuerza status = AVAILABLE independiente del input
- TC-16: Concurrencia: 10 goroutines → solo 1 Success (AtomicInteger)

- [ ] **Step 2: Commit**

```bash
git add docs/testing/casos-de-prueba.md
git commit -m "docs: agregar 16 casos de prueba documentados (8 caja negra + 8 caja blanca)"
```

---

### Task 3: Crear reporte de defectos (reporte-defectos.md)

**Files:**
- Create: `docs/testing/reporte-defectos.md`

- [ ] **Step 1: Crear el documento reporte-defectos.md**

Crear `docs/testing/reporte-defectos.md` con tabla de 6 defectos detectados mediante revisión de código y análisis de cobertura:

| ID | Severidad | Módulo | Descripción | Evidencia | Estado |
|----|-----------|--------|-------------|-----------|--------|

**Defectos a documentar:**

- DEF-001 (Alta): `AuthRepositoryImpl.register()` usa `hashCode()` de Java como hash de contraseña — inseguro e inconsistente entre JVMs / plataformas
  - Evidencia: `core/data/.../AuthRepositoryImpl.kt:32` — `password.hashCode().toString()`

- DEF-002 (Media): `PackRepositoryImpl.getAvailablePacksNearby()` ignora completamente los parámetros de latitud, longitud y radio — retorna todos los packs sin filtro geográfico
  - Evidencia: `core/data/.../PackRepositoryImpl.kt:34-35` — llama directamente `packDao.getAvailablePacks()`

- DEF-003 (Alta): `AuthRepositoryImpl.getCurrentUser()` siempre retorna `null` — el sistema nunca puede saber qué usuario está logueado, rompe flujos que dependan de sesión
  - Evidencia: `core/data/.../AuthRepositoryImpl.kt:55`

- DEF-004 (Media): `ReservationRepositoryImpl.cancelReservation()` permite cancelar reservas que ya están en estado CANCELLED (solo bloquea WITHDRAWN) — debería permitir cancelar únicamente desde estado RESERVED
  - Evidencia: `core/data/.../ReservationRepositoryImpl.kt:71` — `if (entity.status == ReservationStatus.WITHDRAWN.name)`

- DEF-005 (Baja): `RegisterUserUseCase` no valida el formato del email — acepta strings como `"noesunEmail"` o `"@"` como emails válidos
  - Evidencia: `core/domain/.../RegisterUserUseCase.kt:39` — solo valida `isBlank()`

- DEF-006 (Media): `PublishPackUseCase` no valida que los precios sean positivos — acepta `originalPrice = -100.0` y `discountPrice = -200.0`, lo que haría que el descuento sea incorrecto
  - Evidencia: `core/domain/.../PublishPackUseCase.kt:25-32` — solo valida `discountPrice >= originalPrice`

- [ ] **Step 2: Commit**

```bash
git add docs/testing/reporte-defectos.md
git commit -m "docs: agregar reporte de 6 defectos detectados hito 4"
```

---

### Task 4: Verificar que los tests corren y la cobertura es ≥60%

**Files:**
- No new files (verificación)

- [ ] **Step 1: Ejecutar tests en módulo core/domain**

```bash
cd C:\Users\tizia\OneDrive\Documentos\GitHub\Proyecto_IngSoft2
./gradlew :core:domain:test --tests "*" 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL, todos los tests pasan

- [ ] **Step 2: Ejecutar tests en módulo core/data**

```bash
./gradlew :core:data:testDebugUnitTest 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Ejecutar tests de features**

```bash
./gradlew :feature:products:testDebugUnitTest :feature:reservations:testDebugUnitTest 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Generar reporte JaCoCo y verificar cobertura**

```bash
./gradlew :core:domain:jacocoTestCoverageVerification :core:data:jacocoTestCoverageVerification 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL (≥70% instrucciones, ≥60% ramas)

- [ ] **Step 5: Commit final**

```bash
git add -A
git commit -m "docs: completar documentacion hito 4 - plan, casos y reporte de defectos"
```
