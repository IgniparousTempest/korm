package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.generic.SqlBase

/**
 * Defines conditions for finding, updating and deleting.
 * Can be composed from conditions in the conditions package.
 * @see com.github.igniparoustempest.korm.conditions
 */
class KormCondition(sql: String, values: List<Any>): SqlBase(sql, values)