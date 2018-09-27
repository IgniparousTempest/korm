package com.github.igniparoustempest.korm

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * The same as ResultSet.getBoolean(), but allows for null.
 */
fun ResultSet.getBool(columnLabel: String?): Boolean? {
    val nValue = this.getBoolean(columnLabel)
    return if (this.wasNull()) null else nValue
}

/**
 * The same as ResultSet.getFloat(), but allows for null.
 */
fun ResultSet.getFloating(columnLabel: String?): Float? {
    val nValue = this.getFloat(columnLabel)
    return if (this.wasNull()) null else nValue
}

/**
 * The same as ResultSet.getInt(), but allows for null.
 */
fun ResultSet.getInteger(columnLabel: String?): Int? {
    val nValue = this.getInt(columnLabel)
    return if (this.wasNull()) null else nValue
}

/**
 * The same as PreparedStatement.setBoolean(), but allows for null.
 */
fun PreparedStatement.setBool(parameterIndex: Int, x: Boolean?) {
    if (x == null)
        this.setNull(parameterIndex, Types.INTEGER)
    else
        this.setBoolean(parameterIndex, x)
}

/**
 * The same as PreparedStatement.setFloat(), but allows for null.
 */
fun PreparedStatement.setFloating(parameterIndex: Int, x: Float?) {
    if (x == null)
        this.setNull(parameterIndex, Types.INTEGER)
    else
        this.setFloat(parameterIndex, x)
}

/**
 * The same as PreparedStatement.setInt(), but allows for null.
 */
fun PreparedStatement.setInteger(parameterIndex: Int, x: Int?) {
    if (x == null)
        this.setNull(parameterIndex, Types.INTEGER)
    else
        this.setInt(parameterIndex, x)
}