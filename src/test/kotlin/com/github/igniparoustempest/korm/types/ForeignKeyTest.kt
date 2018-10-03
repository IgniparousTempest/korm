package com.github.igniparoustempest.korm.types

import com.github.igniparoustempest.korm.testingtables.Dog
import com.github.igniparoustempest.korm.testingtables.MultiplePrimaries
import org.junit.jupiter.api.Test
import kotlin.test.*

class ForeignKeyTest {
    private val fk1 = ForeignKey(Dog::dogId, PrimaryKey(4))
    private val fk2 = ForeignKey(Dog::dogId, PrimaryKey(4))
    private val fk3 = ForeignKey(Dog::dogId, PrimaryKey(5))
    private val fk4 = ForeignKey(MultiplePrimaries::id2, PrimaryKey("abc"))

    @Suppress("ReplaceCallWithBinaryOperator")
    @Test
    fun testEquals_and_hashCode() {
        assertFalse(fk1.equals(null))
        assertTrue(fk1.equals(fk1))
        assertTrue(fk1.equals(fk2) && fk2.equals(fk1))
        assertFalse(fk1.equals(fk3) || fk3.equals(fk1))
        assertFalse(fk1.equals(fk4) || fk4.equals(fk1))


        assertEquals(fk1.hashCode(), fk1.hashCode())
        assertEquals(fk1.hashCode(), fk2.hashCode())
        assertNotEquals(fk1.hashCode(), fk3.hashCode())
        assertNotEquals(fk1.hashCode(), fk4.hashCode())
    }

    @Test
    fun testToString() {
        val keys = listOf(fk1, fk2, fk3, fk4)
        val stringsExpected = keys.map { "FK(${it.value})" }
        val stringsReceived = keys.map { it.toString() }
        assertEquals(stringsExpected, stringsReceived)
    }

    @Test
    fun testForeignColumnName_and_foreignTableName() {
        val fk5 = ForeignKey(null, null, 5)
        val fk6 = ForeignKey("abc", "def", 5)
        assertFailsWith<Exception> { fk5.foreignColumnName }
        assertFailsWith<Exception> { fk5.foreignTableName }
        assertEquals(fk6.foreignColumnName, "abc")
        assertEquals(fk6.foreignTableName, "def")
    }
}