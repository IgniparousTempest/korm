package com.github.igniparoustempest.korm.types

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PrimaryKeyAutoTest {
    @Test
    fun testSafeValue() {
        assertEquals(3, PrimaryKeyAuto(3).safeValue, "Should get safe value for set primary keys")
        assertEquals(null, PrimaryKeyAuto().safeValue, "Should get safe value for unset primary keys")
    }
}