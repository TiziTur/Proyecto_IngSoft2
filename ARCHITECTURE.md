# Aprovecha! - Android App con MVVM y Compose

Aplicación Android moderna con arquitectura MVVM, Clean Architecture y multi-módulos.

## Estructura del Proyecto

```
Proyecto_IngSoft2/
├── app/                      # Aplicación principal (entrada)
├── core/
│   ├── common/               # Utilidades compartidas y extensiones
│   ├── data/                 # Implementaciones de repositorios, data sources
│   ├── domain/               # Lógica de negocio, use cases, interfaces
│   └── ui/                   # Componentes Compose compartidos, temas
├── feature/
│   └── products/             # Feature de productos (screens, viewmodels)
└── build-logic/              # (Opcional) Plugins de build personalizados
```

## Tecnologías Utilizadas

### Core
- **Kotlin 2.3.21+** - Lenguaje de programación
- **Jetpack Compose** - UI framework declarativo
- **Material 3** - Diseño Material Design 3

### Arquitectura
- **MVVM** - Model-View-ViewModel pattern
- **Clean Architecture** - Separación en capas (Domain, Data, Presentation)
- **Multi-módulos** - Separación de concerns y reutilización

### Networking
- **Retrofit 3** - Cliente HTTP
- **OkHttp** - Interceptor de red
- **Kotlinx Serialization** - Serialización JSON

### Database
- **Room** - Base de datos local

### Async
- **Coroutines** - Programación reactiva
- **Flow** - Streams reactivos

### Lifecycle
- **Lifecycle Runtime KTX** - Gestión del ciclo de vida
- **ViewModel** - Gestión de estado

## Convenciones de Código

### Estructura de Módulos
- `core:common` - Extensiones, utilidades, constantes
- `core:domain` - Interfaces, modelos, use cases
- `core:data` - Implementaciones, API, base de datos
- `core:ui` - Componentes Compose compartidos
- `feature:*` - Features independientes (screens, viewmodels)

### Nomenclatura
- ViewModels: `{Feature}ViewModel.kt`
- Screens/Composables: `{Feature}Screen.kt`
- Repositories: `{Entity}Repository.kt` / `{Entity}RepositoryImpl.kt`
- Use Cases: `{Verb}{Entity}UseCase.kt`

## Configuración Inicial

### Clonar el Proyecto
```bash
git clone https://github.com/TiziTur/Proyecto_IngSoft2.git
cd Proyecto_IngSoft2
```

### Abrir en Android Studio
1. Abre Android Studio
2. Selecciona `Open an Existing Project`
3. Navega a la carpeta del proyecto
4. Android Studio sincronizará automáticamente Gradle

### Ejecutar
- Conecta un dispositivo Android o inicia un emulador
- Presiona `Shift + F10` o usa `Run → Run 'app'`

## Git Workflow

### Crear una rama para tu feature
```bash
git checkout -b feature/nombre-feature
```

### Hacer cambios y commits
```bash
git add .
git commit -m "feat: descripción clara del cambio"
```

### Push y crear Pull Request
```bash
git push origin feature/nombre-feature
```
Luego en GitHub, crea un Pull Request.

### Merge a main
- Revisa el código
- Aprueba y merges el PR
- Elimina la rama remota

## Testing

### Unit Tests
```bash
./gradlew test
```

### UI Tests
```bash
./gradlew connectedAndroidTest
```

## Build & Release

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

## Contribuciones

1. Crea una rama con tu feature: `git checkout -b feature/AmazingFeature`
2. Commit tus cambios: `git commit -m 'Add AmazingFeature'`
3. Push a la rama: `git push origin feature/AmazingFeature`
4. Abre un Pull Request

## Licencia

Este proyecto está bajo la licencia MIT.

---

**Última actualización**: 18/05/2026
