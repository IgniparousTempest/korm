package com.github.igniparoustempest.korm

class KormUpdater(val sql: String, val values: List<Any>, val condition: KormCondition? = null) {
}