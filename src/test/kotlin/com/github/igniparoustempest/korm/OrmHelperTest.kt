package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.OrmHelper.Companion.columnNames
import com.github.igniparoustempest.korm.OrmHelper.Companion.isUnsetPrimaryKeyAuto
import com.github.igniparoustempest.korm.types.PrimaryKey
import com.github.igniparoustempest.korm.types.PrimaryKeyAuto
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OrmHelperTest {
    @Test
    fun isUnsetPrimaryKeyAutoTest() {
        data class C1(val id: PrimaryKeyAuto? = PrimaryKeyAuto())
        data class C2(val id: PrimaryKey<Int>? = PrimaryKey(3))
        data class C3(val id: PrimaryKeyAuto = PrimaryKeyAuto())
        val t1 = C1()
        val t2 = C1(PrimaryKeyAuto(2))
        val t3 = C1(null)
        val t4 = C2()
        val t5 = C3()
        println(columnNames(t1).first())
        assertTrue(isUnsetPrimaryKeyAuto(t1, columnNames(t1).first()))
        assertFalse(isUnsetPrimaryKeyAuto(t2, columnNames(t2).first()))
        assertFalse(isUnsetPrimaryKeyAuto(t3, columnNames(t3).first()))
        assertFalse(isUnsetPrimaryKeyAuto(t4, columnNames(t4).first()))
        assertTrue(isUnsetPrimaryKeyAuto(t5, columnNames(t5).first()))
    }
}