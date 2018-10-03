package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.testingtables.*
import com.github.igniparoustempest.korm.types.ForeignKey
import com.github.igniparoustempest.korm.types.PrimaryKey
import com.github.igniparoustempest.korm.types.PrimaryKeyAuto
import org.fluttercode.datafactory.impl.DataFactory
import java.util.Random

fun randomDepartment(): Department {
    val df = DataFactory()
    return Department(
            name = "Department of " + df.businessName
    )
}

fun randomStudent(): Student {
    val df = DataFactory()
    val rand = Random()
    return Student(
            firstName = df.firstName,
            surname = df.lastName,
            maidenName = listOf(df.lastName, null).shuffled().asSequence().take(1).first(),
            height = listOf(rand.nextFloat() + 1, null).shuffled().asSequence().take(1).first(),
            age = rand.nextInt(5) + 18
    )
}

fun randomStudentAdvanced(): StudentAdvanced {
    val df = DataFactory()
    val rand = Random()
    return StudentAdvanced(
            firstName = df.firstName,
            surname = df.lastName,
            maidenName = listOf(df.lastName, null).shuffled().asSequence().take(1).first(),
            discipline = Discipline(df.randomChar.toUpperCase(), rand.nextInt(5)),
            height = listOf(rand.nextFloat() + 1, null).shuffled().asSequence().take(1).first(),
            age = rand.nextInt(5) + 18
    )
}

fun randomStudentFK(departmentId: PrimaryKeyAuto): StudentFK {
    val df = DataFactory()
    return StudentFK(
            name = df.firstName,
            departmentId = ForeignKey(Department::departmentId, departmentId)
    )
}

fun randomMultiplePrimaries(key1: Int, key2: String): MultiplePrimaries {
    val df = DataFactory()
    return MultiplePrimaries(
            id1 = PrimaryKey(key1),
            id2 = PrimaryKey(key2),
            name = df.firstName
    )
}