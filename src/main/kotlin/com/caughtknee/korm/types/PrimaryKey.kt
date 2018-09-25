package com.caughtknee.korm.types

class PrimaryKey(val value: Int?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrimaryKey

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value ?: 0
    }

    override fun toString(): String {
        return "PK($value)"
    }
}