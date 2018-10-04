package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.types.ForeignKey
import com.github.igniparoustempest.korm.types.PrimaryKey
import java.util.Random
import kotlin.math.abs

fun Random.nextForeignKeyFloat(): ForeignKey<Float> {
    return ForeignKey(null, null, this.nextFloat())
}

fun Random.nextForeignKeyInt(): ForeignKey<Int> {
    return ForeignKey(null, null, this.nextInt())
}

fun Random.nextForeignKeyString(): ForeignKey<String> {
    return ForeignKey(null, null, this.nextString())
}

fun Random.nextPrimaryKeyFloat(): PrimaryKey<Float> {
    return PrimaryKey(this.nextFloat())
}

fun Random.nextPrimaryKeyInt(): PrimaryKey<Int> {
    return PrimaryKey(this.nextInt())
}

fun Random.nextPrimaryKeyString(): PrimaryKey<String> {
    return PrimaryKey(this.nextString())
}

fun Random.nextString(): String {
    val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val lettersLength = letters.length
    val length = this.nextInt() % 12
    return (0 until length).joinToString("") { "" + letters[abs(this.nextInt() % lettersLength)] }
}