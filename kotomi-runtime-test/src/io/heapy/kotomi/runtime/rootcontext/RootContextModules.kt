package io.heapy.kotomi.runtime.rootcontext

import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.Module
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Root-context sample: providers realize on the framework-owned realm scope whose
 * `CoroutineContext` is `dispatcher + rootContext`. The graph builder's `rootContext`
 * is how the bootstrap installs always-present elements (e.g. the root logging context
 * element), so a provider can rely on them being in `coroutineContext`.
 */
class TenantElement(
    val name: String,
) : AbstractCoroutineContextElement(TenantElement) {
    companion object Key : CoroutineContext.Key<TenantElement>
}

@Module
class RootContextModule {
    context(_: KotomiContext)
    suspend fun tenant(): String =
        coroutineContext[TenantElement]?.name ?: "absent"
}
