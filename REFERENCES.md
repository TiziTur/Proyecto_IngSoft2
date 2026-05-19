# 📖 Referencias y Recursos - Proyecto Aprovecha!

## 🎯 Documentación Oficial de Google

### Android Architecture Components
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [MVVM Pattern](https://developer.android.com/jetpack/guide/architecture)
- [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [LiveData vs StateFlow](https://developer.android.com/topic/libraries/data-binding/databinding-adapters)

### Jetpack Compose
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Compose API Reference](https://developer.android.com/reference/kotlin/androidx/compose/package-summary)
- [Compose Layouts](https://developer.android.com/jetpack/compose/layouts/basics)
- [Compose State Management](https://developer.android.com/jetpack/compose/state)

### Coroutines y Flow
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Flow - Asynchronous Flows](https://kotlinlang.org/docs/flow.html)
- [StateFlow vs Flow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)

---

## 🛠️ Bibliotecas y Dependencias

### Red (Networking)
- **Retrofit 3** - HTTP Client
  - [Documentación](https://square.github.io/retrofit/)
  - [Ejemplo básico](#retrofit-ejemplo)
  
- **OkHttp** - Interceptores y configuración HTTP
  - [Documentación](https://square.github.io/okhttp/)
  
- **Kotlinx Serialization** - Serialización JSON
  - [Documentación](https://github.com/Kotlin/kotlinx.serialization)

### Base de Datos
- **Room** - ORM para SQLite
  - [Documentación](https://developer.android.com/jetpack/androidx/releases/room)
  - [Guía de Room](https://developer.android.com/training/data-storage/room)

### Inyección de Dependencias
- **Hilt** - (Recomendado agregar luego)
  - [Documentación](https://dagger.dev/hilt/)

### UI y Diseño
- **Material 3**
  - [Material Design 3](https://m3.material.io/)
  - [Color System](https://m3.material.io/styles/color/overview)

---

## 📚 Patrones y Best Practices

### Clean Architecture
```
Presentation Layer (UI)
        ↓
Domain Layer (Use Cases, Models)
        ↓
Data Layer (Repositories, Data Sources)
```

### MVVM Pattern
- **Model** → Data classes, Repositories
- **View** → Composables
- **ViewModel** → Business Logic, State Management

### Repository Pattern
```kotlin
// La única forma de acceder a datos
interface ProductRepository {
    suspend fun getProducts(): List<Product>
}
```

---

## 🔍 Ejemplos de Código

### Retrofit Ejemplo
```kotlin
// core/data/src/main/kotlin/.../api/ProductApi.kt
interface ProductApi {
    @GET("products")
    suspend fun getProducts(): List<ProductDto>
}

// core/data/src/main/kotlin/.../datasource/ProductRemoteDataSource.kt
class ProductRemoteDataSource(private val api: ProductApi) {
    suspend fun getProducts(): List<Product> =
        api.getProducts().map { it.toDomain() }
}
```

### Room Ejemplo
```kotlin
// core/data/src/main/kotlin/.../database/ProductEntity.kt
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val price: Double
)

// core/data/src/main/kotlin/.../database/ProductDao.kt
@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<ProductEntity>
}
```

### Flow en ViewModel
```kotlin
class ProductsViewModel(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    fun loadProducts() {
        viewModelScope.launch {
            _products.value = getProductsUseCase()
        }
    }
}
```

### Composable con Estado
```kotlin
@Composable
fun ProductsScreen(viewModel: ProductsViewModel) {
    val products by viewModel.products.collectAsState()
    
    when {
        products.isEmpty() -> Text("No products")
        else -> ProductsList(products)
    }
}
```

---

## 🧪 Testing

### Unit Testing
```kotlin
// core/domain/src/test/kotlin/.../usecase/GetProductsUseCaseTest.kt
class GetProductsUseCaseTest {
    private val repository = FakeProductRepository()
    private val useCase = GetProductsUseCase(repository)

    @Test
    fun testGetProducts() = runTest {
        val result = useCase()
        assert(result.isNotEmpty())
    }
}
```

### UI Testing (Compose)
```kotlin
// feature/products/src/androidTest/.../ProductsScreenTest.kt
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun testProductsScreenDisplaysProducts() {
    composeTestRule.setContent {
        ProductsScreen(uiState = ProductsUiState.Success(testProducts))
    }
    composeTestRule.onNodeWithText("Producto 1").assertIsDisplayed()
}
```

---

## 🐛 Debugging

### Logcat Filtering
```
// En Android Studio: View → Tool Windows → Logcat
TAG:ProductsViewModel   // Filtrar por tag
WARN                    // Filtrar por nivel
```

### StrictMode (Detectar ANRs)
```kotlin
// En MainActivity.onCreate()
if (BuildConfig.DEBUG) {
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
    )
}
```

---

## 🔐 Seguridad

### API Keys y Secrets
**NUNCA** commitear credenciales:
```kotlin
// ❌ MAL - No hacer esto
const val API_KEY = "sk_live_abc123def456"

// ✅ BIEN - Usar local.properties o BuildConfig.gradle
```

```gradle
// buildTypes {
//     debug {
//         buildConfigField "String", "API_KEY", "\"${apiKey}\""
//     }
// }
```

---

## 📊 Herramientas Útiles

### Android Studio
- **Profiler** → Monitorear memoria, CPU
- **Layout Inspector** → Inspeccionar jerarquía de views
- **Debugger** → Breakpoints y watch expressions

### Android Device Monitor
```bash
adb logcat | grep ProductsViewModel
adb shell am start -n com.undef.aprovecha/.MainActivity
```

### Gradle Tasks
```bash
./gradlew tasks                    # Ver todas las tareas
./gradlew build                    # Compilar proyecto
./gradlew test                     # Ejecutar unit tests
./gradlew assembleDebug            # Build debug apk
./gradlew lint                     # Análisis estático
```

---

## 🎓 Artículos y Tutoriales Recomendados

### Medium - Android Developers
- [MVVM Best Practices](https://medium.com/androiddevelopers)
- [Coroutines Guide](https://medium.com/androiddevelopers/coroutines-on-android-part-i-getting-the-big-picture-337366dd6910)

### YouTube Channels
- [Android Developers](https://www.youtube.com/channel/UCVHFbqXqoYvEWM1Ddxl0QDg)
- [Philipp Lackner](https://www.youtube.com/@PhilippLackner)
- [Code with Mitch](https://www.youtube.com/@CodeWithMitch)

### Codelabs (Tutoriales Google)
- [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course)
- [Advanced Android Kotlin](https://developer.android.com/courses/kotlin-android-advanced/course)

---

## 🌐 Comunidades

- **Stack Overflow** → [Tag android](https://stackoverflow.com/questions/tagged/android)
- **Reddit** → [r/androiddev](https://www.reddit.com/r/androiddev/)
- **Dev.to** → [Android Tag](https://dev.to/t/android)
- **Kotlin Slack** → [Community Discussions](https://kotlinlang.slack.com/)

---

## 📋 Checklist para Code Reviews

- [ ] El código sigue la arquitectura MVVM
- [ ] Los módulos están bien separados
- [ ] No hay dependencias circulares
- [ ] Hay unit tests para la lógica de negocio
- [ ] Hay UI tests para Composables
- [ ] Los nombres son descriptivos
- [ ] Documentación actualizada
- [ ] Sin TODOs sin resolver
- [ ] Sin hardcoded strings
- [ ] Performance considerado

---

## 🚀 Recursos de Desempeño

- [Android Performance](https://developer.android.com/topic/performance)
- [Profiling Guide](https://developer.android.com/studio/profile)
- [Memory Optimization](https://developer.android.com/topic/performance/memory)

---

**Última actualización**: 18/05/2026

Para más información, consulta la [documentación oficial de Android](https://developer.android.com/).
