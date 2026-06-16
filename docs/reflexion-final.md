# Reflexión Final — Aprovecha!
## Ingeniería de Software II — IUA | Grupo: Gang of Four
### Hito 5 | Junio 2026

---

## ¿Qué aprendimos?

**Arquitectura como inversión, no como overhead.**
Al principio el setup multi-módulo (8 módulos Gradle) parecía excesivo para el tamaño del proyecto. A medida que avanzamos, la separación estricta entre `core:domain`, `core:data` y las features nos permitió agregar funcionalidades (favoritos, sesión DataStore, reservas pendientes) sin tocar código ya estabilizado. La arquitectura MVVM + Clean Architecture no es solo teoría: nos salvó cuando tuvimos que corregir DEF-003 (sesión siempre null) sin romper ningún test existente.

**Los tests documentan la intención, no solo el código.**
Escribir tests con la convención `given[Contexto]_when[Acción]_then[Resultado]` nos forzó a pensar en contratos antes de implementar. El `ReservationConcurrencyTest` (TC-16) fue el ejemplo más claro: al intentar escribir el test de 10 usuarios concurrentes, descubrimos que la transacción Room `@Transaction` necesitaba ser atómica desde el DAO, no desde el repositorio. El test guió el diseño.

**La trazabilidad no es burocracia.**
Mantener las anotaciones `@Requirement("REQ-FXX", ...)` en el código y la RTM sincronizada nos permitió detectar en la revisión final que REQ-F07 (favoritos) estaba implementado en el código pero completamente ausente de los documentos. Esa desincronización habría sido un punto débil en la defensa.

**Gestión de defectos como herramienta de aprendizaje.**
El reporte de defectos no fue un trámite: DEF-001 (hash inseguro con `hashCode()`) y DEF-003 (sesión siempre null) nos hicieron investigar alternativas reales (`BCrypt`, `DataStore`). DEF-003 fue resuelto durante el desarrollo; los demás quedan documentados como deuda técnica priorizada.

**ISO 25010 como lente de diseño.**
Usar las características de calidad de ISO 25010 como guía nos hizo considerar dimensiones que normalmente ignoramos: la **fiabilidad** nos llevó a diseñar el test de concurrencia; la **mantenibilidad** justificó Detekt con límite de complejidad ciclomática ≤ 10; la **usabilidad** guió el análisis heurístico de Nielsen.

---

## ¿Qué haríamos distinto?

**Empezar la RTM desde el primer commit.**
La RTM empezó a construirse en el Hito 3 y requirió trabajo retroactivo considerable. Si hubiéramos establecido los IDs de requerimientos (REQ-F01...) desde el scaffold inicial, cada commit habría podido referenciarlos directamente.

**Implementar sesión persistente desde el principio.**
DEF-003 (getCurrentUser siempre null) fue un stub que quedó sin implementar durante demasiado tiempo. Esto hizo que varios ViewModels tuvieran que asumir un userId hardcodeado, generando deuda técnica que luego requirió refactorizaciones en cadena (AuthViewModel + ReservationsViewModel + ProfileViewModel).

**CI desde el Hito 2.**
Configuramos GitHub Actions en el Hito 3. Retrospectivamente, tenerlo desde el Hito 2 habría detectado antes las regresiones de Detekt que aparecieron cuando la complejidad ciclomática superó el umbral al agregar lógica de filtros en `ReservationsViewModel`.

**Tests de UI (Compose) además de tests unitarios.**
La cobertura de 84.73% cubre la lógica de negocio, pero las pantallas Compose no están cubiertas (están excluidas explícitamente en JaCoCo). En un proyecto real añadiríamos tests de UI con `ComposeTestRule` para los flujos críticos (reserva, cancelación, login).

---

## Temas de la materia aplicados

| Tema | Cómo se aplicó |
|------|----------------|
| **ISO/IEC 25010** | 5 características de calidad como guía de decisiones de diseño (funcionalidad, fiabilidad, mantenibilidad, seguridad, usabilidad) |
| **IEEE 730 (Plan SQA)** | Estructura del plan de calidad: roles, revisiones, métricas, herramientas |
| **IEEE 829 (Plan de pruebas)** | Estructura del documento de plan de pruebas, casos documentados, criterios de entrada/salida |
| **FMEA** | Identificación de modos de falla (concurrencia de reservas, sesión null, hash inseguro) |
| **RTM** | Trazabilidad completa REQ → módulo → función → test → CI |
| **Análisis estático** | Detekt con complejidad ciclomática ≤ 10 y maxIssues = 0 |
| **Cobertura de código** | JaCoCo con umbral 70% instrucciones / 60% ramas en CI |
| **Programación defensiva** | `Result<T>` como tipo de retorno en toda la capa de datos, evitando excepciones no controladas |
| **Heurísticas de Nielsen** | Análisis de las 10 heurísticas sobre 3 pantallas clave |
| **Gestión de defectos** | 6 defectos reportados con severidad, impacto y corrección sugerida; 1 resuelto con test de regresión |
| **MVVM + Clean Architecture** | Separación de responsabilidades: dominio sin dependencias de framework, data independiente de UI |

---

*Grupo: Gang of Four | IUA — Ingeniería de Software II — 2026*
