package com.github.igniparoustempest.korm.generic

import com.github.igniparoustempest.korm.KormCondition
import com.github.igniparoustempest.korm.helper.escapedFullyQualifiedName
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

fun <T, R: Any> select(firstColumn: KProperty1<T, R?>, vararg columns: KProperty1<T, R?>): SelectLeft {
    val columnNames = (listOf(firstColumn) + columns).joinToString(", ") { escapedFullyQualifiedName(it) }
    val sql = "SELECT $columnNames"
    return SelectLeft(sql)
}

fun selectAll(): SelectLeft {
    return SelectLeft("SELECT *")
}

infix fun <T: Any> SelectLeft.from(clazz: KClass<T>): SelectStatement {
    val tableName = clazz.simpleName
    val sql = " FROM `$tableName`"
    return SelectStatement(this.sql + sql)
}

infix fun <T: Any> SelectStatement.innerJoin(clazz: KClass<T>): JoinLeft {
    val tableName = clazz.simpleName
    val sql = " INNER JOIN `$tableName`"
    return JoinLeft(this.sql + sql)
}

infix fun JoinLeft.on(expression: KormCondition): SelectWithJoinClause {
    val expressionSQL = expression.sql
    val sql = " ON $expressionSQL"
    return SelectWithJoinClause(this.sql + sql, expression.values)
}

infix fun SelectStatement.where(expression: KormCondition): SelectWithWhereClause {
    val expressionSQL = expression.sql
    val sql = " WHERE $expressionSQL"
    return SelectWithWhereClause(this.sql + sql, expression.values)
}