package io.heapy.kotomi.runtime.fixtures

import io.heapy.kotomi.runtime.BindingId
import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.KotomiRuntime
import io.heapy.kotomi.runtime.ProviderSignature
import io.heapy.kotomi.time.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import java.time.Clock
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Hand-written "generated" module forms for P1 verification — exactly the shape the
 * KSP processor will emit in P3 (`${M}_Kotomi(rt) : ${M}()` overriding each provider
 * with `rt.binding(${M}Keys.x) { super.x() }`, plus `object ${M}Keys`). Writing them by
 * hand both verifies the runtime in isolation and proves the shape compiles (notably
 * `super.x()` inside the non-inline `binding` lambda).
 */

private const val UTILS_FQN = "io.heapy.kotomi.runtime.fixtures.UtilsModule"

object UtilsModuleKeys {
    val timeSource = BindingId(UTILS_FQN, ProviderSignature("timeSource"))
    val clock = BindingId(UTILS_FQN, ProviderSignature("clock"))
}

open class UtilsModule {
    val timeSourceCalls = AtomicInteger(0)
    val clockCalls = AtomicInteger(0)

    context(_: KotomiContext)
    open suspend fun timeSource(): TimeSource {
        timeSourceCalls.incrementAndGet()
        return TimeSource(Clock.systemUTC())
    }

    context(_: KotomiContext)
    open suspend fun clock(): Clock {
        clockCalls.incrementAndGet()
        // intra-module self-call: virtual dispatch records edge clock -> timeSource.
        return timeSource().clock
    }
}

class UtilsModule_Kotomi(
    private val rt: KotomiRuntime,
) : UtilsModule() {
    context(_: KotomiContext)
    override suspend fun timeSource(): TimeSource =
        rt.binding(UtilsModuleKeys.timeSource) { super.timeSource() }

    context(_: KotomiContext)
    override suspend fun clock(): Clock =
        rt.binding(UtilsModuleKeys.clock) { super.clock() }
}

private const val BRANCHY_FQN = "io.heapy.kotomi.runtime.fixtures.BranchyModule"

object BranchyModuleKeys {
    val config = BindingId(BRANCHY_FQN, ProviderSignature("config"))
    val systemSource = BindingId(BRANCHY_FQN, ProviderSignature("systemSource"))
    val source = BindingId(BRANCHY_FQN, ProviderSignature("source"))
}

open class BranchyModule(
    private val useConfig: Boolean,
) {
    context(_: KotomiContext)
    open suspend fun config(): String = "config-source"

    context(_: KotomiContext)
    open suspend fun systemSource(): String = "system-source"

    context(_: KotomiContext)
    open suspend fun source(): String =
        if (useConfig) config() else systemSource()
}

class BranchyModule_Kotomi(
    private val rt: KotomiRuntime,
    useConfig: Boolean,
) : BranchyModule(useConfig) {
    context(_: KotomiContext)
    override suspend fun config(): String =
        rt.binding(BranchyModuleKeys.config) { super.config() }

    context(_: KotomiContext)
    override suspend fun systemSource(): String =
        rt.binding(BranchyModuleKeys.systemSource) { super.systemSource() }

    context(_: KotomiContext)
    override suspend fun source(): String =
        rt.binding(BranchyModuleKeys.source) { super.source() }
}

private const val CYCLE_FQN = "io.heapy.kotomi.runtime.fixtures.CycleModule"

object CycleModuleKeys {
    val a = BindingId(CYCLE_FQN, ProviderSignature("a"))
    val b = BindingId(CYCLE_FQN, ProviderSignature("b"))
}

open class CycleModule {
    context(_: KotomiContext)
    open suspend fun a(): Int = b()

    context(_: KotomiContext)
    open suspend fun b(): Int = a()
}

class CycleModule_Kotomi(
    private val rt: KotomiRuntime,
) : CycleModule() {
    context(_: KotomiContext)
    override suspend fun a(): Int =
        rt.binding(CycleModuleKeys.a) { super.a() }

    context(_: KotomiContext)
    override suspend fun b(): Int =
        rt.binding(CycleModuleKeys.b) { super.b() }
}

class ServiceFailure : RuntimeException("service realization failed")

private const val SERVICE_FQN = "io.heapy.kotomi.runtime.fixtures.ServiceModule"

object ServiceModuleKeys {
    val gated = BindingId(SERVICE_FQN, ProviderSignature("gated"))
    val failing = BindingId(SERVICE_FQN, ProviderSignature("failing"))
    val cancelling = BindingId(SERVICE_FQN, ProviderSignature("cancelling"))
    val plain = BindingId(SERVICE_FQN, ProviderSignature("plain"))
}

