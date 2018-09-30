package com.github.igniparoustempest.korm

import org.sqlite.SQLiteConfig

/**
 * A file defining the standard config for a the Kotlin ORM.
 */
class KormConfig: SQLiteConfig() {
    init {
        this.enforceForeignKeys(true)
    }
}