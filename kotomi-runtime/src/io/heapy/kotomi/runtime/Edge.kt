package io.heapy.kotomi.runtime

/**
 * A recorded binding-graph edge `from -> to`: provider [from] called provider [to]
 * during realization. Edges are a global fact of execution, accumulated per
 * [KotomiRuntime] (forks share the same edge store).
 */
data class Edge(
    val from: BindingId,
    val to: BindingId,
)

/** Thread-safe accumulator of [Edge]s. */
internal class EdgeStore {
    private val edges = KotomiConcurrentSet<Edge>()

    fun add(from: BindingId, to: BindingId) {
        realized(edges.add(Edge(from, to)))
    }

    fun snapshot(): Set<Edge> =
        edges.snapshot()
}
