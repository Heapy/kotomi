package io.heapy.kotomi.di.delegate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MutableBeanTest {
    @Test
    fun `initializer runs lazily once`() {
        var initializations = 0
        val bean = MutableBean(
            initializer = {
                initializations += 1
                "value"
            },
            name = "test.bean",
        )

        assertFalse(bean.isInitialized)
        assertEquals("value", bean.value)
        assertEquals("value", bean.value)
        assertEquals(1, initializations)
        assertTrue(bean.isInitialized)
    }

    @Test
    fun `mock prevents initializer from running`() {
        var initializations = 0
        val bean = MutableBean(
            initializer = {
                initializations += 1
                "value"
            },
            name = "test.bean",
        )

        val mocked = bean.mock {
            "mocked"
        }

        assertEquals("mocked", mocked)
        assertEquals("mocked", bean.value)
        assertEquals(0, initializations)
    }

    @Test
    fun `setValue prevents initializer from running`() {
        var initializations = 0
        val bean = MutableBean(
            initializer = {
                initializations += 1
                "value"
            },
            name = "test.bean",
        )

        bean.setValue("set")

        assertEquals("set", bean.value)
        assertEquals(0, initializations)
    }

    @Test
    fun `initializer can retry after failure`() {
        var attempts = 0
        val bean = MutableBean(
            initializer = {
                attempts += 1
                if (attempts == 1) {
                    error("first attempt")
                }
                "value"
            },
            name = "test.bean",
        )

        assertThrows<IllegalStateException> {
            val _ = bean.value
        }
        assertFalse(bean.isInitialized)
        assertEquals("value", bean.value)
        assertEquals(2, attempts)
    }

    @Test
    fun `initialized bean rejects replacement`() {
        val bean = MutableBean(
            initializer = { "value" },
            name = "test.bean",
        )
        val _ = bean.value

        assertThrows<IllegalStateException> {
            bean.setValue("replacement")
        }
        assertThrows<IllegalStateException> {
            val _ = bean.mock { "replacement" }
        }
    }
}
