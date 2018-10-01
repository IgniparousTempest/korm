package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.conditions.*
import com.github.igniparoustempest.korm.exceptions.DatabaseException
import com.github.igniparoustempest.korm.testingtables.Department
import com.github.igniparoustempest.korm.testingtables.Discipline
import com.github.igniparoustempest.korm.testingtables.Student
import com.github.igniparoustempest.korm.testingtables.StudentAdvanced
import com.github.igniparoustempest.korm.testingtables.StudentFK
import com.github.igniparoustempest.korm.types.PrimaryKey
import com.github.igniparoustempest.korm.updates.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.nio.file.Paths
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KormTest {
    @Test
    fun createTable() {
        val conn = mock(Connection::class.java)
        val statement = mock(Statement::class.java)
        val orm = Korm(conn = conn)
        Mockito.`when`(conn.createStatement()).thenReturn(statement)
        orm.createTable(randomStudent())

        verify(statement).execute("CREATE TABLE IF NOT EXISTS Student (age INTEGER NOT NULL, firstName TEXT NOT NULL, height REAL, maidenName TEXT, studentId INTEGER PRIMARY KEY NOT NULL, surname TEXT NOT NULL)")
    }

    @Test
    fun createTableCustomEncoder() {
        val conn = mock(Connection::class.java)
        val statement = mock(PreparedStatement::class.java)
        val orm = Korm(conn = conn)
        Mockito.`when`(conn.createStatement()).thenReturn(statement)

        // Custom encoder/decoder
        val encoder: Encoder<Discipline> = { ps, parameterIndex, x -> ps.setString(parameterIndex, x.toString())}
        val decoder: Decoder<Discipline> = { rs, columnLabel -> Discipline.fromString(rs.getString(columnLabel))}
        orm.addCoder(encoder, decoder, "TEXT")

        orm.createTable(randomStudentAdvanced())

        verify(statement).execute("CREATE TABLE IF NOT EXISTS StudentAdvanced (age INTEGER NOT NULL, discipline TEXT NOT NULL, firstName TEXT NOT NULL, height REAL, isCurrent INTEGER NOT NULL, isFailing INTEGER NOT NULL, maidenName TEXT, studentId INTEGER PRIMARY KEY NOT NULL, surname TEXT NOT NULL)")
    }

    @Test
    fun createTableForeignKey() {
        val conn = mock(Connection::class.java)
        val statement = mock(PreparedStatement::class.java)
        val orm = Korm(conn = conn)
        Mockito.`when`(conn.createStatement()).thenReturn(statement)
        orm.createTable(randomDepartment())
        orm.createTable(randomStudentFK(PrimaryKey(9001)))

        verify(statement).execute("CREATE TABLE IF NOT EXISTS Department (departmentId INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL)")
        verify(statement).execute("CREATE TABLE IF NOT EXISTS StudentFK (departmentId INTEGER REFERENCES Department(departmentId) ON UPDATE CASCADE NOT NULL, name TEXT NOT NULL, studentId INTEGER PRIMARY KEY NOT NULL)")
    }

    @Test
    fun delete() {
        val conn = mock(Connection::class.java)
        val statement = mock(PreparedStatement::class.java)
        val orm = Korm(conn = conn)
        Mockito.`when`(conn.prepareStatement(any())).thenReturn(statement)
        orm.delete(Student::class, (Student::studentId eq 2) and (Student::age eq 12))

        verify(conn).prepareStatement("DELETE FROM Student WHERE Student.studentId = ? AND Student.age = ?")
    }

    @Test
    fun drop() {
        val conn = mock(Connection::class.java)
        val statement = mock(Statement::class.java)
        val orm = Korm(conn = conn)
        Mockito.`when`(conn.createStatement()).thenReturn(statement)
        orm.drop(Student::class)

        verify(statement).executeUpdate("DROP TABLE IF EXISTS Student")
    }

    @Test
    fun find() {
        val conn = mock(Connection::class.java)
        val statement = mock(PreparedStatement::class.java)
        val resultSet = mock(ResultSet::class.java)
        val orm = Korm(conn = conn)
        Mockito.`when`(conn.prepareStatement(any())).thenReturn(statement)
        Mockito.`when`(statement.executeQuery()).thenReturn(resultSet)
        orm.find(Student::class)

        verify(conn).prepareStatement("SELECT * FROM Student")

        val conditions = (Student::firstName eq "Donald") and (Student::age lte 22)
        orm.find(Student::class, conditions)

        verify(conn).prepareStatement("SELECT * FROM Student WHERE Student.firstName = ? AND Student.age <= ?")
    }

    @Test
    fun insert() {
        val student = randomStudent()
        val conn = mock(Connection::class.java)
        val statement = mock(PreparedStatement::class.java)
        val resultSet = mock(ResultSet::class.java)
        val orm = Korm(conn = conn)
        Mockito.`when`(conn.prepareStatement(any())).thenReturn(statement)
        Mockito.`when`(statement.generatedKeys).thenReturn(resultSet)
        Mockito.`when`(resultSet.getInt("last_insert_rowid()")).thenReturn(1)
        orm.insert(student)

        verify(conn).prepareStatement("INSERT INTO Student(age,firstName,height,maidenName,surname) VALUES(?,?,?,?,?)")
    }

    @Test
    fun update() {
        val conn = mock(Connection::class.java)
        val statement = mock(PreparedStatement::class.java)
        val orm = Korm(conn = conn)
        Mockito.`when`(conn.prepareStatement(any())).thenReturn(statement)
        val condition = (Student::age gt 99) and (Student::maidenName eq "Donald") and Student::height.isNull()
        val updater = (Student::age set 100) and (Student::maidenName set null) onCondition condition
        orm.update(Student::class, updater)

        verify(conn).prepareStatement("UPDATE Student SET age = ?, maidenName = NULL WHERE Student.age > ? AND Student.maidenName = ? AND Student.height IS NULL")
    }

    @Test
    fun integrationDatabaseException() {
        val orm = Korm()

        //Insert data
        (1..10).map { orm.insert(randomStudent()) }

        assertFailsWith<DatabaseException> {
            orm.find(Student::class, KormCondition("sdfgsd = ?", listOf(3)))
        }
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
    fun integrationFile() {
        val path = Paths.get(createTempDir().path, "test.db").toAbsolutePath().toString()
        val orm = Korm(path)

        // Save data
        val students = (1..10).map { orm.insert(randomStudent()) }

        // Retrieve data
        val retrievedStudents = orm.find(Student::class)

        // Run tests
        assertEquals(students, retrievedStudents, "Should work from a file database")
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
        assertEquals(emptyList(), orm.find(Student::class, Student::age eq -999), "Should not fail when there aren't rows")
        assertEquals(emptyList(), orm.find(StudentFK::class), "Should not fail on a table that doesn't exist")
        orm.close()
    }

    @Test
    fun integrationForeignKey() {
        val orm = Korm()

        // Save data
        val departments = (1..4).map { orm.insert(randomDepartment()) }
        val students = (1..12).mapIndexed { i, _ -> orm.insert(randomStudentFK(departments[i % departments.size].departmentId)) }

        // Retrieve data
        val retrievedStudents = orm.find(StudentFK::class)
        val studentsInDepartment0 = orm.find(StudentFK::class, StudentFK::departmentId eq departments[0].departmentId)

        // Run tests
        assertEquals(students.map { it.departmentId.value }, retrievedStudents.map { it.departmentId.value }, "Should preserve foreign key")
        assertEquals(3, studentsInDepartment0.size, "Should search on foreign key")
        // This might fail if SQLite is compiled without foreign key support
        assertFailsWith<DatabaseException>("An impassable error occurred while trying to delete rows.") {
            orm.delete(Department::class, Department::departmentId eq 2)
        }
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

    /**
     * Nesting a data class in a function changes its class name.
     */
    @Test
    fun integrationLocalClass() {
        data class Local(val localId: PrimaryKey = PrimaryKey(), val name: String)

        val orm = Korm()

        // Save data
        val rows = ('a'..'z').map { orm.insert(Local(name = it.toString())) }

        // Run tests
        assertEquals(rows, orm.find(Local::class), "Should retrieve all rows.")
        assertEquals(listOf(rows[0]), orm.find(Local::class, Local::name eq "a"), "Should retrieve with condition.")
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