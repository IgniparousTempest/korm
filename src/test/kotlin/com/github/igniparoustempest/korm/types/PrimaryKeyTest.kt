package com.github.igniparoustempest.korm.types

import org.fluttercode.datafactory.impl.DataFactory
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PrimaryKeyTest {
    private val values: List<Any>
    private val keys: List<PrimaryKey<Any>>

    init {
        val df = DataFactory()
        values = listOf(df.getNumberBetween(-100, 100), df.name, df.birthDate, df.randomChar, df.chance(50), PrimaryKey(4), df.getNumberBetween(-100, 100) * 1.1925134692387, df.getNumberBetween(-100, 100) * 1.1925134692387f)
        keys = values.map { PrimaryKey(it) }
    }

    @Test
    fun testEquals_and_hashCode() {
        // Should be true
        for (i in 0 until keys.size) {
            val other = PrimaryKey(values[i])
            @Suppress("ReplaceCallWithBinaryOperator")
            assertTrue(keys[i].equals(other) && other.equals(keys[i]))
            assertEquals(keys[i].hashCode(), other.hashCode())
        }
        // Should be false
        for (i in 0 until keys.size / 2) {
            val other = keys[ keys.size - i - 1 ]
            @Suppress("ReplaceCallWithBinaryOperator")
            assertFalse(keys[i].equals(other) || other.equals(keys[i]))
            assertNotEquals(keys[i].hashCode(), other.hashCode())
        }
        @Suppress("ReplaceCallWithBinaryOperator")
        assertFalse(PrimaryKey(3).equals(null))
    }

    @Test
    fun testToString() {
        val stringsExpected = keys.map { "PK(${it.value})" }
        val stringsReceived = keys.map { it.toString() }
        assertEquals(stringsExpected, stringsReceived)
    }
}