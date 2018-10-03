package com.github.igniparoustempest.korm.exceptions

import org.fluttercode.datafactory.impl.DataFactory
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UnsupportedDataTypeExceptionTest {
    @Test
    fun constructor() {
        val df = DataFactory()
        val message = (0..10).joinToString(" ") { df.randomWord }
        assertEquals(message, UnsupportedDataTypeException(message).message)
    }
}