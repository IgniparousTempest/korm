package com.github.igniparoustempest.korm.testingtables

import com.github.igniparoustempest.korm.types.PrimaryKeyAuto

data class Student(
        val studentId: PrimaryKeyAuto = PrimaryKeyAuto(),
        val firstName: String,
        val surname: String,
        val maidenName: String?,
        val height: Float?,
        val age: Int
)
