package com.caughtknee.korm

import com.caughtknee.korm.types.PrimaryKey

data class Student(
        val studentId: PrimaryKey = PrimaryKey(null),
        val firstName: String,
        val middleName: String?,
        val surname: String,
        val maidenName: String?,
        val discipline: Discipline,
        val height: Float?,
        val age: Int
)
