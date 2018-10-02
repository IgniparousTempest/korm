package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.types.ForeignKey
import com.github.igniparoustempest.korm.types.PrimaryKey
import com.github.igniparoustempest.korm.types.PrimaryKeyAuto
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

internal class OrmHelper {
    companion object {
        /**
         * Gets the table name from a property.
         */
        fun <T, R: Any> tableName(property: KProperty1<T, R?>): String {
            // TODO: Is there a better way to do this?
            val fullyQualifiedClass = property.toString().split(":").first()
            val className = fullyQualifiedClass.split(".").dropLast(1).last().split( " ").last()
            if (!className.contains("$"))
                return className
            val unescapedClassName = className.drop(1).dropLast(1)
            return unescapedClassName.split("$").last()
        }

        /**
         * Gets fully qualified name of a column.
         * Eg age => Student.age
         */
        fun <T, R: Any> fullyQualifiedName(property: KProperty1<T, R?>): String {
            val tableName = tableName(property)
            return tableName + "." + property.name
        }

        /**
         * Reads a property from a object based on the name of the property.
         * Can read private properties too.
         */
        fun <T: Any> readProperty(instance: T, propertyName: String): T? {
            val clazz = instance.javaClass.kotlin
            @Suppress("UNCHECKED_CAST")
            clazz.declaredMemberProperties.first { it.name == propertyName }.let {
                it.isAccessible = true
                return it.get(instance) as T?
            }
        }

        /**
         * determines if a column is a primary key regardless of type.
         */
        fun isPrimaryKey(column: KProperty1<out String, Any?>): Boolean {
            @Suppress("UNCHECKED_CAST")
            return column.returnType.withNullability(false).isSubtypeOf(PrimaryKey::class.starProjectedType)
        }

        /**
         * Gets all columns that are primary keys regardless of type.
         */
        fun primaryKeyColumns(columns: List<KProperty1<out String, Any?>>): List<KProperty1<out String, PrimaryKey<*>?>> {
            @Suppress("UNCHECKED_CAST")
            return columns.filter { isPrimaryKey(it) } as List<KProperty1<out String, PrimaryKey<*>?>>
        }

        /**
         * Gets all columns that are foreign keys regardless of type.
         */
        fun foreignKeyColumns(columns: List<KProperty1<out String, Any?>>): List<KProperty1<out String, ForeignKey<*>?>> {
            @Suppress("UNCHECKED_CAST")
            return columns.filter { it.returnType.withNullability(false).isSubtypeOf(ForeignKey::class.starProjectedType) } as List<KProperty1<out String, ForeignKey<*>?>>
        }

        /**
         * Determines if the automatic primary key has yet to have it's value assigned.
         */
        fun isUnsetPrimaryKeyAuto(row: Any, column: KProperty1<out String, Any?>): Boolean {
            return column.returnType == PrimaryKeyAuto::class.createType() && (readProperty(row, column.name) as PrimaryKeyAuto?)?.isSet == false
        }

        /**
         * Determines if the column is an auto primary key.
         */
        fun isPrimaryKeyAuto(column: KProperty1<out String, Any?>): Boolean {
            return column.returnType == PrimaryKeyAuto::class.createType()
        }

        /**
         * Generates a primary key for the type of clazz.
         */
        fun <T: Any> primaryKeyType(clazz: KClass<T>): KType {
            return PrimaryKey::class.createType(listOf(KTypeProjection(KVariance.INVARIANT, clazz.createType())))
        }

        /**
         * Generates a primary key for the type of clazz.
         */
        fun <T: Any> foreignKeyType(clazz: KClass<T>): KType {
            return ForeignKey::class.createType(listOf(KTypeProjection(KVariance.INVARIANT, clazz.createType())))
        }
    }
}