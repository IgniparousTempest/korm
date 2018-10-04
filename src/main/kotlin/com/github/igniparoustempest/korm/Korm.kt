package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.OrmHelper.Companion.columnNames
import com.github.igniparoustempest.korm.OrmHelper.Companion.foreignKeyColumns
import com.github.igniparoustempest.korm.OrmHelper.Companion.foreignKeyType
import com.github.igniparoustempest.korm.OrmHelper.Companion.isPrimaryKeyAuto
import com.github.igniparoustempest.korm.OrmHelper.Companion.isUnsetPrimaryKeyAuto
import com.github.igniparoustempest.korm.OrmHelper.Companion.primaryKeyColumns
import com.github.igniparoustempest.korm.OrmHelper.Companion.primaryKeyType
import com.github.igniparoustempest.korm.OrmHelper.Companion.readProperty
import com.github.igniparoustempest.korm.OrmHelper.Companion.tableName
import com.github.igniparoustempest.korm.exceptions.DatabaseException
import com.github.igniparoustempest.korm.exceptions.UnsupportedDataTypeException
import com.github.igniparoustempest.korm.types.ForeignKey
import com.github.igniparoustempest.korm.types.PrimaryKey
import com.github.igniparoustempest.korm.types.PrimaryKeyAuto
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
import kotlin.reflect.jvm.reflect


