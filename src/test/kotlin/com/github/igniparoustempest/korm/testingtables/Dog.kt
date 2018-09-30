package com.github.igniparoustempest.korm.testingtables

import com.github.igniparoustempest.korm.types.PrimaryKey

data class Dog(
        val dogId: PrimaryKey = PrimaryKey(),
        val name: String,
        val yearOfBirsth: Int,
        val breed: String
)