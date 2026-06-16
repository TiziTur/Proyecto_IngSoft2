# Aprovecha! — Arquitectura del Proyecto

Aplicación Android con arquitectura MVVM + Clean Architecture, multi-módulos Gradle.

## Estructura de Módulos

```
Proyecto_IngSoft2/
├── app/                        # Punto de entrada: MainActivity, NavGraph, AprovechaApplication
├── core/
│   ├── common/                 # Utilidades compartidas: Result<T>, @Requirement
│   ├── data/                   # Room (DAOs, entidades), implementaciones de repositorios, Hilt modules
│   │                           # SessionManager (DataStore), FavoriteRepositoryImpl
│   ├── domain/                 # Modelos de dominio, interfaces de repositorios, use cases
│   │                           # FavoriteRepository (interfaz)
│   └── ui/                     # Tema Compose, colores, tipografía compartida
├── feature/
│   ├── auth/                   # Login, registro y perfil (LoginScreen, RegisterScreen, ProfileScreen)
│   │                           # AuthViewModel, ProfileViewModel
│   ├── products/               # Lista de packs, detalle y favoritos (HomeConsumerScreen, PackDetailScreen)
│   │                           # FavoritesScreen, ProductsViewModel
│   └── reservations/           # Mis reservas, panel del comercio y reservas pendientes
│                               # MyReservationsScreen, HomeCommerceScreen, PublishPackScreen
│                               # PendingReservationsScreen, ReservationsViewModel
└── config/
    └── detekt/                 # Configuración Detekt (detekt.yml)
```

## Stack Tecnológico

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Lenguaje | Kotlin | 2.0.21 |
| UI | Jetpack Compose + Material 3 | BOM 2024.09.00 |
| Arquitectura | MVVM + Clean Architecture | — |
| DI | Hilt (sobre Dagger) | 2.51.1 |
| Base de datos local | Room | 2.6.1 |
| Async | Coroutines + Flow | 1.9.0 |
| Networking (preparado) | Retrofit + OkHttp | 2.9.0 / 4.11.0 |
| Serialización | Kotlinx Serialization | 1.6.0 |
| Análisis estático | Detekt | 1.23.8 |
| Tests unitarios | JUnit4 + MockK | 4.13.2 / 1.13.13 |
| Cobertura | JaCoCo | 0.8.12 |
| Build | AGP + KSP | 8.7.3 / 2.0.21-1.0.28 |

## Capas de la Arquitectura

```
┌─────────────────────────────┐
│   feature:auth              │  Presentation (Compose Screens + HiltViewModels)
│   feature:products          │
│   feature:reservations      │
└──────────────┬──────────────┘
               │ usa
┌──────────────▼──────────────┐
│   core:domain               │  Domain (UseCases, modelos, interfaces Repository)
└──────────────┬──────────────┘
               │ implementa
┌──────────────▼──────────────┐
│   core:data                 │  Data (Room DAOs, entidades, RepositoryImpl)
└─────────────────────────────┘
```

## Convenciones de Nombres

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| ViewModel | `{Feature}ViewModel.kt` | `ProductsViewModel.kt` |
| Screen | `{Feature}Screen.kt` | `HomeConsumerScreen.kt` |
| Repository interface | `{Entity}Repository.kt` | `PackRepository.kt` |
| Repository impl | `{Entity}RepositoryImpl.kt` | `PackRepositoryImpl.kt` |
| Use Case | `{Verbo}{Entidad}UseCase.kt` | `ReservePackUseCase.kt` |
| Entity (Room) | `{Entidad}Entity.kt` | `PackEntity.kt` |
| DAO | `{Entidad}Dao.kt` | `PackDao.kt` |

## Trazabilidad de Requerimientos

Cada use case, repositorio y pantalla lleva anotaciones `// @REQ-FXX` y la anotación
`@Requirement("REQ-FXX", "descripción")` definida en `core:common`. Esto permite
rastrear cada línea de código hasta el requerimiento funcional que la origina.

Ver [RTM_Aprovecha.md](./RTM_Aprovecha.md) para la matriz completa.

## Flujo de Navegación

```
LOGIN ──(éxito CONSUMER)──► HOME_CONSUMER ──► PACK_DETAIL/{id} ──► MY_RESERVATIONS
      │                           │                                        │
      │                           └──► FAVORITES                    (filtro TODOS/
      │                                                           RETIRADOS/CANCELADOS)
      ──(éxito COMMERCE)──► HOME_COMMERCE ──► PUBLISH_PACK
      │                           └──► PENDING_RESERVATIONS
      ──(sin cuenta)──────► REGISTER
      └── (cualquier rol) ──► PROFILE (cierre de sesión)
```

## Comandos útiles

```bash
# Compilar debug
./gradlew assembleDebug

# Ejecutar todos los tests unitarios
./gradlew testDebugUnitTest

# Análisis estático Detekt
./gradlew detekt

# Reporte de cobertura JaCoCo
./gradlew jacocoTestReport

# Verificar cobertura mínima 70%
./gradlew jacocoTestCoverageVerification
```

---
**Última actualización**: 16/06/2026
