package com.github.igniparoustempest.korm.types

import kotlin.reflect.KProperty1

class Row<V: Any>(m: Map<String, V?>): HashMap<String, V?>(m) {
    operator fun <T, R: Any> get(i: KProperty1<T, R?>): R? {
        return get(i.name) as R?
    }
}