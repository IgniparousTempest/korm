package com.github.igniparoustempest.korm

import kotlin.reflect.KProperty1

class OrmHelper {
    companion object {
        /**
         * Gets fully qualified name of a column.
         * Eg age => Student.age
         */
        fun <T, R: Any> fullyQualifiedName(property: KProperty1<T, R?>): String {
            // TODO: Is there a better way to do this?
            val tableName = property.toString().split(":").first().split(".").dropLast(1).last()
            return tableName + "." + property.name
        }
    }
}