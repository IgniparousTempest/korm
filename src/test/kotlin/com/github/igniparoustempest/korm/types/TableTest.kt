package com.github.igniparoustempest.korm.types

import com.github.igniparoustempest.korm.Korm
import com.github.igniparoustempest.korm.randomStudent
import com.github.igniparoustempest.korm.testingtables.Simple
import com.github.igniparoustempest.korm.testingtables.Student
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TableTest {
    @Test
    fun testConstructor() {
        val rs = mock(ResultSet::class.java)
        val rsmd = mock(ResultSetMetaData::class.java)
        Mockito.`when`(rs.metaData).thenReturn(rsmd)
        Mockito.`when`(rs.next()).thenReturn(true).thenReturn(false)
        Mockito.`when`(rsmd.columnCount).thenReturn(1)
        Mockito.`when`(rsmd.getColumnName(1)).thenReturn("")
        Mockito.`when`(rsmd.getColumnType(1)).thenReturn(Int.MIN_VALUE)

        assertFailsWith<Exception> {
            Table(rs)
        }
    }

    @Test
    fun testToString() {
        val orm = Korm()
        (1..10).mapIndexed { i, _ -> orm.insert(Simple(PrimaryKeyAuto(i), "a".repeat(i))) }
        assertEquals("", Table().toString())

        val table = orm.rawSqlQuery("SELECT * FROM Simple")
        assertEquals("" +
                "|data    |id      |\n" +
                "|--------|--------|\n" +
                "|        |0       |\n" +
                "|a       |1       |\n" +
                "|aa      |2       |\n" +
                "|aaa     |3       |\n" +
                "|aaaa    |4       |\n" +
                "|aaaaa   |5       |\n" +
                "|aaaaaa  |6       |\n" +
                "|aaaaaaa |7       |\n" +
                "|aaaaaaaa|8       |\n" +
                "|aaaaa...|9       |", table.toString())
        assertEquals("" +
                "|data|id  |\n" +
                "|----|----|\n" +
                "|    |0   |\n" +
                "|a   |1   |\n" +
                "|aa  |2   |\n" +
                "|aaa |3   |\n" +
                "|aaaa|4   |\n" +
                "|a...|5   |\n" +
                "|a...|6   |\n" +
                "|a...|7   |\n" +
                "|a...|8   |\n" +
                "|a...|9   |", table.toString(4))
    }

    @Test
    fun testIntegration() {
        val orm = Korm()
        val students = (1..10).map { orm.insert(randomStudent()) }

        val retrievedStudents = orm.rawSqlQuery("SELECT * FROM Student")

        fun mapper(it: Row<Any>) = Student(
                PrimaryKeyAuto(it[Student::studentId.name] as Int?),
                it[Student::firstName]!!,
                it[Student::surname]!!,
                it[Student::maidenName],
                it[Student::height],
                it[Student::age]!!
        )

        val processedStudents = retrievedStudents.rows.map { mapper(it) }

        assertEquals(students, processedStudents)
        assertEquals(students[0], mapper(retrievedStudents[0]))
        assertEquals("", Table().toString())
    }
}