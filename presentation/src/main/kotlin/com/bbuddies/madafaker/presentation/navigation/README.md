# Navigation Actions Architecture

## Обзор

Система NavigationActions предоставляет чистую, тестируемую архитектуру навигации, которая отделяет навигационную логику от UI компонентов.

## Структура

```
presentation/navigation/
├── actions/
│   ├── NavigationAction.kt          # Базовый интерфейс
│   ├── SplashNavigationAction.kt    # Навигация для Splash экрана
│   ├── AuthNavigationAction.kt      # Навигация для Auth экрана
│   ├── MainNavigationAction.kt      # Навигация для Main экрана
│   └── NotificationPermissionNavigationAction.kt # Навигация для Permission экрана
├── AppNavigation.kt                 # Главный NavHost
└── NavigationTest.kt               # Тесты компиляции
```

## Принципы

### 1. **Инкапсуляция навигационной логики**
Каждый экран имеет свой NavigationAction класс, который содержит всю логику навигации для этого экрана.

### 2. **Type-Safe Navigation**
Используются Kotlin Serializable объекты вместо строк для маршрутов.

### 3. **Тестируемость**
NavigationAction классы легко мокаются для unit тестов.

### 4. **Единственная ответственность**
Каждый NavigationAction отвечает только за навигацию своего экрана.

## Использование

### В NavHost (AppNavigation.kt)

```kotlin
composable<SplashRoute> {
    val splashNavAction = SplashNavigationAction(navController)
    
    SplashScreen(
        navAction = splashNavAction,
        splashViewModel = hiltViewModel(),
        modifier = Modifier.fillMaxSize()
    )
}
```

### В Composable экранах

```kotlin
@Composable
fun SplashScreen(
    navAction: SplashNavigationAction,
    splashViewModel: SplashViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { destination ->
            navAction.navigateBasedOnDestination(destination)
        }
    }
    // UI код...
}
```

### В ViewModels

ViewModels больше не содержат навигационную логику. Вместо этого они эмитят события, которые обрабатываются в UI слое через NavigationActions.

## Преимущества

1. **🧪 Тестируемость** - NavigationActions легко мокаются
2. **🔒 Type Safety** - Compile-time проверка маршрутов
3. **🎯 Единственная ответственность** - Четкое разделение логики
4. **🔄 Переиспользование** - Общие навигационные паттерны
5. **📝 Читаемость** - Понятная структура кода
6. **🛡️ Безопасность** - Предотвращение навигационных ошибок

## Примеры NavigationActions

### SplashNavigationAction
- `navigateBasedOnDestination()` - Навигация на основе результата splash логики
- `navigateToMainFromSplash()` - Переход на главный экран
- `navigateToAuthFromSplash()` - Переход на экран авторизации

### AuthNavigationAction  
- `navigateAfterSuccessfulAuth()` - Навигация после успешной авторизации
- `navigateToMainAfterAuth()` - Переход на главный экран после авторизации
- `navigateBackFromAuth()` - Возврат с экрана авторизации

### MainNavigationAction
- `navigateToAuthFromMain()` - Переход на авторизацию (logout)
- `handleDeepLinkNavigation()` - Обработка deep links
- `navigateBackFromMain()` - Возврат с главного экрана

### NotificationPermissionNavigationAction
- `navigateToMainAfterPermission()` - Переход после обработки разрешений
- `navigateAfterPermissionGranted()` - Навигация при предоставлении разрешения
- `navigateAfterPermissionDenied()` - Навигация при отказе в разрешении

## Миграция

Старый подход:
```kotlin
// ❌ Прямая передача NavController
SplashScreen(
    navController = navController,
    onNavigateToMain = { navController.navigate("main") }
)
```

Новый подход:
```kotlin
// ✅ Использование NavigationAction
val splashNavAction = SplashNavigationAction(navController)
SplashScreen(navAction = splashNavAction)
```
