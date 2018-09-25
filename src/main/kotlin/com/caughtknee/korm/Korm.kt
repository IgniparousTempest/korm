package com.caughtknee.korm

import com.caughtknee.korm.annotations.AutoIncrement
import com.caughtknee.korm.exceptions.UnsupportedDataTypeException
import com.caughtknee.korm.types.PrimaryKey
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.*
import kotlin.reflect.jvm.reflect


class Korm(path: String? = null) {
    private val conn: Connection = DriverManager.getConnection(if (path == null) "jdbc:sqlite::memory:" else "jdbc:sqlite:$path")
    private val coders = mutableMapOf<KType, Coder<Any>>()

    /**
     * Creates a table.
     * @throws UnsupportedDataTypeException If the row class contains a member with an unsupported data type.
     */
    private fun <T: Any> createTable(row: T) {
        val tableName = tableName(row)
        val columns = columnNames(row)
        val columnNames = columns.joinToString(",") {
            var colType = it.name + " " +
                if (coders.contains(it.returnType.withNullability(false)))
                    coders[it.returnType.withNullability(false)]!!.dataType
                else
                    when (it.returnType.withNullability(false)) {
                        PrimaryKey::class.createType() -> "INTEGER PRIMARY KEY"
                        Float::class.createType() -> "REAL"
                        Int::class.createType() -> "INTEGER"
                        String::class.createType() -> "TEXT"
                        else -> throw UnsupportedDataTypeException("Invalid data type ${it.returnType}.")
                    }
            if (!it.returnType.isMarkedNullable)
                colType += " NOT NULL"
            if (it.findAnnotation<AutoIncrement>() != null)
                colType += " AUTOINCREMENT"

            colType
        }
        val sql = "CREATE TABLE IF NOT EXISTS $tableName ($columnNames)"

        try {
            println(sql)
            val stmt = conn.createStatement()
            stmt.execute(sql)
            stmt.close()
        } catch (e: SQLException) {
            throw e
        }
    }

    /**
     * Deletes rows in the table that match the specified condition.
     */
    fun <T: Any> delete(clazz: KClass<T>) {
    }

    /**
     * Drops the specified table.
     */
    fun <T: Any> drop(clazz: KClass<T>) {
        val tableName = clazz.simpleName
        val sql = "DROP TABLE IF EXISTS $tableName"
        try {
            val stmt = conn.createStatement()
            stmt.executeUpdate(sql)
            stmt.close()
        } catch (e: SQLException) {
            throw e
        }
    }

    /**
     * Gets all rows from a table that match the specified condition.
     * @throws UnsupportedDataTypeException If the T class contains a member with an unsupported data type.
     */
    fun <T: Any> find(clazz: KClass<T>): List<T> {
        val tableString = clazz.simpleName
        val sql = "SELECT * FROM $tableString"

        val results = mutableListOf<T>()

        try {
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery(sql)

            while (rs.next()) {
                val params = clazz.primaryConstructor!!.parameters.map {
                    @Suppress("IMPLICIT_CAST_TO_ANY")
                    if (coders.contains(it.type.withNullability(false)))
                        coders[it.type.withNullability(false)]!!.decoder(rs, it.name)
                    else
                        when (it.type.withNullability(false)) {
                            Float::class.createType() -> rs.getFloating(it.name)
                            Int::class.createType() -> rs.getInteger(it.name)
                            PrimaryKey::class.createType() -> PrimaryKey(rs.getInteger(it.name))
                            String::class.createType() -> rs.getString(it.name)
                            else -> throw UnsupportedDataTypeException("Invalid data type ${it.type}.")
                        }
                }.toTypedArray()
                results.add(clazz.primaryConstructor!!.call(*params))
            }

            rs.close()
            stmt.close()
        } catch (e: SQLException) {
            if (e.message == "[SQLITE_ERROR] SQL error or missing database (no such table: $tableString)")
                return emptyList()
            else
                throw e
        }

        return results
    }

    /**
     * Inserts a row into the table.
     * @throws UnsupportedDataTypeException If the row class contains a member with an unsupported data type.
     */
    fun <T: Any> insert(row: T) {
        val tableString = tableName(row)
        val columns = columnNames(row).filter { it.returnType != PrimaryKey::class.createType() }
        val columnString = columns.joinToString(",") { it.name }
        val valuesString = columns.joinToString(",") { "?" }
        val sql = "INSERT INTO $tableString($columnString) VALUES($valuesString)"

        try {
            createTable(row)
            val pstmt = conn.prepareStatement(sql)
            columns.forEachIndexed { i, col ->
                if (coders.contains(col.returnType.withNullability(false)))
                    coders[col.returnType.withNullability(false)]!!.encoder(pstmt, i + 1, readPropery(row, col.name))
                else
                    when (col.returnType.withNullability(false)) {
                        Float::class.createType() -> pstmt.setFloating(i + 1, readPropery(row, col.name))
                        Int::class.createType() -> pstmt.setInteger(i + 1, readPropery(row, col.name))
                        String::class.createType() -> pstmt.setString(i + 1, readPropery(row, col.name))
                        else -> throw UnsupportedDataTypeException("Invalid data type ${col.returnType}.")
                    }
            }
            pstmt.executeUpdate()
            pstmt.close()
        } catch (e: SQLException) {
            throw e
        }

    }

    fun close() {
        conn.close()
    }

    fun <T: Any> addCoder(encoder: Encoder<T>, decoder: Decoder<T>, dataType: String) {
        @Suppress("UNCHECKED_CAST")
        coders[decoder.reflect()!!.returnType] = Coder(encoder, decoder, dataType) as Coder<Any>
    }

    private fun <T: Any> tableName(row: T) = row::class.simpleName
    private fun <T: Any> columnNames(row: T) = row::class.declaredMemberProperties.filter { it.visibility == KVisibility.PUBLIC }
    fun <T: Any?> readPropery(instance: Any, propertyName: String): T {
        val clazz = instance.javaClass.kotlin
        @Suppress("UNCHECKED_CAST")
        return clazz.declaredMemberProperties.first { it.name == propertyName }.get(instance) as T
    }
}