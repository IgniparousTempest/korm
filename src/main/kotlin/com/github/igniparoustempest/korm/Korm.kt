package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.exceptions.DatabaseException
import com.github.igniparoustempest.korm.exceptions.UnsupportedDataTypeException
import com.github.igniparoustempest.korm.types.ForeignKey
import com.github.igniparoustempest.korm.types.PrimaryKey
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.reflect


class Korm(private val conn: Connection) {
    constructor(): this(DriverManager.getConnection("jdbc:sqlite::memory:", KormConfig().toProperties()))
    constructor(path: String): this(DriverManager.getConnection("jdbc:sqlite:$path", KormConfig().toProperties()))
    private val coders = mutableMapOf<KType, Coder<Any>>()

    /**
     * Creates a table based on a row that will be inserted into it.
     * There is no need to call this as inserting calls this automatically.
     * @throws DatabaseException If the underlying database throws an error that can't be handled here.
     * @throws UnsupportedDataTypeException If the row class contains a member with an unsupported data type.
     */
    fun <T: Any> createTable(row: T) {
        val tableName = tableName(row)
        val columns = columnNames(row)
        val columnNames = columns.joinToString(", ") {
            var colType = it.name + " " +
                if (coders.contains(it.returnType.withNullability(false)))
                    coders[it.returnType.withNullability(false)]!!.dataType
                else
                    when (it.returnType.withNullability(false)) {
                        PrimaryKey::class.createType() -> "INTEGER PRIMARY KEY"
                        Boolean::class.createType() -> "INTEGER"
                        Float::class.createType() -> "REAL"
                        ForeignKey::class.createType() -> {
                            val fk = readProperty(row, it.name) as ForeignKey
                            "INTEGER REFERENCES ${fk.foreignTableName}(${fk.foreignColumnName}) ON UPDATE CASCADE"
                        }
                        Int::class.createType() -> "INTEGER"
                        String::class.createType() -> "TEXT"
                        else -> throw UnsupportedDataTypeException("Invalid data type ${it.returnType}.")
                    }
            if (!it.returnType.isMarkedNullable)
                colType += " NOT NULL"

            colType
        }
        val sql = "CREATE TABLE IF NOT EXISTS $tableName ($columnNames)"

        try {
            val stmt = conn.createStatement()
            stmt.execute(sql)
            stmt.close()
        } catch (e: SQLException) {
            throw DatabaseException("An impassable error occurred while trying to create table.", e, sql)
        }
    }

    /**
     * Deletes rows in the table that match the specified condition.
     * @throws DatabaseException If the underlying database throws an error that can't be handled here.
     */
    fun <T: Any> delete(clazz: KClass<T>, condition: KormCondition) {
        val tableName = clazz.simpleName
        val sql = "DELETE FROM $tableName WHERE ${condition.sql}"

        try {
            val pstmt = conn.prepareStatement(sql)
            condition.values.forEachIndexed { i, col ->
                applyEncoder(col::class.createType(nullable = false), pstmt, i + 1, col)
            }
            pstmt.executeUpdate()
            pstmt.close()
        } catch (e: SQLException) {
            // Creates the table if it doesn't already exist
            if (e.message == "[SQLITE_ERROR] SQL error or missing database (no such table: $tableName)") {
                return
            }
            else
                throw DatabaseException("An impassable error occurred while trying to delete rows.", e, sql)
        }
    }

    /**
     * Drops the specified table.
     * @throws DatabaseException If the underlying database throws an error that can't be handled here.
     */
    fun <T: Any> drop(clazz: KClass<T>) {
        val tableName = clazz.simpleName
        val sql = "DROP TABLE IF EXISTS $tableName"
        try {
            val stmt = conn.createStatement()
            stmt.executeUpdate(sql)
            stmt.close()
        } catch (e: SQLException) {
            throw DatabaseException("An impassable error occurred while trying to drop table.", e, sql)
        }
    }

