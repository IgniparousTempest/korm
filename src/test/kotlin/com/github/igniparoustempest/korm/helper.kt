package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.testingtables.Discipline
import com.github.igniparoustempest.korm.testingtables.Student
import com.github.igniparoustempest.korm.testingtables.StudentAdvanced
import org.fluttercode.datafactory.impl.DataFactory
import java.util.Random

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