package io.heapy.kotomi.runtime

/**
 * Thrown at runtime when a binding re-enters itself within the same logical
 * [KotomiContext] (the [path] ends with the offending repeated [BindingId]).
 *
 * A read from a different coroutine/context is a wait, not a cycle; only a reentrant
 * read in the same caller-stack is reported here.
 */
class KotomiCycleException(
    val path: List<BindingId>,
) : RuntimeException(
    "Binding cycle detected: ${path.joinToString(" -> ") { it.display() }}",
)
