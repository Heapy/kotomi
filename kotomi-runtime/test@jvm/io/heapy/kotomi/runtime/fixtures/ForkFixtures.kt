package io.heapy.kotomi.runtime.fixtures

import io.heapy.kotomi.runtime.BindingId
import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.KotomiRuntime
import io.heapy.kotomi.runtime.ProviderSignature
import io.heapy.kotomi.runtime.currentKotomiContext
import io.heapy.kotomi.time.TimeSource
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger

/**
 * Hand-wired cross-module fixtures for P2 fork verification. Module interning is
 * simulated by hand: one [ConfigModule_Kotomi] instance is passed to two dependents, so
 * both share one [BindingId].
 */

class FakeDataSource(val url: String)
class FakeWorker(val url: String)

fun fixedTimeSource(): TimeSource =
    TimeSource(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))

private const val CONFIG_FQN = "io.heapy.kotomi.runtime.fixtures.ConfigModule"

object ConfigModuleKeys {
    val jdbcUrl = BindingId(CONFIG_FQN, ProviderSignature("jdbcUrl"))
}

open class ConfigModule {
    val jdbcUrlCalls = AtomicInteger(0)

    context(_: KotomiContext)
    open suspend fun jdbcUrl(): String {
        jdbcUrlCalls.incrementAndGet()
        return "jdbc:postgresql://localhost/db"
    }
}

class ConfigModule_Kotomi(
    private val rt: KotomiRuntime,
) : ConfigModule() {
    context(_: KotomiContext)
    override suspend fun jdbcUrl(): String =
        rt.binding(ConfigModuleKeys.jdbcUrl) { super.jdbcUrl() }
}

private const val JDBC_FQN = "io.heapy.kotomi.runtime.fixtures.JdbcModule"

object JdbcModuleKeys {
    val dataSource = BindingId(JDBC_FQN, ProviderSignature("dataSource"))
}

open class JdbcModule(
    private val config: ConfigModule,
) {
    context(_: KotomiContext)
    open suspend fun dataSource(): FakeDataSource =
        FakeDataSource(config.jdbcUrl())
}

class JdbcModule_Kotomi(
    private val rt: KotomiRuntime,
    config: ConfigModule,
) : JdbcModule(config) {
    context(_: KotomiContext)
    override suspend fun dataSource(): FakeDataSource =
        rt.binding(JdbcModuleKeys.dataSource) { super.dataSource() }
}

private const val WORKER_FQN = "io.heapy.kotomi.runtime.fixtures.WorkerModule"

object WorkerModuleKeys {
    val worker = BindingId(WORKER_FQN, ProviderSignature("worker"))
}

open class WorkerModule(
    private val config: ConfigModule,
) {
    context(_: KotomiContext)
    open suspend fun worker(): FakeWorker =
        FakeWorker(config.jdbcUrl())
}

class WorkerModule_Kotomi(
    private val rt: KotomiRuntime,
    config: ConfigModule,
) : WorkerModule(config) {
    context(_: KotomiContext)
    override suspend fun worker(): FakeWorker =
        rt.binding(WorkerModuleKeys.worker) { super.worker() }
}

/**
 * #10 known-bad sample — a service that captures [KotomiContext] for late graph reads,
 * violating "No late graph-read" (invariant #6): dependencies must be materialized
 * inside the provider body. Kept compiling on purpose; v1 has no automatic diagnosis
 * (the structural enforcement is deferred to Kotlin Local Lifetimes), so the test
 * documents the silently-wrong behavior the violation produces.
 */
class LateReadService(
    private val capturedContext: KotomiContext,
    private val config: ConfigModule,
) {
    suspend fun url(): String {
        val read: suspend context(KotomiContext) () -> String = { config.jdbcUrl() }
        // positional context-argument invocation — the same mechanism the runtime uses.
        return read(capturedContext)
    }
}

private const val LATE_READ_FQN = "io.heapy.kotomi.runtime.fixtures.LateReadModule"

object LateReadModuleKeys {
    val lateService = BindingId(LATE_READ_FQN, ProviderSignature("lateService"))
}

open class LateReadModule(
    private val config: ConfigModule,
) {
    context(_: KotomiContext)
    open suspend fun lateService(): LateReadService =
        LateReadService(currentKotomiContext(), config)
}

class LateReadModule_Kotomi(
    private val rt: KotomiRuntime,
    config: ConfigModule,
) : LateReadModule(config) {
    context(_: KotomiContext)
    override suspend fun lateService(): LateReadService =
        rt.binding(LateReadModuleKeys.lateService) { super.lateService() }
}
