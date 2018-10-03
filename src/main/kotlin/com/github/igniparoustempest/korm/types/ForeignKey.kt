package com.github.igniparoustempest.korm.types

import com.github.igniparoustempest.korm.OrmHelper.Companion.tableName
import kotlin.reflect.KProperty1

class ForeignKey<T: Any>(private val foreignColumn: String?, private val foreignTable: String?, val value: T) {
    constructor(property: KProperty1<*,PrimaryKey<T>>, primaryKey: PrimaryKey<T>) : this(property.name, tableName(property), primaryKey.value)
    internal val foreignColumnName: String
        get() = foreignColumn ?: throw Exception("This should not be accessed after a create table is called")
    internal val foreignTableName: String
        get() = foreignTable ?: throw Exception("This should not be accessed after a create table is called")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ForeignKey<*>

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "FK($value)"
    }
}