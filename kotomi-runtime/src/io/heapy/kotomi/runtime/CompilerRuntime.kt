package io.heapy.kotomi.runtime

/** Runtime entry point emitted into provider bodies by the Kotomi compiler plugin. */
context(context: KotomiContext)
suspend fun <T> kotomiBinding(
    owner: String,
    name: String,
    parameters: String,
    body: suspend context(KotomiContext) () -> T,
    decorate: (T) -> T = { it },
): T {
    val id = BindingId(owner, ProviderSignature(name, parameters))
    return if (context.bypassBinding == id) {
        decorate(body(KotomiContext(context.realm, context.callerStack)))
    } else {
        context.realm.runtime.binding(id) { decorate(body()) }
    }
}
