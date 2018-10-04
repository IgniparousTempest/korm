package com.github.igniparoustempest.korm

import com.github.igniparoustempest.korm.conditions.eq
import com.github.igniparoustempest.korm.testingtables.Dog
import com.github.igniparoustempest.korm.updates.and
import com.github.igniparoustempest.korm.updates.onCondition
import com.github.igniparoustempest.korm.updates.set
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UpdatesTest {
    @Test
    fun testAnd() {
        // (Table::id eq 3) is preserved
        val left = Dog::breed set 2 onCondition (Dog::dogId eq 3) and (Dog::name set 21)
        // (Table::id eq 3) is preserved
        val right = Dog::breed set 2 and (Dog::name set 21) onCondition (Dog::dogId eq 3)
        // (Table::id eq 3) is preserved
        val leftAndRight = Dog::breed set 2 onCondition (Dog::dogId eq 5) and (Dog::name set 21) onCondition (Dog::dogId eq 3)

        for(table in listOf(left, right, leftAndRight)) {
            assertEquals("breed = ?, name = ?", table.sql)
            assertEquals(listOf(2, 21), table.values)
            assertEquals("Dog.dogId = ?", table.condition?.sql)
            assertEquals(listOf(3), table.condition?.values)
        }
    }
}