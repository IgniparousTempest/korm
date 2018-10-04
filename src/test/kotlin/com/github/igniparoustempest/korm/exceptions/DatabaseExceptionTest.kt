package com.github.igniparoustempest.korm.exceptions

import org.fluttercode.datafactory.impl.DataFactory
import org.junit.jupiter.api.Test
import java.sql.SQLException
import kotlin.test.assertEquals

class DatabaseExceptionTest {
    @Test
    fun constructor() {
        val df = DataFactory()
        val cause = SQLException()
        val message = (0..10).joinToString(" ") { df.randomWord }
        val sql = (0..10).joinToString(" ") { df.randomWord }
        val exception = DatabaseException(message, cause, sql)
        assertEquals(cause, exception.cause)
        assertEquals(sql, exception.sql)
    }
}