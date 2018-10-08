package com.github.igniparoustempest.korm.testingtables

import com.github.igniparoustempest.korm.types.PrimaryKeyAuto

data class Simple(val id: PrimaryKeyAuto = PrimaryKeyAuto(), val data: String)