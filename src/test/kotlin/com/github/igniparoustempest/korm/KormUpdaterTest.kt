package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.testingtables.Student
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KormUpdaterTest {
    @Test
    fun set() {
        val condition = (Student::age eq 22) and (Student::surname eq "George")
        val updater = (Student::age set 23) and (Student::maidenName set null) onCondition condition
        assertEquals("Student.age = ? AND Student.surname = ?", updater.condition?.sql)
        assertEquals(listOf(22, "George"), updater.condition?.values)
        assertEquals("age = ?, maidenName = NULL", updater.sql)
        assertEquals(listOf(23), updater.values)
    }
}