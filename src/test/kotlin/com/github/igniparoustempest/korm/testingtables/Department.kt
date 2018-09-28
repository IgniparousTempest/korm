package com.github.igniparoustempest.korm.testingtables

import com.github.igniparoustempest.korm.types.PrimaryKey

data class Department(
        val departmentId: PrimaryKey = PrimaryKey(),
        val name: String
)