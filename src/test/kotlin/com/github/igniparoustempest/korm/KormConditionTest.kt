package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.testingtables.Student
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KormConditionTest {
    @Test
    fun eq() {
        val condition = (Student::age eq 12) and (Student::firstName eq "Fred") or (Student::surname eq "George")
        assertEquals("Student.age = ? AND Student.firstName = ? OR Student.surname = ?", condition.sql)
        assertEquals(listOf(12, "Fred", "George"), condition.values)
    }

    @Test
    fun neq() {
        val condition = (Student::age neq 12) and (Student::firstName neq "Fred") or (Student::surname neq "George")
        assertEquals("Student.age != ? AND Student.firstName != ? OR Student.surname != ?", condition.sql)
        assertEquals(listOf(12, "Fred", "George"), condition.values)
    }

    @Test
    fun lt() {
        val condition = (Student::age lt 12) and (Student::firstName lt "Fred") or (Student::surname lt "George")
        assertEquals("Student.age < ? AND Student.firstName < ? OR Student.surname < ?", condition.sql)
        assertEquals(listOf(12, "Fred", "George"), condition.values)
    }

    @Test
    fun lte() {
        val condition = (Student::age lte 12) and (Student::firstName lte "Fred") or (Student::surname lte "George")
        assertEquals("Student.age <= ? AND Student.firstName <= ? OR Student.surname <= ?", condition.sql)
        assertEquals(listOf(12, "Fred", "George"), condition.values)
    }

    @Test
    fun gt() {
        val condition = (Student::age gt 12) and (Student::firstName gt "Fred") or (Student::surname gt "George")
        assertEquals("Student.age > ? AND Student.firstName > ? OR Student.surname > ?", condition.sql)
        assertEquals(listOf(12, "Fred", "George"), condition.values)
    }

    @Test
    fun gte() {
        val condition = (Student::age gte 12) and (Student::firstName gte "Fred") or (Student::surname gte "George")
        assertEquals("Student.age >= ? AND Student.firstName >= ? OR Student.surname >= ?", condition.sql)
        assertEquals(listOf(12, "Fred", "George"), condition.values)
    }

    @Test
    fun between() {
        val condition = (Student::age between Pair(12, 14)) and (Student::firstName between Pair("Fred", "Freddy")) or (Student::surname between Pair("George", "Georgie"))
        assertEquals("Student.age BETWEEN ? AND ? AND Student.firstName BETWEEN ? AND ? OR Student.surname BETWEEN ? AND ?", condition.sql)
        assertEquals(listOf(12, 14, "Fred", "Freddy", "George", "Georgie"), condition.values)
    }

    @Test
    fun isNull() {
        val condition = Student::age.isNull() and Student::maidenName.isNull()
        assertEquals("Student.age IS NULL AND Student.maidenName IS NULL", condition.sql)
        assertEquals(listOf(), condition.values)
    }

    @Test
    fun notNull() {
        val condition = Student::age.notNull() and Student::maidenName.notNull()
        assertEquals("Student.age IS NOT NULL AND Student.maidenName IS NOT NULL", condition.sql)
        assertEquals(listOf(), condition.values)
    }

    @Test
    fun brackets() {
        val condition = (Student::age eq 12) and b((Student::firstName eq "Fred") or (Student::surname eq "George"))
        assertEquals("Student.age = ? AND (Student.firstName = ? OR Student.surname = ?)", condition.sql)
        assertEquals(listOf(12, "Fred", "George"), condition.values)
    }

    @Test
    fun and() {
        val condition = (Student::age eq 12) and (Student::maidenName eq "Fred")
        assertEquals("Student.age = ? AND Student.maidenName = ?", condition.sql)
        assertEquals(listOf(12, "Fred"), condition.values)
    }

    @Test
    fun or() {
        val condition = (Student::age eq 12) or (Student::firstName eq "Fred")
        assertEquals("Student.age = ? OR Student.firstName = ?", condition.sql)
        assertEquals(listOf(12, "Fred"), condition.values)
    }
}