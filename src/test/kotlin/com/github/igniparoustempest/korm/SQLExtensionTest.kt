package com.github.igniparoustempest.korm

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.util.Random
import kotlin.test.assertEquals

class SQLExtensionTest {
    private val preparedStatement = spy(PreparedStatement::class.java)
    private val resultSet = spy(ResultSet::class.java)
    private val random = Random()

    @Test
    fun testGetBool() {
        val value = random.nextBoolean()
        Mockito.`when`(resultSet.wasNull()).thenReturn(false)
        Mockito.`when`(resultSet.getBoolean(any(String::class.java))).thenReturn(value)
        assertEquals(value, resultSet.getBool(""))
        Mockito.`when`(resultSet.wasNull()).thenReturn(true)
        assertEquals(null, resultSet.getBool(""))
    }

    @Test
    fun testGetFloating() {
        val value = random.nextFloat()
        Mockito.`when`(resultSet.wasNull()).thenReturn(false)
        Mockito.`when`(resultSet.getFloat(any(String::class.java))).thenReturn(value)
        assertEquals(value, resultSet.getFloating(""))
        Mockito.`when`(resultSet.wasNull()).thenReturn(true)
        assertEquals(null, resultSet.getFloating(""))
    }

    @Test
    fun testGetInteger() {
        val value = random.nextInt()
        Mockito.`when`(resultSet.wasNull()).thenReturn(false)
        Mockito.`when`(resultSet.getInt(any(String::class.java))).thenReturn(value)
        assertEquals(value, resultSet.getInteger(""))
        Mockito.`when`(resultSet.wasNull()).thenReturn(true)
        assertEquals(null, resultSet.getInteger(""))
    }

    @Test
    fun testSetBool() {
        val index = random.nextInt()
        val value = random.nextBoolean()

        preparedStatement.setBool(index, value)
        verify(preparedStatement).setBoolean(index, value)
        preparedStatement.setBool(index, null)
        verify(preparedStatement).setNull(index, Types.INTEGER)
    }

    @Test
    fun testSetFloating() {
        val index = random.nextInt()
        val value = random.nextFloat()

        preparedStatement.setFloating(index, value)
        verify(preparedStatement).setFloat(index, value)
        preparedStatement.setFloating(index, null)
        verify(preparedStatement).setNull(index, Types.REAL)
    }

    @Test
    fun testSetInteger() {
        val index = random.nextInt()
        val value = random.nextInt()

        preparedStatement.setInteger(index, value)
        verify(preparedStatement).setInt(index, value)
        preparedStatement.setInteger(index, null)
        verify(preparedStatement).setNull(index, Types.INTEGER)
    }
}