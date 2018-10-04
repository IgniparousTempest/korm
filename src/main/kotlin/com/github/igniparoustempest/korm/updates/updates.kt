package com.github.igniparoustempest.korm.updates

import com.github.igniparoustempest.korm.KormCondition
import com.github.igniparoustempest.korm.KormUpdater
import kotlin.reflect.KProperty1

/**
 * Joins updates together.
 * If either of the updates contains a condition, then it tries to preserve the condition to the right of the and function.
 *
 * Examples:
 * 1) (Table::id eq 3) is preserved
 * Table::property set 2 onCondition (Table::id eq 3) and (Table::age set 21)
 * 2) (Table::id eq 4) is preserved
 * Table::property set 2 and (Table::age set 21) onCondition (Table::id eq 4)
 * 3) (Table::id eq 6) is preserved
 * Table::property set 2 onCondition (Table::id eq 5) and (Table::age set 21) onCondition (Table::id eq 6)
 */
infix fun KormUpdater.and(other: KormUpdater): KormUpdater {
    val cond = when {
        other.condition != null -> other.condition
        this.condition != null -> this.condition
        else -> null
    }
    return KormUpdater(this.sql + ", " + other.sql, this.values + other.values, cond)
}

infix fun <T: Any, R: Any> KProperty1<T, R?>.set(value: R?): KormUpdater {
    val columnName = this.name
    return if (value == null)
        KormUpdater("$columnName = NULL", emptyList())
    else
        KormUpdater("$columnName = ?", listOf(value))
}

infix fun KormUpdater.onCondition(condition: KormCondition): KormUpdater {
    return KormUpdater(this.sql, this.values, condition)
}