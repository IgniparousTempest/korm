package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.testingtables.Department
import com.github.igniparoustempest.korm.testingtables.Discipline
import com.github.igniparoustempest.korm.testingtables.Student
import com.github.igniparoustempest.korm.testingtables.StudentAdvanced
import com.github.igniparoustempest.korm.testingtables.StudentFK
import com.github.igniparoustempest.korm.types.ForeignKey
import com.github.igniparoustempest.korm.types.PrimaryKey
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

fun randomStudentFK(departmentId: PrimaryKey): StudentFK {
    val df = DataFactory()
    return StudentFK(
            name = df.firstName,
            departmentId = ForeignKey(Department::departmentId, departmentId.value!!)
    )
}