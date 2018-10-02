package com.github.igniparoustempest.korm.types

class PrimaryKeyAuto(key: Int, val isSet: Boolean): PrimaryKey<Int>(key) {
    internal constructor(key: Int? = null): this(key ?: -1, key != null)

    /**
     * Gets the value if it has been set, otherwise returns a null.
     */
    val safeValue: Int?
        get() = if (isSet) value else null
}