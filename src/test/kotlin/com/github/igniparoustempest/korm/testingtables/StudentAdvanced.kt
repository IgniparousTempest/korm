package com.github.igniparoustempest.korm.testingtables

import com.github.igniparoustempest.korm.types.PrimaryKey

data class StudentAdvanced(
        val studentId: PrimaryKey = PrimaryKey(),
        val firstName: String,
        val surname: String,
        val maidenName: String?,
        val discipline: Discipline,
        val height: Float?,
        val age: Int
)
