package com.caughtknee.korm

import java.sql.PreparedStatement
import java.sql.ResultSet

typealias Encoder<T> = (ps: PreparedStatement, parameterIndex: Int, x: T?) -> Unit
typealias Decoder<T> = (rs: ResultSet, columnLabel: String?) -> T?

class Coder<T: Any> (val encoder: Encoder<T>, val decoder: Decoder<T>, val dataType: String)