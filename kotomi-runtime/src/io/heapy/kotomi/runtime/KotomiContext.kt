package io.heapy.kotomi.runtime

/**
 * The single source of truth for "where am I in the graph". Carries the current
 * [Realm] (which owns memoized values) and the caller-stack used for cycle detection.
 */
class KotomiContext internal constructor(
    internal val realm: Realm,
    val callerStack: List<BindingId>,
    internal val bypassBinding: BindingId? = null,
) {
    internal fun enter(id: BindingId): KotomiContext =
        KotomiContext(realm, callerStack + id)
}

/**
 * Bridge so generated overrides can be written `context(_: KotomiContext)` and still read
 * the current context without tripping the context-parameter-name-mismatch warning under
 * `allWarningsAsErrors`.
 */
context(ctx: KotomiContext)
fun currentKotomiContext(): KotomiContext = ctx
