package com.github.igniparoustempest.korm.exceptions

class DatabaseException(message: String, cause: Throwable, val sql: String): Exception("$message\nSQL: \"$sql\"", cause)