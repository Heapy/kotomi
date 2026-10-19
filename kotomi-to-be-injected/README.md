# To Be Injected

Minimal and simple dependency injection library for Kotlin/JVM.
Based on idea of using Kotlin `lazy` delegate:

```kotlin
open class MyModule {
    open val myBean by lazy {
        MyBean()
    }
}
```

Modules do not need to inherit framework base types and remain directly usable
in tests.

## Installation

Add the following dependency:

```kotlin
dependencies {
    implementation("io.heapy.kotomi:kotomi-to-be-injected:1.2.0")
}
```

Requires Java 21 or newer.

For Maven:

```xml
<dependency>
    <groupId>io.heapy.kotomi</groupId>
    <artifactId>kotomi-to-be-injected</artifactId>
    <version>1.2.0</version>
</dependency>
```

## Usage

This is a simplified example of a multi-module project with dependencies
between the modules.

```kotlin
import io.heapy.kotomi.di.delegate.bean

// UtilsModule.kt
class UtilsModule {
    val configuration by bean {
        Configuration()
    }

    val httpClient by bean {
        HttpClient(
            configuration = configuration.value,
        )
    }
}

// DaoModule.kt
class DaoModule(
    val utilsModule: UtilsModule,
) {
    val userDao: MutableBean<UserDao> by bean {
        DefaultUserDao(
            configuration = utilsModule.configuration.value,
        )
    }
}

// ServiceModule.kt
class ServiceModule(
    val utilsModule: UtilsModule,
    val daoModule: DaoModule,
) {
    val userService by bean {
        UserService(
            userDao = daoModule.userDao.value,
            httpClient = utilsModule.httpClient.value,
        )
    }
}

// ControllerModule.kt
class ControllerModule(
    val serviceModule: ServiceModule,
) {
    val userController by bean {
        UserController(
            userService = serviceModule.userService.value,
        )
    }
}

// ApplicationModule.kt
class ApplicationModule(
    val controllerModule: ControllerModule,
) {
    val server by bean {
        Server(
            userController = controllerModule.userController.value,
        )
    }
}

// main.kt
fun main() {
    val utilsModule = UtilsModule()
    val daoModule = DaoModule(utilsModule)
    val serviceModule = ServiceModule(utilsModule, daoModule)
    val controllerModule = ControllerModule(serviceModule)
    val applicationModule = ApplicationModule(controllerModule)

    applicationModule.server.value.start()
}
```

Modules are ordinary classes, so the compiler checks the wiring: a missing
dependency is a compile error.

## Reflective wiring

The wiring can be derived from the constructors using JVM reflection:

```kotlin
import io.heapy.kotomi.di.delegate.buildModule

fun main() {
    val applicationModule = buildModule<ApplicationModule>()
    applicationModule.server.value.start()
}
```

Every constructor parameter must itself be a module. Module dependencies must
not contain cycles.

`buildModules` returns a registry when tests or application code need more than
the root module:

```kotlin
import io.heapy.kotomi.di.delegate.buildModules

val modules = buildModules<ApplicationModule>()
val applicationModule = modules<ApplicationModule>()
val utilsModule = modules<UtilsModule>()
```

## Testing

A bean typed as an interface can be replaced with a fake before its first read.

```kotlin
class UserServiceTest {
    @Test
    fun `user service reads the user from the dao`() {
        val utilsModule = UtilsModule()
        val daoModule = DaoModule(utilsModule)
        val serviceModule = ServiceModule(utilsModule, daoModule)

        daoModule.userDao.setValue(
            FakeUserDao(
                users = mapOf(1 to User(id = 1, name = "Test User")),
            ),
        )

        assertEquals(
            User(
                id = 1,
                name = "Test User",
            ),
            serviceModule.userService.value.getUser(1),
        )
    }
}
```

Use `mock` instead of `setValue` when the replacement needs to be built lazily.
Both calls fail once the bean is initialized.

## License

This project is licensed under the Apache License 2.0 – see the [LICENSE-Apache2](../LICENSE-Apache2) file for details.
