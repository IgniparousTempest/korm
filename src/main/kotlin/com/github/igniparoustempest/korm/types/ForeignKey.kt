package com.github.igniparoustempest.korm.types

import com.github.igniparoustempest.korm.OrmHelper.Companion.fullyQualifiedName
import kotlin.reflect.KProperty1

class ForeignKey(val foreignName: String, val foreignFullyQualifiedName: String, val value: Int) {
    constructor(property: KProperty1<*,*>, value: Int) : this(property.name, fullyQualifiedName(property), value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return when (other?.javaClass) {
            javaClass -> (other as ForeignKey).value == value
            Int::class.java -> value == other
            else -> false
        }
    }

    override fun hashCode(): Int {
        return value
    }

    override fun toString(): String {
        return "FK($value)"
    }
}