package com.github.igniparoustempest.korm.types

import com.github.igniparoustempest.korm.testingtables.Simple
import com.github.igniparoustempest.korm.testingtables.Student
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RowTest {
    @Test
    fun testGet() {
        val row = Row(mapOf("data" to "abc"))
        assertEquals("abc", row[Simple::data])
        assertEquals("abc", row["data"])
        assertEquals(null, row[Student::height])
    }
}