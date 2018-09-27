package com.github.igniparoustempest.korm.testingtables

import com.github.igniparoustempest.korm.types.PrimaryKey

data class Student(
        val studentId: PrimaryKey = PrimaryKey(),
        val firstName: String,
        val surname: String,
        val maidenName: String?,
        val height: Float?,
        val age: Int
)
