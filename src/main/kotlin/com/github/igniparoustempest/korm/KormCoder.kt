package com.github.igniparoustempest.korm

import java.sql.PreparedStatement
import java.sql.ResultSet

typealias Encoder<T> = (ps: PreparedStatement, parameterIndex: Int, x: T?) -> Unit
typealias Decoder<T> = (rs: ResultSet, columnLabel: String?) -> T?

/**
 * Encapsulates an encoder/decoder for a Type.
 * @param encoder The encoder for the type.
 * @param decoder The decoder for the type.
 * @param dataType The SQLite data type, i.e. BLOB, INTEGER, NULL, REAL, TEXT.
 */
data class KormCoder<T: Any> (val encoder: Encoder<T>, val decoder: Decoder<T>, val dataType: String)