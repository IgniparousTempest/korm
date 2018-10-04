package com.github.igniparoustempest.korm.conditions

import com.github.igniparoustempest.korm.testingtables.Dog
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ConditionsTest {
    @Test
    fun testNEQ() {
        val neqValue = Dog::breed neq "Dalmatian"
        val neqNull = Dog::breed neq null

        assertEquals("Dog.breed != ?", neqValue.sql)
        assertEquals(listOf("Dalmatian"), neqValue.values)
        assertEquals("Dog.breed IS NOT NULL", neqNull.sql)
        assertEquals(emptyList(), neqNull.values)
    }
}