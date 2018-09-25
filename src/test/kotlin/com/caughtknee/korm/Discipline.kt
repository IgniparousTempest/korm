package com.caughtknee.korm

data class Discipline(val department: Char, val year: Int) {
    override fun toString(): String {
        return "$department,$year"
    }

    companion object {
        fun fromString(str: String): Discipline {
            val params = str.split(",")
            return Discipline(params[0][0], params[1].toInt())
        }
    }
}