open class ServiceModule(
    private val gate: CompletableDeferred<Unit>? = null,
    private val started: CompletableDeferred<Unit>? = null,
) {
    val gatedCalls = AtomicInteger(0)
    val failingCalls = AtomicInteger(0)
    val cancellingCalls = AtomicInteger(0)

    context(_: KotomiContext)
    open suspend fun gated(): String {
        gatedCalls.incrementAndGet()
        started?.complete(Unit)
        gate?.await()
        return "gated-value"
    }

    context(_: KotomiContext)
    open suspend fun failing(): String {
        failingCalls.incrementAndGet()
        throw ServiceFailure()
    }

    context(_: KotomiContext)
    open suspend fun cancelling(): String {
        cancellingCalls.incrementAndGet()
        throw CancellationException("provider-thrown cancellation")
    }

    context(_: KotomiContext)
    open suspend fun plain(): String = "plain-value"
}

class ServiceModule_Kotomi(
    private val rt: KotomiRuntime,
    gate: CompletableDeferred<Unit>? = null,
    started: CompletableDeferred<Unit>? = null,
) : ServiceModule(gate, started) {
    context(_: KotomiContext)
    override suspend fun gated(): String =
        rt.binding(ServiceModuleKeys.gated) { super.gated() }

    context(_: KotomiContext)
    override suspend fun failing(): String =
        rt.binding(ServiceModuleKeys.failing) { super.failing() }

    context(_: KotomiContext)
    override suspend fun cancelling(): String =
        rt.binding(ServiceModuleKeys.cancelling) { super.cancelling() }

    context(_: KotomiContext)
    override suspend fun plain(): String =
        rt.binding(ServiceModuleKeys.plain) { super.plain() }
}

class CloseableResource(
    val active: Boolean = true,
) : AutoCloseable {
    var closed: Boolean = false
        private set

    override fun close() {
        closed = true
    }
}

private const val CLOSEABLE_FQN = "io.heapy.kotomi.runtime.fixtures.CloseableModule"

object CloseableModuleKeys {
    val flag = BindingId(CLOSEABLE_FQN, ProviderSignature("flag"))
    val shared = BindingId(CLOSEABLE_FQN, ProviderSignature("shared"))
    val coneResource = BindingId(CLOSEABLE_FQN, ProviderSignature("coneResource"))
}

open class CloseableModule {
    context(_: KotomiContext)
    open suspend fun flag(): Boolean = true

    context(_: KotomiContext)
    open suspend fun shared(): CloseableResource = CloseableResource()

    context(_: KotomiContext)
    open suspend fun coneResource(): CloseableResource =
        // depends on flag() so replacing flag pulls coneResource into the cone.
        CloseableResource(active = flag())
}

class CloseableModule_Kotomi(
    private val rt: KotomiRuntime,
) : CloseableModule() {
    context(_: KotomiContext)
    override suspend fun flag(): Boolean =
        rt.binding(CloseableModuleKeys.flag) { super.flag() }

    context(_: KotomiContext)
    override suspend fun shared(): CloseableResource =
        rt.binding(CloseableModuleKeys.shared) { super.shared() }

    context(_: KotomiContext)
    override suspend fun coneResource(): CloseableResource =
        rt.binding(CloseableModuleKeys.coneResource) { super.coneResource() }
}

private const val LATE_CLOSE_FQN = "io.heapy.kotomi.runtime.fixtures.LateCloseModule"

object LateCloseModuleKeys {
    val resource = BindingId(LATE_CLOSE_FQN, ProviderSignature("resource"))
}

open class LateCloseModule(
    private val created: CompletableDeferred<CloseableResource>,
    private val release: CountDownLatch,
) {
    context(_: KotomiContext)
    open suspend fun resource(): CloseableResource {
        val resource = CloseableResource()
        created.complete(resource)
        // Thread-blocking on purpose: with no suspension point left, realm close cannot
        // cancel this body — it completes after the close sweep (the race under test).
        release.await()
        return resource
    }
}

class LateCloseModule_Kotomi(
    private val rt: KotomiRuntime,
    created: CompletableDeferred<CloseableResource>,
    release: CountDownLatch,
) : LateCloseModule(created, release) {
    context(_: KotomiContext)
    override suspend fun resource(): CloseableResource =
        rt.binding(LateCloseModuleKeys.resource) { super.resource() }
}
