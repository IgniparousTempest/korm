package com.github.igniparoustempest.korm

import kotlin.reflect.KProperty1

class OrmHelper {
    companion object {
        /**
         * Gets the table name from a property.
         */
        fun <T, R: Any> tableName(property: KProperty1<T, R?>): String {
            // TODO: Is there a better way to do this?
            return property.toString().split(":").first().split(".").dropLast(1).last()
        }

        /**
         * Gets fully qualified name of a column.
         * Eg age => Student.age
         */
        fun <T, R: Any> fullyQualifiedName(property: KProperty1<T, R?>): String {
            val tableName = tableName(property)
            return tableName + "." + property.name
        }
    }
}