    /**
     * Gets all rows from a table that match the specified condition.
     * If the table doesn't exist it returns an empty list.
     * @throws DatabaseException If the underlying database throws an error that can't be handled here.
     * @throws UnsupportedDataTypeException If the T class contains a member with an unsupported data type.
     */
    fun <T: Any> find(clazz: KClass<T>, condition: KormCondition? = null): List<T> {
        val tableString = clazz.simpleName
        var sql = "SELECT * FROM $tableString"
        if (condition != null)
            sql += " WHERE " + condition.sql

        val results = mutableListOf<T>()

        try {
            val pstmt = conn.prepareStatement(sql)
            condition?.values?.forEachIndexed { i, col ->
                applyEncoder(col::class.createType(nullable = false), pstmt, i + 1, col)
            }
            val rs = pstmt.executeQuery()

            val publicProperties = columnNames(clazz).map { it.name }
            val columns = clazz.primaryConstructor!!.parameters.filter { publicProperties.contains(it.name) }
            while (rs.next()) {
                val params = columns.map {
                    applyDecoder(it.type.withNullability(false), rs, it.name)
                }.toTypedArray()
                results.add(clazz.primaryConstructor!!.call(*params))
            }

            rs.close()
            pstmt.close()
        } catch (e: SQLException) {
            if (e.message == "[SQLITE_ERROR] SQL error or missing database (no such table: $tableString)")
                return emptyList()
            else
                throw DatabaseException("An impassable error occurred while trying to find rows.", e, sql)
        }

        return results
    }

    /**
     * Inserts a row into the table.
     * @throws DatabaseException If the underlying database throws an error that can't be handled here.
     * @throws UnsupportedDataTypeException If the row class contains a member with an unsupported data type.
     */
    fun <T: Any> insert(row: T): T {
        val tableName = tableName(row)
        val columns = columnNames(row).filter { it.returnType != PrimaryKey::class.createType() }
        val columnString = columns.joinToString(",") { it.name }
        val valuesString = columns.joinToString(",") { "?" }
        val sql = "INSERT INTO $tableName($columnString) VALUES($valuesString)"

        try {
            val pstmt = conn.prepareStatement(sql)
            columns.forEachIndexed { i, col ->
                applyEncoder(col.returnType.withNullability(false), pstmt, i + 1, readProperty(row, col.name))
            }
            pstmt.executeUpdate()
            val generatedKeys = pstmt.generatedKeys
            pstmt.close()

            // Create copy of row with the correct primary key
            generatedKeys.next()
            val key = PrimaryKey(generatedKeys.getInt("last_insert_rowid()"))
            val primaryKeyProperty = columnNames(row).first { it.returnType.withNullability(false) == PrimaryKey::class.createType() }
            val updates = mapOf(primaryKeyProperty.name to key)
            return reflectCopy(row, updates)
        } catch (e: SQLException) {
            // Creates the table if it doesn't already exist
            if (e.message == "[SQLITE_ERROR] SQL error or missing database (no such table: $tableName)") {
                createTable(row)
                return insert(row)
            }
            else
                throw DatabaseException("An impassable error occurred while trying to insert a row.", e, sql)
        }
    }

    /**
     * Updates a row based on the specified conditions.
     * @throws DatabaseException If the underlying database throws an error that can't be handled here.
     * @throws UnsupportedDataTypeException If the class contains a member with an unsupported data type.
     */
    fun <T: Any> update(clazz: KClass<T>, updater: KormUpdater) {
        val tableName = clazz.simpleName
        var sql = "UPDATE $tableName SET ${updater.sql}"
        if (updater.condition != null)
            sql += " WHERE ${updater.condition.sql}"

        try {
            val pstmt = conn.prepareStatement(sql)
            val columns = updater.values + (updater.condition?.values ?: emptyList())
            columns.forEachIndexed { i, col ->
                applyEncoder(col::class.createType(nullable = false), pstmt, i + 1, col)
            }
            pstmt.executeUpdate()
            pstmt.close()
        } catch (e: SQLException) {
            // Creates the table if it doesn't already exist
            if (e.message == "[SQLITE_ERROR] SQL error or missing database (no such table: $tableName)")
                return
            else
                throw DatabaseException("An impassable error occurred while trying to update the table.", e, sql)
        }
    }

