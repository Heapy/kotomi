package io.heapy.kotomi.di.delegate

import kotlin.properties.ReadOnlyProperty

/**
 * Creates property delegate for a bean.
 *
 * Usage:
 *
 * ```kotlin
 * class MyModule {
 *     val myBean by bean {
 *         MyBean()
 *     }
 * }
 * ```
 */
fun <V> bean(
    initializer: () -> V,
): ReadOnlyProperty<Any, MutableBean<V>> =
    ApplicationBeanDelegate(
        initializer = initializer,
    )
