package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.testingtables.Discipline
import com.github.igniparoustempest.korm.testingtables.Student
import com.github.igniparoustempest.korm.testingtables.StudentAdvanced
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import kotlin.test.assertEquals

class KormTest {
    @Test
    fun createTable() {
        val conn = mockk<Connection>()
        val statement = mockk<Statement>(relaxed = true)
        val orm = spyk(Korm(conn = conn))
        every { conn.createStatement() } returns statement
        orm.createTable(randomStudent())

        verify { statement.execute("CREATE TABLE IF NOT EXISTS Student (age INTEGER NOT NULL, firstName TEXT NOT NULL, height REAL, maidenName TEXT, studentId INTEGER PRIMARY KEY NOT NULL, surname TEXT NOT NULL)") }

        // Try with move complex values
        val encoder: Encoder<Discipline> = { ps, parameterIndex, x -> ps.setString(parameterIndex, x.toString())}
        val decoder: Decoder<Discipline> = { rs, columnLabel -> Discipline.fromString(rs.getString(columnLabel))}
        orm.addCoder(encoder, decoder, "TEXT")
        orm.createTable(randomStudentAdvanced())

        verify { statement.execute("CREATE TABLE IF NOT EXISTS StudentAdvanced (age INTEGER NOT NULL, discipline TEXT NOT NULL, firstName TEXT NOT NULL, height REAL, isCurrent INTEGER NOT NULL, isFailing INTEGER NOT NULL, maidenName TEXT, studentId INTEGER PRIMARY KEY NOT NULL, surname TEXT NOT NULL)") }
    }

    @Test
    fun delete() {
        val conn = mockk<Connection>()
        val statement = mockk<PreparedStatement>(relaxed = true)
        val orm = spyk(Korm(conn = conn))
        every { conn.prepareStatement(any()) } returns statement
        orm.delete(Student::class, (Student::studentId eq 2) and (Student::age eq 12))

        verify { conn.prepareStatement("DELETE FROM Student WHERE studentId = ? AND age = ?") }
    }

    @Test
    fun drop() {
        val conn = mockk<Connection>()
        val statement = mockk<Statement>(relaxed = true)
        val orm = spyk(Korm(conn = conn))
        every { conn.createStatement() } returns statement
        orm.drop(Student::class)

        verify { statement.executeUpdate("DROP TABLE IF EXISTS Student") }
    }

    @Test
    fun find() {
        val conn = mockk<Connection>()
        val statement = mockk<PreparedStatement>(relaxed = true)
        val orm = spyk(Korm(conn = conn))
        every { conn.prepareStatement(any()) } returns statement
        orm.find(Student::class)

        verify { conn.prepareStatement("SELECT * FROM Student") }

        val conditions = (Student::firstName eq "Donald") and (Student::age lte 22)
        orm.find(Student::class, conditions)

        verify { conn.prepareStatement("SELECT * FROM Student WHERE firstName = ? AND age <= ?") }
    }

    @Test
    fun insert() {
        val student = randomStudent()
        val conn = mockk<Connection>()
        val statement = mockk<PreparedStatement>(relaxed = true)
        val orm = spyk(Korm(conn = conn))
        every { conn.prepareStatement(any()) } returns statement
        orm.insert(student)

        verify { conn.prepareStatement("INSERT INTO Student(age,firstName,height,maidenName,surname) VALUES(?,?,?,?,?)") }
    }

    @Test
    fun update() {
        val conn = mockk<Connection>()
        val statement = mockk<PreparedStatement>(relaxed = true)
        val orm = spyk(Korm(conn = conn))
        every { conn.prepareStatement(any()) } returns statement
        val condition = (Student::age gt 99) and (Student::maidenName eq "Donald") and Student::height.isNull()
        val updater = (Student::age set 100) and (Student::maidenName set null) onCondition condition
        orm.update(Student::class, updater)

        verify { conn.prepareStatement("UPDATE Student SET age = ?, maidenName = NULL WHERE age > ? AND maidenName = ? AND height IS NULL") }
    }

    @Test
    fun integrationDelete() {
        var students = (1..10).map { randomStudent() }

        val orm = Korm()

        // Save data
        students = students.map { orm.insert(it) }

        // Drop table
        val beforeDelete = orm.find(Student::class)
        orm.delete(Student::class, Student::studentId eq 1)
        val afterDelete = orm.find(Student::class)

        // Run tests
        assertEquals(10, beforeDelete.size)
        assertEquals(9, afterDelete.size, "Should delete row safely")
        assertEquals(students.filter { it.studentId.value != 1 }, orm.find(Student::class), "Should remove row.")
        orm.close()
    }

    @Test
    fun integrationDrop() {
        val students = (1..10).map { randomStudent() }

        val orm = Korm()

        // Save data
        for (student in students)
            orm.insert(student)

        // Drop table
        val beforeDrop = orm.find(Student::class)
        orm.drop(Student::class)
        val afterDrop = orm.find(Student::class)

        // Run tests
        assertEquals(10, beforeDrop.size)
        assertEquals(0, afterDrop.size, "Should drop table safely")
        orm.close()
    }

    @Test
    fun integrationFind() {
        var students = (1..12).map { randomStudent() }
        students = students.mapIndexed { i, student -> if (i % 3 == 0) student.copy(maidenName = "Donald", age = 100, height = null) else student }

        val orm = Korm()

        // Save data
        for (student in students)
            orm.insert(student)

        // Retrieve data
        val condition = (Student::age gt 99) and (Student::maidenName eq "Donald") and Student::height.isNull()
        val retrievedStudents = orm.find(Student::class, condition)

        // Run tests
        assertEquals(4, retrievedStudents.size, "Should work with complex conditions")
        orm.close()
    }

    @Test
    fun integrationInsert() {
        var students = (1..10).map { randomStudentAdvanced() }

        val orm = Korm()

        // Add the encoder/decoder for the Discipline type
        val encoder: Encoder<Discipline> = { ps, parameterIndex, x -> ps.setString(parameterIndex, x.toString())}
        val decoder: Decoder<Discipline> = { rs, columnLabel -> Discipline.fromString(rs.getString(columnLabel))}
        orm.addCoder(encoder, decoder, "TEXT")

        // Save data
        students = students.map { orm.insert(it) }

        // Retrieve data
        val retrievedStudents = orm.find(StudentAdvanced::class)

        // Run tests
        assertEquals(students, retrievedStudents, "Should reconstruct data class when retrieving.")
        assertEquals(listOf(), orm.find(Discipline::class), "Should return empty list for missing Table.")
        orm.close()
    }

    @Test
    fun integrationUpdate() {
        var students = (1..12).map { randomStudent() }
        students = students.mapIndexed { i, student -> if (i % 3 == 0) student.copy(maidenName = "Donald", age = 100, height = null) else student }

        val orm = Korm()

        // Update table that doesn't exist
        var condition = (Student::age gt 99) and (Student::maidenName eq "Donald") and Student::height.isNull()
        val updater = (Student::age set 100) and (Student::maidenName set null) onCondition condition
        orm.update(Student::class, updater)

        // Save data
        for (student in students)
            orm.insert(student)

        // Update table that exists
        orm.update(Student::class, updater)

        condition = (Student::age eq 100) and (Student::maidenName eq null) and Student::height.isNull()
        val retrievedStudents = orm.find(Student::class, condition)

        // Run tests
        assertEquals(4, retrievedStudents.size, "Should update correctly")
        orm.close()
    }
}