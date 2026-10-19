package io.heapy.kotomi.runtime

internal expect class KotomiConcurrentMap<K : Any, V : Any>() {
    operator fun get(key: K): V?

    fun put(
        key: K,
        value: V,
    )

    fun putIfAbsent(
        key: K,
        value: V,
    ): V?
}

internal expect class KotomiConcurrentSet<T : Any>() {
    fun add(value: T): Boolean

    fun snapshot(): Set<T>
}

internal expect class KotomiConcurrentList<T : Any>() {
    fun add(value: T): Boolean

    fun snapshot(): List<T>
}
