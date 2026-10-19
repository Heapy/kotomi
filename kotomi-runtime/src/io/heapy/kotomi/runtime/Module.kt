package io.heapy.kotomi.runtime

/**
 * A module whose provider bodies are instrumented by the Kotomi FIR/IR compiler plugin.
 * Classes remain final. Public suspend non-Unit functions with context(KotomiContext)
 * and no value parameters are providers; interface providers may additionally take
 * dependencies resolved by exact type. Constructor parameters reference other modules.
 * FIR exposes typed keys as `ModuleName.Bindings.provider` for reads and replacements.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Module
