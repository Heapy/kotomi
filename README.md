# Kotomi

**Typed dependency graphs, isolated test overrides, and practical Kotlin libraries.**

Kotomi helps you wire a Kotlin application, manage its lifetime, and test it with
controlled dependencies. Providers are ordinary Kotlin functions. The compiler
connects them; the runtime shares their results and tracks the dependencies they
actually use.

- **Wire with types.** Generate graph bindings at compile time and catch missing,
  ambiguous, or cyclic autowired dependencies before running the application.
- **Test with a fork.** Replace a configuration value or service in an isolated
  graph. Recompute its dependents while sharing unaffected, already-created values.
- **Own the lifecycle.** Start dependencies in order and stop them in reverse order.
  Shared initialization belongs to the graph, so cancelling a caller does not cancel
  work needed by other callers.
- **Add what you need.** Use configuration, logging, time, and OpenAPI libraries
  alongside the runtime, or use them independently where their APIs allow it.

Current runtime support: **Kotlin 2.4.21, JVM 21+**. The compiler plugin must match
the Kotlin compiler version.

## Quickstart

This example uses locally built artifacts:

```sh
git clone https://github.com/Heapy/kotomi.git
cd kotomi
./ktc-plugins sync
./publish-local
mkdir ../kotomi-quickstart
cp kotlin kotlin.bat ../kotomi-quickstart/
cd ../kotomi-quickstart
```

Create `module.yaml`:

```yaml
product: jvm/app

repositories:
  - url: mavenLocal

dependencies:
  - io.heapy.kotomi:kotomi-runtime:1.2.0

settings:
  kotlin:
    version: 2.4.21
    compilerPlugins:
      - id: io.heapy.kotomi.runtime
        dependency: io.heapy.kotomi:kotomi-runtime-compiler:1.2.0
  jvm:
    release: 21
```

Create `src/main.kt`:

```kotlin
import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.Module
import io.heapy.kotomi.runtime.graph

@Module
class Greetings {
    context(_: KotomiContext)
    suspend fun name(): String = "world"

    context(_: KotomiContext)
    suspend fun message(): String = "Hello, ${name()}!"
}

suspend fun main() {
    val app = graph<Greetings>()
    try {
        println(app { message() })
        val test = app.fork {
            replace(Greetings.Bindings.name, "Kotomi")
        }
        try {
            println(test { message() })
        } finally {
            test.stop()
        }
    } finally {
        app.stop()
    }
}
```

Run it:

```sh
./kotlin run
```

```text
Hello, world!
Hello, Kotomi!
```

The fork changes `name`, so `message` is recomputed using that replacement. The
original graph keeps its own values. The same pattern lets a test replace a clock,
configuration, or a service without changing application wiring.

## Libraries

All artifacts use the Maven group `io.heapy.kotomi`.

| Library | Use it for |
| --- | --- |
| `kotomi-runtime` + `kotomi-runtime-compiler` | Typed graphs, services, lifecycle, forks, and generated mocks |
| `kotomi-runtime-wrappers` | Logging, metrics, and retries around service calls |
| `kotomi-config` + `kotomi-config-dotenv` | Configuration and environment-file loading |
| `kotomi-logging`, `kotomi-logging-slf4j`, `kotomi-logging-jul` | Logging pipelines and JVM logging bridges |
| `kotomi-time` + `kotomi-time-test-fixtures` | Explicit time sources and controllable time in tests |
| `kotomi-api-dsl` + `kotomi-api-dsl-ui` | OpenAPI 3.2 documents and their web presentation |
| `kotomi-to-be-injected` | Lightweight lazy bean delegates and reflective module construction |
| `kotomi-utils-slf4j` | Small SLF4J logger helpers |

Read the [runtime guide](kotomi-runtime.md) for module and service wiring,
decorators, mocks, graph inspection, and fork semantics. Individual libraries have
README files in their module directories. The [technology roadmap](docs/technology.md)
describes the longer-term direction.

## Development

```sh
./ktc-plugins sync
./ktc-plugins verify
./kotlin build --platform jvm
./kotlin check
```

See [publishing](docs/publishing.md) for local artifacts and release configuration.

Licensed under [Apache License 2.0](LICENSE-Apache2).
