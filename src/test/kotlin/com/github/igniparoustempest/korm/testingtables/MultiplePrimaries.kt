package com.github.igniparoustempest.korm.testingtables

import com.github.igniparoustempest.korm.types.PrimaryKey

data class MultiplePrimaries(
        val id1: PrimaryKey<Int>,
        val id2: PrimaryKey<String>,
        val name: String
)