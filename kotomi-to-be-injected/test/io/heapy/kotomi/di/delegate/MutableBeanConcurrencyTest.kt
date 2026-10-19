package io.heapy.kotomi.di.delegate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class MutableBeanConcurrencyTest {
    @Test
    fun `concurrent readers initialize once`() {
        val initializationCount = AtomicInteger()
        val bean = MutableBean(
            initializer = {
                val _ = initializationCount.incrementAndGet()
                "value"
            },
            name = "test.bean",
        )
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(8)

        try {
            val futures = List(64) {
                executor.submit<String> {
                    start.await()
                    bean.value
                }
            }
            start.countDown()

            assertEquals(List(64) { "value" }, futures.map { it.get() })
            assertEquals(1, initializationCount.get())
        } finally {
            val _ = executor.shutdownNow()
        }
    }
}
