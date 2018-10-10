package com.github.igniparoustempest.korm.generic

import com.github.igniparoustempest.korm.conditions.eq
import com.github.igniparoustempest.korm.testingtables.Department
import com.github.igniparoustempest.korm.testingtables.Student
import com.github.igniparoustempest.korm.testingtables.StudentFK
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GenericTest {

    @Test
    fun testSelect() {
        var statement = select(Student::firstName, Student::age, Student::height) from Student::class
        assertEquals("SELECT `Student`.`firstName`, `Student`.`age`, `Student`.`height` FROM `Student`", statement.sql)
        assertEquals(emptyList(), statement.values)

        statement = selectAll() from Student::class
        assertEquals("SELECT * FROM `Student`", statement.sql)
        assertEquals(emptyList(), statement.values)
    }

    @Test
    fun testInnerJoin() {
        val statement = selectAll() from StudentFK::class innerJoin Department::class on (StudentFK::departmentId eq Department::departmentId)
        assertEquals("SELECT * FROM `StudentFK` INNER JOIN `Department` ON `StudentFK`.`departmentId` = `Department`.`departmentId`", statement.sql)
        assertEquals(emptyList(), statement.values)
    }

}