package com.github.igniparoustempest.korm.types

import com.github.igniparoustempest.korm.OrmHelper.Companion.tableName
import kotlin.reflect.KProperty1

class ForeignKey(private val foreignColumn: String?, private val foreignTable: String?, val value: Int) {
    constructor(property: KProperty1<*,*>, primaryKey: PrimaryKey) : this(property.name, tableName(property), primaryKey.value!!)
    internal val foreignColumnName: String
        get() = foreignColumn ?: throw Exception("This should not be accessed after a create table is called")
    internal val foreignTableName: String
        get() = foreignTable ?: throw Exception("This should not be accessed after a create table is called")

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