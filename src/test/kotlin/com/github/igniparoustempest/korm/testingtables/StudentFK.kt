package com.github.igniparoustempest.korm.testingtables

import com.github.igniparoustempest.korm.types.ForeignKey
import com.github.igniparoustempest.korm.types.PrimaryKey

/**
 * This is a student with a foreign key.
 */
data class StudentFK(
        val studentId: PrimaryKey = PrimaryKey(),
        val name: String,
        val departmentId: ForeignKey
)