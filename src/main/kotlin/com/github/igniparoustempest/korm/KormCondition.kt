package com.github.igniparoustempest.korm

/**
 * Defines conditions for finding, updating and deleting.
 * Can be composed from conditions in the conditions package.
 * @see com.github.igniparoustempest.korm.conditions
 */
class KormCondition(val sql: String, val values: List<Any>) {
}