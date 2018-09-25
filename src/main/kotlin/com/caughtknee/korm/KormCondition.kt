package com.caughtknee.korm

import kotlin.reflect.KProperty1

class KormCondition() {
    class Builder() {
        val sql = StringBuilder()
        val values = mutableListOf<Any>()

        fun <T, R: Any> eq(key: KProperty1<T, R>, value: R) {
            val columnName = key.name
            sql.append("$columnName = ?")
            values.add()
        }

        fun build(): KormCondition {
            return KormCondition()
        }
    }
}