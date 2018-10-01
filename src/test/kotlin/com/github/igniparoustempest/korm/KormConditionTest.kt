package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.conditions.*
import com.github.igniparoustempest.korm.testingtables.Dog
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
    fun like() {
        val condition = (Student::firstName like "Fred") or (Student::surname like "George")
        assertEquals("Student.firstName LIKE ? OR Student.surname LIKE ?", condition.sql)
        assertEquals(listOf("Fred", "George"), condition.values)
    }

    @Test
    fun glob() {
        val condition = (Student::firstName glob "Fred") or (Student::surname glob "George")
        assertEquals("Student.firstName GLOB ? OR Student.surname GLOB ?", condition.sql)
        assertEquals(listOf("Fred", "George"), condition.values)
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

    @Test
    fun integration() {
        val orm = Korm()
        val dogs = listOf(
                Dog(
                        name = "Jasper",
                        yearOfBirsth = 1988,  // This is wrong
                        breed = "Golden Retriever"
                ),
                Dog(
                        name = "Honey",
                        yearOfBirsth = 1988,  // This is wrong
                        breed = "Golden Retriever"
                ),
                Dog(
                        name = "Freckle",
                        yearOfBirsth = 1989,  // This is wrong
                        breed = "Jack-Russel"
                ),
                Dog(
                        name = "Ditto",
                        yearOfBirsth = 2000,  // This is wrong
                        breed = "Jack-Russel"
                ),
                Dog(
                        name = "Baggins",
                        yearOfBirsth = 2003,
                        breed = "Jack-Russel"
                ),
                Dog(
                        name = "Shenzi",
                        yearOfBirsth = 2004,
                        breed = "Golden Retriever"
                ),
                Dog(
                        name = "Ntombi",
                        yearOfBirsth = 2011,
                        breed = "Jack-Russel"
                )
        ).map { orm.insert(it) }

    assertEquals(listOf(dogs[0]), orm.find(Dog::class, Dog::name eq "Jasper"), "=")
    assertEquals(dogs.drop(2), orm.find(Dog::class, Dog::yearOfBirsth neq 1988), "!=")
    assertEquals(dogs.dropLast(4), orm.find(Dog::class, Dog::yearOfBirsth lt 2000), "<")
    assertEquals(dogs.dropLast(3), orm.find(Dog::class, Dog::yearOfBirsth lte 2000), "<=")
    assertEquals(dogs.drop(4), orm.find(Dog::class, Dog::yearOfBirsth gt 2000), ">")
    assertEquals(dogs.drop(3), orm.find(Dog::class, Dog::yearOfBirsth gte 2000), ">=")
    assertEquals(listOf(dogs[4]), orm.find(Dog::class, Dog::name like "Bag_i%"), "LIKE")
    assertEquals(listOf(dogs[4]), orm.find(Dog::class, Dog::name glob "Bag?i*"), "GLOB")
    assertEquals(dogs.drop(3).dropLast(1), orm.find(Dog::class, Dog::yearOfBirsth between Pair(2000, 2004)), "BETWEEN")
    assertEquals(dogs, orm.find(Dog::class, Dog::yearOfBirsth.notNull()), "NOT NULL")
    assertEquals(emptyList(), orm.find(Dog::class, Dog::name.isNull()), "IS NULL")
    assertEquals(listOf(dogs[4]), orm.find(Dog::class, Dog::name glob "B*" and (Dog::breed eq "Jack-Russel")), "AND")
    assertEquals(listOf(dogs[4], dogs[6]), orm.find(Dog::class, Dog::name like "Nt%" or (Dog::name eq "Baggins")), "OR")
    assertEquals(listOf(dogs[4]), orm.find(Dog::class, Dog::name like "B%" and b(Dog::yearOfBirsth eq 2003 or (Dog::yearOfBirsth eq 2004))), "()")
    }
}