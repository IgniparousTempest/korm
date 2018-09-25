package com.github.igniparoustempest.korm.types

class PrimaryKey(val value: Int? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return when (other?.javaClass) {
            javaClass -> (other as PrimaryKey).value == value
            Int::class.java -> value == other
            else -> false
        }
    }

    override fun hashCode(): Int {
        return value ?: 0
    }

    override fun toString(): String {
        return "PK($value)"
    }
}