    fun close() {
        conn.close()
    }

    fun <T: Any> addCoder(encoder: Encoder<T>, decoder: Decoder<T>, dataType: String) {
        @Suppress("UNCHECKED_CAST")
        coders[decoder.reflect()!!.returnType] = Coder(encoder, decoder, dataType) as Coder<Any>
    }

    private fun <T: Any> applyEncoder(type: KType, pstmt: PreparedStatement, index: Int, data: T?) {
        if (coders.contains(type))
            coders[type]!!.encoder(pstmt, index, data)
        else
            when (type) {
                Boolean::class.createType() -> pstmt.setBool(index, data as Boolean?)
                Float::class.createType() -> pstmt.setFloating(index, data as Float?)
                ForeignKey::class.createType() -> pstmt.setInteger(index, (data as ForeignKey).value)
                Int::class.createType() -> pstmt.setInteger(index, data as Int?)
                PrimaryKey::class.createType() -> pstmt.setInteger(index, (data as PrimaryKey).value)
                String::class.createType() -> pstmt.setString(index, data as String?)
                else -> throw UnsupportedDataTypeException("Invalid data type $type.")
            }
    }

    private fun applyDecoder(type: KType, rs: ResultSet, columnName: String?): Any? {
        return if (coders.contains(type))
            coders[type]!!.decoder(rs, columnName)
        else
            when (type) {
                Boolean::class.createType() -> rs.getBool(columnName)
                Float::class.createType() -> rs.getFloating(columnName)
                ForeignKey::class.createType() -> ForeignKey(null, null, rs.getInt(columnName))
                Int::class.createType() -> rs.getInteger(columnName)
                PrimaryKey::class.createType() -> PrimaryKey(rs.getInteger(columnName))
                String::class.createType() -> rs.getString(columnName)
                else -> throw UnsupportedDataTypeException("Invalid data type $type.")
            }
    }

    /**
     * Returns a copy of the object with the specified updates applied to the copy.
     * @param row The object to apply the update to.
     * @param updates A map of member property names to new value.
     * @return A copy of the object with the updates applied.
     */
    private fun <T: Any> reflectCopy(row: T, updates: Map<String, Any>): T {
        @Suppress("UNCHECKED_CAST")
        return with(row::class.memberFunctions.first { it.name == "copy" }) {
            callBy(mapOf(instanceParameter!! to row)
                    .plus(updates.mapNotNull { (property, newValue) ->
                        parameters.firstOrNull { it.name == property }
                                ?.let { it to newValue }
                    })
            )
        } as T
    }

    /**
     * Gets the name of a class from an instance of that class.
     */
    private fun <T: Any> tableName(row: T) = row::class.simpleName
    private fun <T: KClass<*>> tableName(clazz: T) = clazz.simpleName

    /**
     * Gets the name of all member properties of an object.
     */
    private fun <T: Any> columnNames(row: T) = columnNames(row::class)
    private fun <T: KClass<*>> columnNames(clazz: T): List<KProperty1<out String, Any?>> {
        val parametersNames = clazz.primaryConstructor!!.parameters.map { it.name }
        return clazz.declaredMemberProperties.filter { parametersNames.contains(it.name) } as List<KProperty1<out String, Any?>>
    }

    /**
     * Reads a property from a object based on the name of the property.
     * Can read private properties too.
     */
    private fun <T: Any> readProperty(instance: T, propertyName: String): T? {
        val clazz = instance.javaClass.kotlin
        @Suppress("UNCHECKED_CAST")
        clazz.declaredMemberProperties.first { it.name == propertyName }.let {
            it.isAccessible = true
            return it.get(instance) as T?
        }
    }
}