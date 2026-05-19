# 🚀 Guía de Inicio Rápido - Proyecto Aprovecha!

## ¿Qué se configuró?

Tu proyecto Android ahora cuenta con:

✅ **Arquitectura MVVM** con Clean Architecture  
✅ **Multi-módulos** para mejor organización  
✅ **Kotlin** 100%  
✅ **Jetpack Compose** para UI  
✅ **Material 3** con diseño moderno  
✅ **Git** configurado y sincronizado con GitHub  

---

## 📁 Estructura de Módulos Explicada

### `core:domain`
**¿Qué va aquí?**
- Modelos de negocio (Data classes)
- Interfaces de repositorios
- Use cases (lógica de negocio)

**Ejemplo**: `Product.kt`, `ProductRepository.kt`, `GetProductsUseCase.kt`

```kotlin
// core/domain/src/main/kotlin/.../domain/Product.kt
data class Product(
    val id: Int,
    val name: String,
    val price: Double
)
```

### `core:data`
**¿Qué va aquí?**
- Implementaciones de repositorios
- Data sources (API, database)
- Modelos de API/DB

**Ejemplo**: `ProductRepositoryImpl.kt`

```kotlin
// core/data/src/main/kotlin/.../data/ProductRepositoryImpl.kt
class ProductRepositoryImpl : ProductRepository {
    override suspend fun getProducts(): List<Product> {
        // Llamada al API o BD
        return listOf(...)
    }
}
```

### `core:ui`
**¿Qué va aquí?**
- Componentes Compose reutilizables
- Temas y estilos
- Estados UI compartidos

### `core:common`
**¿Qué va aquí?**
- Extensiones de Kotlin
- Utilidades
- Constantes

### `feature:*` (ej: `feature:products`)
**¿Qué va aquí?**
- Screens (Composables)
- ViewModels
- Navegación específica de la feature

```kotlin
// feature/products/src/main/kotlin/.../products/ProductsViewModel.kt
class ProductsViewModel(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {
    // Lógica de presentación
}

// feature/products/src/main/kotlin/.../products/ProductsScreen.kt
@Composable
fun ProductsScreen(viewModel: ProductsViewModel) {
    // UI con Compose
}
```

---

## 🔧 Primeros Pasos en Android Studio

### 1. Abre el Proyecto
- Abre Android Studio
- **File → Open**
- Navega a: `C:\Users\tizia\OneDrive\Documentos\GitHub\Proyecto_IngSoft2`
- Android Studio sincronizará automáticamente

### 2. Espera a que se Sincronice
```
Gradle: 0% > :Downloading...
```
(Esto puede tomar 2-5 minutos en la primera sincronización)

### 3. Verifica la Estructura
En la vista de Project, deberías ver:
```
Proyecto_IngSoft2
├── app (módulo aplicación)
├── core/common
├── core/data
├── core/domain
├── core/ui
├── feature/products
```

### 4. Ejecuta la App
- Conecta un dispositivo Android o inicia un emulador
- **Run → Run 'app'** o presiona **Shift + F10**

---

## 🔄 Git Workflow Diario

### Crear una nueva rama para trabajar
```bash
cd "C:\Users\tizia\OneDrive\Documentos\GitHub\Proyecto_IngSoft2"

# Crear y cambiar a rama
git checkout -b feature/nombre-de-tu-feature

# Ejemplo
git checkout -b feature/login-screen
```

### Hacer cambios y commit
```bash
# Ver cambios
git status

# Agregar cambios
git add .

# Hacer commit con mensaje descriptivo
git commit -m "feat: Agregar pantalla de login

- Crear LoginScreen composable
- Agregar LoginViewModel
- Integrar validación de email"
```

### Push y Pull Request
```bash
# Empujar rama a GitHub
git push origin feature/nombre-de-tu-feature

# Luego en GitHub:
# 1. Ve a https://github.com/TiziTur/Proyecto_IngSoft2
# 2. Verás un botón "Compare & pull request"
# 3. Describe los cambios y crea el PR
```

---

## 📝 Ejemplo: Agregar una Nueva Pantalla

### Paso 1: Crear el Domain Model
```kotlin
// core/domain/src/main/kotlin/.../domain/User.kt
data class User(
    val id: Int,
    val name: String,
    val email: String
)
```

### Paso 2: Crear la Interface en Domain
```kotlin
// core/domain/src/main/kotlin/.../domain/repository/UserRepository.kt
interface UserRepository {
    suspend fun getUser(id: Int): User?
}
```

### Paso 3: Implementar en Data
```kotlin
// core/data/src/main/kotlin/.../data/repository/UserRepositoryImpl.kt
class UserRepositoryImpl : UserRepository {
    override suspend fun getUser(id: Int): User? {
        // Aquí iría la llamada al API
        return User(1, "Juan", "juan@example.com")
    }
}
```

### Paso 4: Crear Use Case
```kotlin
// core/domain/src/main/kotlin/.../domain/usecase/GetUserUseCase.kt
class GetUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: Int): User? = repository.getUser(id)
}
```

### Paso 5: Crear ViewModel
```kotlin
// feature/users/src/main/kotlin/.../users/UserViewModel.kt
class UserViewModel(
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    fun loadUser(id: Int) {
        viewModelScope.launch {
            _user.value = getUserUseCase(id)
        }
    }
}
```

### Paso 6: Crear Screen
```kotlin
// feature/users/src/main/kotlin/.../users/UserScreen.kt
@Composable
fun UserScreen(viewModel: UserViewModel) {
    val user by viewModel.user.collectAsState()
    
    user?.let {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nombre: ${it.name}")
            Text(text = "Email: ${it.email}")
        }
    }
}
```

---

## 🧪 Testing

### Unit Tests (para lógica sin Android)
```bash
# En core:domain
./gradlew :core:domain:test
```

### UI Tests (para Composables)
```bash
# Conecta un dispositivo/emulador
./gradlew :app:connectedAndroidTest
```

---

## 🔐 Buenas Prácticas

1. **Siempre crea ramas** para nuevas features
2. **Commits pequeños y descriptivos** - Facilita las revisiones
3. **No mergees a main** sin pull request review
4. **Pull regularmente** antes de empezar a trabajar:
   ```bash
   git pull origin main
   ```
5. **Mantén las ramas limpias** - Elimina ramas merged:
   ```bash
   git branch -d nombre-rama
   ```

---

## 📚 Referencias Útiles

- [Documentación Completa](./ARCHITECTURE.md)
- [Google Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

## ❓ Preguntas Frecuentes

**¿Dónde agrego mis dependencias?**
→ En el `build.gradle.kts` del módulo correspondiente

**¿Cómo hago para que funcione con Hilt (inyección de dependencias)?**
→ Agrega Hilt en `app/build.gradle.kts` y en los módulos que necesites

**¿Qué pasa si tengo conflictos en Git?**
→ Usa `git status` para verlos, resuélvelos manualmente y luego `git add .` y `git commit`

**¿Cómo elimino una rama local?**
```bash
git branch -d nombre-rama        # Eliminar localmente
git push origin -d nombre-rama   # Eliminar en remoto
```

---

**¡Listo para empezar!** 🎉

Cualquier duda, revisa `ARCHITECTURE.md` o consulta la documentación oficial de Android.