class Korm(private val conn: Connection) {
    constructor(): this(DriverManager.getConnection("jdbc:sqlite::memory:", KormConfig().toProperties()))
    constructor(path: String): this(DriverManager.getConnection("jdbc:sqlite:$path", KormConfig().toProperties()))
    private val coders = mutableMapOf<KType, KormCoder<Any>>()

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
                        Boolean::class.createType() -> "INTEGER"
                        Float::class.createType() -> "REAL"
                        foreignKeyType(Float::class) -> "REAL"
                        foreignKeyType(Int::class) -> "INTEGER"
                        foreignKeyType(String::class) -> "TEXT"
                        Int::class.createType() -> "INTEGER"
                        PrimaryKeyAuto::class.createType() -> "INTEGER"
                        primaryKeyType(Float::class) -> "REAL"
                        primaryKeyType(Int::class) -> "INTEGER"
                        primaryKeyType(String::class) -> "TEXT"
                        String::class.createType() -> "TEXT"
                        else -> throw UnsupportedDataTypeException("Invalid data type ${it.returnType}.")
                    }
            if (!it.returnType.isMarkedNullable)
                colType += " NOT NULL"

            colType
        }
        // Primary Keys
        val primaryKeyNames = primaryKeyColumns(columns).map { it.name }
        val primaryKeyDefinition = if (primaryKeyNames.isEmpty()) "" else ", PRIMARY KEY(%s)".format(primaryKeyNames.joinToString(", "))
        // Foreign Keys
        val foreignKeys = foreignKeyColumns(columns)
        val foreignKeyNames = foreignKeys.joinToString(", ") { it.name }
        val foreignPrimaryKeyNames = foreignKeys.joinToString(", ") { (readProperty(row, it.name) as ForeignKey<*>).foreignColumnName }
        val foreignTableNames = foreignKeys.map { (readProperty(row, it.name) as ForeignKey<*>).foreignTableName  }
        val foreignTableName = if (foreignTableNames.isEmpty()) null else foreignTableNames.first()
        foreignTableNames.forEach { if (it != foreignTableName) throw DatabaseException("Composite Foreign Keys need to reference the same table.", Exception(), "") }
        val foreignKeyDefinition = if (foreignKeys.isEmpty()) "" else ", FOREIGN KEY(%s) REFERENCES $foreignTableName(%s) ON UPDATE CASCADE".format(foreignKeyNames, foreignPrimaryKeyNames)

        val sql = "CREATE TABLE IF NOT EXISTS $tableName ($columnNames$primaryKeyDefinition$foreignKeyDefinition)"

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
        val tableName = tableName(clazz)
        val sql = "DELETE FROM $tableName WHERE ${condition.sql}"

        try {
            val pstmt = conn.prepareStatement(sql)
            condition.values.forEachIndexed { i, col ->
                applyEncoder(col::class.createType(nullable = false), pstmt, i + 1, col)
            }
            pstmt.executeUpdate()
            pstmt.close()
        } catch (e: SQLException) {
            // Table doesn't exist
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
        val tableName = tableName(clazz)
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
        val tableString = tableName(clazz)
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
        val columns = columnNames(row).filter { !isUnsetPrimaryKeyAuto(row, it) }
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

            // Create copy of row with the correct auto primary key, if there are no values set
            return if (columnNames(row).sumBy { if (isUnsetPrimaryKeyAuto(row, it)) 1 else 0 } > 0) {
                generatedKeys.next()
                val key = PrimaryKeyAuto(generatedKeys.getInt("last_insert_rowid()"))
                val primaryKeyProperty = columnNames(row).first { isPrimaryKeyAuto(it) }
                val updates = mapOf(primaryKeyProperty.name to key)
                reflectCopy(row, updates)
            } else
                row
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
        val tableName = tableName(clazz)
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

    /**
     * Executes raw SQL.
     * Avoid doing this unless very necessary.
     * @param sql The sql statement to execute.
     * @return the number of rows affected by this operation.
     */
    fun rawSql(sql: String): Int {
        try {
            val stmt = conn.createStatement()
            val affected = stmt.executeUpdate(sql)
            stmt.close()
            return affected
        } catch (e: SQLException) {
            throw DatabaseException("An impassable error occurred while trying to run the raw SQL provided. Executing raw SQL is generally a bad idea.", e, sql)
        }
    }
    /**
     * Executes raw SQL.
     * Avoid doing this unless very necessary.
     * @param sql The sql statement to execute.
     * @param action The action to perform with the result set.
     * @return The ResultSet generated by the SQL.
     */
    fun rawSqlQuery(sql: String, action: (ResultSet) -> Unit = {}) {
        try {
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery(sql)
            action.invoke(rs)
            stmt.close()
        } catch (e: SQLException) {
            throw DatabaseException("An impassable error occurred while trying to run the raw SQL provided. Executing raw SQL is generally a bad idea.", e, sql)
        }
    }

    /**
     * Adds a new coder to the ORM to handle custom data types, or overwrite default behaviour.
     */
    fun <T: Any> addCoder(coder: KormCoder<T>) {
        @Suppress("UNCHECKED_CAST")
        coders[coder.decoder.reflect()!!.returnType] = coder as KormCoder<Any>
    }
    fun <T: Any> addCoder(encoder: Encoder<T>, decoder: Decoder<T>, dataType: String) {
        addCoder(KormCoder(encoder, decoder, dataType))
    }

    private fun <T: Any> applyEncoder(type: KType, pstmt: PreparedStatement, index: Int, data: T?) {
        if (coders.contains(type))
            coders[type]!!.encoder(pstmt, index, data)
        else
            @Suppress("UNCHECKED_CAST")
            when (type) {
                Boolean::class.createType() -> pstmt.setBool(index, data as Boolean?)
                Float::class.createType() -> pstmt.setFloating(index, data as Float?)
                foreignKeyType(Float::class) -> pstmt.setFloating(index, (data as ForeignKey<Float>).value)
                foreignKeyType(Int::class) -> pstmt.setInteger(index, (data as ForeignKey<Int>).value)
                foreignKeyType(String::class) -> pstmt.setString(index, (data as ForeignKey<String>).value)
                Int::class.createType() -> pstmt.setInteger(index, data as Int?)
                PrimaryKeyAuto::class.createType() -> pstmt.setInteger(index, (data as PrimaryKey<Int>).value)
                primaryKeyType(Float::class) -> pstmt.setFloating(index, (data as PrimaryKey<Float>).value)
                primaryKeyType(Int::class) -> pstmt.setInteger(index, (data as PrimaryKey<Int>).value)
                primaryKeyType(String::class) -> pstmt.setString(index, (data as PrimaryKey<String>).value)
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
                foreignKeyType(Float::class) -> ForeignKey(null, null, rs.getFloat(columnName))
                foreignKeyType(Int::class) -> ForeignKey(null, null, rs.getInt(columnName))
                foreignKeyType(String::class) -> ForeignKey(null, null, rs.getString(columnName))
                Int::class.createType() -> rs.getInteger(columnName)
                PrimaryKeyAuto::class.createType() -> PrimaryKeyAuto(rs.getInteger(columnName))
                primaryKeyType(Float::class) -> PrimaryKey(rs.getFloat(columnName))
                primaryKeyType(Int::class) -> PrimaryKey(rs.getInt(columnName))
                primaryKeyType(String::class) -> PrimaryKey(rs.getString(columnName))
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
}