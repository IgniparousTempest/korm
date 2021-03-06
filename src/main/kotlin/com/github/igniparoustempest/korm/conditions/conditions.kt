package com.github.igniparoustempest.korm.conditions

import com.github.igniparoustempest.korm.KormCondition
import com.github.igniparoustempest.korm.helper.fullyQualifiedName
import kotlin.reflect.KProperty1

infix fun <T, R: Any> KProperty1<T, R?>.eq(value: R?): KormCondition {
    val columnName = fullyQualifiedName(this)
    return if (value == null)
        isNull()
    else
        KormCondition("$columnName = ?", listOf(value))
}

infix fun <T, R: Any> KProperty1<T, R?>.neq(value: R?): KormCondition {
    val columnName = fullyQualifiedName(this)
    return if (value == null)
        notNull()
    else
        KormCondition("$columnName != ?", listOf(value))
}

infix fun <T, R: Any> KProperty1<T, R?>.lt(value: R): KormCondition {
    val columnName = fullyQualifiedName(this)
    return KormCondition("$columnName < ?", listOf(value))
}

infix fun <T, R: Any> KProperty1<T, R?>.lte(value: R): KormCondition {
    val columnName = fullyQualifiedName(this)
    return KormCondition("$columnName <= ?", listOf(value))
}

infix fun <T, R: Any> KProperty1<T, R?>.gt(value: R): KormCondition {
    val columnName = fullyQualifiedName(this)
    return KormCondition("$columnName > ?", listOf(value))
}

infix fun <T, R: Any> KProperty1<T, R?>.gte(value: R): KormCondition {
    val columnName = fullyQualifiedName(this)
    return KormCondition("$columnName >= ?", listOf(value))
}

infix fun <T> KProperty1<T, String?>.like(value: String): KormCondition {
    val columnName = fullyQualifiedName(this)
    return KormCondition("$columnName LIKE ?", listOf(value))
}

infix fun <T> KProperty1<T, String?>.glob(value: String): KormCondition {
    val columnName = fullyQualifiedName(this)
    return KormCondition("$columnName GLOB ?", listOf(value))
}

infix fun <T, R: Any> KProperty1<T, R?>.between(values: Pair<R, R>): KormCondition {
    val columnName = fullyQualifiedName(this)
    return KormCondition("$columnName BETWEEN ? AND ?", listOf(values.first, values.second))
}

fun <T, R: Any> KProperty1<T, R?>.isNull(): KormCondition {
    val columnName = fullyQualifiedName(this)
    return KormCondition("$columnName IS NULL", emptyList())
}

fun <T, R: Any> KProperty1<T, R?>.notNull(): KormCondition {
    val columnName = fullyQualifiedName(this)
    return KormCondition("$columnName IS NOT NULL", emptyList())
}

/**
 * Wraps a KormCondition object in brackets.
 */
fun b(condition: KormCondition): KormCondition {
    return KormCondition("(${condition.sql})", condition.values)
}

infix fun KormCondition.and(other: KormCondition): KormCondition {
    return KormCondition(this.sql + " AND " + other.sql, this.values + other.values)
}

infix fun KormCondition.or(other: KormCondition): KormCondition {
    return KormCondition(this.sql + " OR " + other.sql, this.values + other.values)
}