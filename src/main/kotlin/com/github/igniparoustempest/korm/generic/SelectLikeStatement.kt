package com.github.igniparoustempest.korm.generic

/**
 * Any SELECT statement that could be executed as a query.
 * E.g.: A select, a select with a join, a select with a group by, etc.
 */
open class SelectLikeStatement(sql: String, values: List<Any>): ValidSql(sql, values)