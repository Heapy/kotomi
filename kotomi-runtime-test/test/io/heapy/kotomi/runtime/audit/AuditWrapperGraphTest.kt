package io.heapy.kotomi.runtime.audit

import io.heapy.kotomi.runtime.*
import io.heapy.kotomi.runtime.wrappers.MethodMeta
import io.heapy.kotomi.runtime.wrappers.ParameterMeta
import io.heapy.kotomi.runtime.test.wrapper.AuditTrail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class AuditWrapperGraphTest {
    /**
     * A library-declared third-party wrapper decorates a service — custom
     * annotation, custom helper, custom context parameter — with zero changes to the
     * core processor.
     */
    @Test
    fun `custom wrapper metadata selects its helper and supplies complete method metadata`() = runTest {
        val trail = AuditTrail()
        val graph = graph<AuditModule> { dispatcher = Dispatchers.Default }
        val service = graph.read { module<AuditModule>().accountService() }

        val result = withTrail(trail) { service.open("main") }

        assertAll(
            { assertEquals("account:main", result) },
            { assertEquals(listOf(MethodMeta(
                owner = "io.heapy.kotomi.runtime.audit.AccountService",
                name = "open",
                parameters = listOf(ParameterMeta("name", "kotlin.String")),
                returnType = "kotlin.String",
                isSuspend = true,
            )), trail.methods) },
            {
                assertEquals(
                    listOf("io.heapy.kotomi.runtime.audit.AccountService.open"),
                    trail.entries,
                )
            },
        )
        graph.stop()
    }
}

/** Supplies the [AuditTrail] context argument positionally. */
private suspend fun <T> withTrail(
    trail: AuditTrail,
    block: suspend context(AuditTrail) () -> T,
): T = block(trail)
