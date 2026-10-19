package io.heapy.kotomi.runtime.sample

import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.Module
import io.heapy.kotomi.time.TimeSource
import java.time.Clock
import java.util.concurrent.atomic.AtomicInteger

/**
 * E2E sample graph: `AppModule(UtilsModule, JdbcModule(ConfigModule), WorkerModule(ConfigModule))`.
 * `ConfigModule` is referenced by both `JdbcModule` and `WorkerModule`, so it is interned to
 * one module node. Classes and provider methods stay final.
 */

class Db(val url: String, val poolSize: Int)
class Worker(val url: String)
class Application(val db: Db, val worker: Worker, val clock: Clock)

@Module
class ConfigModule {
    val jdbcUrlCalls = AtomicInteger(0)

    context(_: KotomiContext)
    suspend fun jdbcUrl(): String {
        jdbcUrlCalls.incrementAndGet()
        return "jdbc:postgresql://localhost/db"
    }

    context(_: KotomiContext)
    suspend fun poolSize(): Int = 10
}

@Module
class UtilsModule {
    val timeSourceCalls = AtomicInteger(0)

    context(_: KotomiContext)
    suspend fun timeSource(): TimeSource {
        timeSourceCalls.incrementAndGet()
        return TimeSource(Clock.systemUTC())
    }

    context(_: KotomiContext)
    suspend fun clock(): Clock =
        timeSource().clock
}

@Module
class JdbcModule(
    private val config: ConfigModule,
) {
    context(_: KotomiContext)
    suspend fun dataSource(): Db =
        Db(config.jdbcUrl(), config.poolSize())
}

@Module
class WorkerModule(
    private val config: ConfigModule,
) {
    context(_: KotomiContext)
    suspend fun worker(): Worker =
        Worker(config.jdbcUrl())
}

@Module
class AppModule(
    private val utils: UtilsModule,
    private val jdbc: JdbcModule,
    private val worker: WorkerModule,
) {
    context(_: KotomiContext)
    suspend fun application(): Application =
        Application(
            db = jdbc.dataSource(),
            worker = worker.worker(),
            clock = utils.clock(),
        )
}
