package io.heapy.kotomi.runtime.audit

import io.heapy.kotomi.runtime.KotomiContext
import io.heapy.kotomi.runtime.Module
import io.heapy.kotomi.runtime.test.wrapper.Audit
import io.heapy.kotomi.runtime.test.wrapper.AuditTrail

/**
 * WrapperPlugin SPI sample: `@Audit` is a "third-party" wrapper the core processor has
 * never heard of — it is discovered via ServiceLoader from `kotomi-runtime-test-wrapper`
 * through @CallWrapper metadata on the annotation. Same user model as built-ins: the wrapped method declares the
 * wrapper's context parameter, the caller provides it.
 */

interface AccountService {
    @Audit
    context(_: AuditTrail)
    suspend fun open(name: String): String
}

class RealAccountService : AccountService {
    context(_: AuditTrail)
    override suspend fun open(name: String): String = "account:$name"
}

@Module
class AuditModule {
    context(_: KotomiContext)
    suspend fun accountService(): AccountService =
        RealAccountService()
}
