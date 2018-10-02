package com.github.igniparoustempest.korm.testingtables

import com.github.igniparoustempest.korm.types.PrimaryKeyAuto

data class Department(
        val departmentId: PrimaryKeyAuto = PrimaryKeyAuto(),
        val name: String
)