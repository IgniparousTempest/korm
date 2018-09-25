package com.caughtknee.korm

import com.caughtknee.korm.types.PrimaryKey
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KormTest {
    @Test
    fun save() {
    }

    @Test
    fun integration() {
        val student1 = Student(
                firstName = "Courtney",
                middleName = "Richard",
                surname = "Pitcher",
                maidenName = null,
                discipline = Discipline('C', 6),
                height = null,
                age = 26
        )
        val student2 = Student(
                firstName = "Hendrik",
                middleName = null,
                surname = "Pienaar",
                maidenName = null,
                discipline = Discipline('E', 2),
                height = 2.1f,
                age = 21
        )

        val orm = Korm()

        // Add the encoder/decoder for the Discipline type
        val encoder: Encoder<Discipline> = { ps, parameterIndex, x: Discipline? -> ps.setString(parameterIndex, x.toString())}
        val decoder: Decoder<Discipline> = { rs, columnLabel -> Discipline.fromString(rs.getString(columnLabel))}
        orm.addCoder(encoder, decoder, "TEXT")

        // Save data
        orm.insert(student1)
        orm.insert(student2)

        // Retrieve data
        val retrievedStudent = orm.find(Student::class)

        // Run tests
        val test = Student::age
        test
        val students = listOf(student1.copy(studentId = PrimaryKey(1)), student2.copy(studentId = PrimaryKey(2)))
        assertEquals(students, retrievedStudent, "Should reconstruct data class when retrieving.")
        assertEquals(listOf(), orm.find(Discipline::class), "Should return empty list for missing Table.")
        orm.close()
    }
}