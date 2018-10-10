package com.github.igniparoustempest.korm.types

import com.github.igniparoustempest.korm.getFloating
import com.github.igniparoustempest.korm.getInteger
import java.sql.ResultSet
import java.sql.Types

open class Table(_rows: List<Row<Any>>? = null) {
    constructor(rs: ResultSet): this(resultSetToRows(rs))
    internal val rows: List<Row<Any>> = _rows ?: emptyList()

    operator fun get(i: Int) = rows[i]

    override fun toString(): String {
        return toString(8)
    }

    fun toString(columnWidth: Int): String {
        if (rows.isEmpty())
            return ""

        val sb = StringBuilder()
        // Column Names
        sb.append('|')
        for (key in rows[0].keys) {
            sb.append(pad(key, columnWidth))
            sb.append('|')
        }
        sb.append("\n|")
        for (key in rows[0].keys) {
            sb.append("-".repeat(columnWidth))
            sb.append('|')
        }
        sb.append("\n")
        sb.append(rows.joinToString("\n") { "|" + it.entries.joinToString("|") { c -> pad(c.value, columnWidth) } + "|" })

        return sb.toString()
    }

    private fun pad(data: Any?, columnWidth: Int): String {
        val str = data.toString()
        return if (str.length > columnWidth)
            str.substring(0, columnWidth - 3) + "..."
        else
            str.padEnd(columnWidth)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Table) return false

        if (rows != other.rows) return false

        return true
    }

    override fun hashCode(): Int {
        return rows.hashCode()
    }

    companion object {
        fun resultSetToRows(rs: ResultSet): List<Row<Any>> {

            val mutableRows = mutableListOf<Map<String, Any?>>()
            val rsmd = rs.metaData
            if (rsmd != null) {
                val columnsNumber = rsmd.columnCount
                while (rs.next()) {
                    val map = mutableMapOf<String, Any?>()
                    for (i in 1..columnsNumber) {
                        val label = rsmd.getColumnName(i)
                        map[label] = when (rsmd.getColumnType(i)) {
                            Types.FLOAT, Types.REAL -> rs.getFloating(label)
                            Types.INTEGER -> rs.getInteger(label)
                            Types.VARCHAR -> rs.getString(label)
                            else -> throw Exception("The column $label had an unknown SQL type with number ${rsmd.getColumnType(i)}")
                        }
                    }
                    mutableRows.add(map as HashMap<String, Any?>)
                }
            }
            return mutableRows.map { Row(it) }
        }
    }
}