package com.github.igniparoustempest.korm.testingtables

import com.github.igniparoustempest.korm.types.PrimaryKeyAuto

data class Dog(
        val dogId: PrimaryKeyAuto = PrimaryKeyAuto(),
        val name: String,
        val yearOfBirth: Int,
        val breed: String
)