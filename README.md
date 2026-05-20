# 🌱 Aprovecha!

**Aprovecha!** es una aplicación Android para reducir el desperdicio alimentario conectando comercios locales con consumidores. Los comercios publican packs de alimentos a precio de descuento al final del día, y los consumidores los reservan para retirar.

> Proyecto: Ingeniería de Software II — IUA  
> Grupo: Gang of Four  
> Referencia de calidad: IEEE 730 (Plan SQA)

---

## Capturas de Pantalla

| Login | Registro | Home Consumidor | Detalle Pack |
|-------|----------|-----------------|--------------|
| Login con email/contraseña | Registro con selección de rol | Lista de packs cercanos | Info del pack + reservar |

| Mis Reservas | Home Comercio | Publicar Pack |
|--------------|---------------|---------------|
| Activas e historial | Panel con stats | Formulario con validación |

---

## Funcionalidades

- **REQ-F01** — Registro y login de usuarios con rol CONSUMER o COMMERCE
- **REQ-F02** — Los comercios publican packs con nombre, precio original, precio con descuento y cantidad
- **REQ-F03** — Los consumidores ven packs disponibles cercanos (radio configurable, default 5 km)
- **REQ-F04** — Reserva de pack con exclusividad garantizada (transacción atómica Room)
- **REQ-F05** — El comercio marca la reserva como retirada (RESERVED → WITHDRAWN)
- **REQ-F06** — El consumidor puede cancelar una reserva antes del retiro (RESERVED → CANCELLED)

---

## Stack Tecnológico

| Tecnología | Versión | Uso |
|-----------|---------|-----|
| Kotlin | 2.0.21 | Lenguaje |
| Jetpack Compose | BOM 2024.09.00 | UI declarativa |
| Material Design 3 | — | Sistema de diseño |
| Hilt | 2.51.1 | Inyección de dependencias |
| Room | 2.6.1 | Base de datos local SQLite |
| Coroutines + Flow | 1.9.0 | Programación asíncrona |
| Retrofit + OkHttp | 2.9.0 / 4.11.0 | Networking (preparado para backend) |
| Detekt | 1.23.8 | Análisis estático (CI) |
| JUnit4 + MockK | 4.13.2 / 1.13.13 | Tests unitarios |
| JaCoCo | 0.8.12 | Cobertura de código (mínimo 70%) |
| AGP | 8.7.3 | Android Gradle Plugin |

---

## Arquitectura

El proyecto usa **MVVM + Clean Architecture** dividida en 8 módulos Gradle:

```
app/                    → Punto de entrada (MainActivity, NavGraph, AprovechaApplication)
core/common/            → Result<T>, @Requirement annotation
core/domain/            → Modelos, interfaces Repository, UseCases
core/data/              → Room (DAOs, entidades), RepositoryImpl, Hilt modules
core/ui/                → Tema Compose, colores, tipografía
feature/auth/           → Login y Registro
feature/products/       → Lista de packs y detalle (consumidor)
feature/reservations/   → Mis reservas y panel del comercio
```

Ver [ARCHITECTURE.md](./ARCHITECTURE.md) para más detalle.

---

## Requisitos

- Android Studio Hedgehog o superior
- JDK 17
- Android SDK 35
- Dispositivo o emulador con Android 7.0+ (API 24+)

---

## Configuración Inicial

```bash
git clone https://github.com/TiziTur/Proyecto_IngSoft2.git
cd Proyecto_IngSoft2
```

1. Abrir Android Studio → **File → Open** → seleccionar la carpeta del proyecto
2. Esperar la sincronización de Gradle (primera vez: ~3-5 minutos)
3. Ejecutar con **Shift + F10** o **Run → Run 'app'**

---

## Tests

El proyecto tiene **90 tests unitarios** distribuidos en 5 módulos:

```bash
# Ejecutar todos los tests
./gradlew testDebugUnitTest

# Reporte de cobertura HTML (abre en build/reports/jacoco/)
./gradlew jacocoTestReport

# Verificar umbral mínimo 70%
./gradlew jacocoTestCoverageVerification
```

| Módulo | Archivo de Test | Tests |
|--------|-----------------|-------|
| `core:domain` | `LoginUserUseCaseTest` | 8 |
| `core:domain` | `RegisterUserUseCaseTest` | 6 |
| `core:domain` | `GetNearbyPacksUseCaseTest` | 5 |
| `core:domain` | `PublishPackUseCaseTest` | 5 |
| `core:domain` | `ReservePackUseCaseTest` | 2 |
| `core:domain` | `MarkReservationWithdrawnUseCaseTest` | 2 |
| `core:domain` | `CancelReservationUseCaseTest` | 2 |
| `core:domain` | `ReservationConcurrencyTest` | 2 |
| `core:data` | `AuthRepositoryImplTest` | 8 |
| `core:data` | `PackRepositoryImplTest` | 10 |
| `core:data` | `ReservationRepositoryImplTest` | 13 |
| `feature:auth` | `AuthViewModelTest` | 11 |
| `feature:products` | `ProductsViewModelTest` | 7 |
| `feature:reservations` | `ReservationsViewModelTest` | 9 |

---

## CI/CD

El pipeline de GitHub Actions corre en cada push a `main` y en Pull Requests a `develop`:

```
Push / PR
    │
    ▼
┌─────────────────────────────────────────┐
│  Job 1: lint                            │
│  ./gradlew detekt                       │
│  → Complejidad ciclomática ≤ 10        │
│  → maxIssues: 0 (falla con cualquier    │
│    issue crítico)                       │
└──────────────────┬──────────────────────┘
                   │ (si pasa)
                   ▼
┌─────────────────────────────────────────┐
│  Job 2: test                            │
│  ./gradlew testDebugUnitTest            │
│  ./gradlew jacocoTestReport             │
│  ./gradlew jacocoTestCoverageVerification│
│  → Cobertura mínima 70% (IEEE 730 §5.3)│
└─────────────────────────────────────────┘
```

Herramientas: **Detekt 1.23.8**, **JUnit4**, **MockK**, **JaCoCo 0.8.12**  
Artefactos generados: reporte Detekt HTML, reporte JaCoCo HTML/XML

---

## Documentación del Proyecto

| Documento | Descripción |
|-----------|-------------|
| [ARCHITECTURE.md](./ARCHITECTURE.md) | Arquitectura, módulos, stack y comandos |
| [RTM_Aprovecha.md](./RTM_Aprovecha.md) | Matriz de trazabilidad requerimientos ↔ código ↔ tests |
| [SQA_Plan_Aprovecha.docx](./SQA_Plan_Aprovecha.docx) | Plan SQA completo (IEEE 730) |
| [QUICK_START.md](./QUICK_START.md) | Guía rápida para nuevos desarrolladores |
| [REFERENCES.md](./REFERENCES.md) | Referencias y recursos útiles |

---

## Trazabilidad

Cada clase, use case y repositorio lleva anotaciones `// @REQ-FXX` y `@Requirement("REQ-FXX", "descripción")` para vincular el código con los requerimientos. Ver [RTM_Aprovecha.md](./RTM_Aprovecha.md) para la matriz completa.

---

## Licencia

Este proyecto es de uso académico — IUA, Ingeniería de Software II, 2026.
