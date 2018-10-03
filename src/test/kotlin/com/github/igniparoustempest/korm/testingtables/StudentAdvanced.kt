package com.github.igniparoustempest.korm.testingtables

import com.github.igniparoustempest.korm.types.PrimaryKeyAuto

data class StudentAdvanced(
        val studentId: PrimaryKeyAuto = PrimaryKeyAuto(),
        val firstName: String,
        val surname: String,
        val maidenName: String?,
        val discipline: Discipline,
        val height: Float?,
        val age: Int,
        val isCurrent: Boolean = true,
        private val isFailing: Boolean = false
) {
    val old = age > 60
    var weight: Float = 60f
}
