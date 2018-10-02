package com.github.igniparoustempest.korm.testingtables

import com.github.igniparoustempest.korm.types.ForeignKey

data class MultipleForeigns(
        val data: String,
        val id1: ForeignKey<Int>,
        val id2: ForeignKey<String>
)