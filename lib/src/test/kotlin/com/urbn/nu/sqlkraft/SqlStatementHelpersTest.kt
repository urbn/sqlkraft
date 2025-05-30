package com.urbn.nu.sqlkraft

import kotlin.test.Test
import kotlin.test.assertEquals

class SqlStatementHelpersTest {

    @Test
    fun `Should create a WHERE statement with a single clause`() {
        val actual = where(sql("id = 'bar'"))
        val expected = "WHERE id = 'bar'"
        assertStatementsEqual(expected, actual)
    }

    @Test
    fun `Should create a WHERE statement with multiple clauses`() {
        val actual = where(sql("id = 'bar'"), sql("created_at > 0"))
        val expected = "WHERE id = 'bar' AND created_at > 0"
        assertStatementsEqual(expected, actual)
    }

    @Test
    fun `Should create an empty statement with no clauses`() {
        val actual = where(emptyList())
        val expected = ""
        assertStatementsEqual(expected, actual)
    }
}

private fun assertStatementsEqual(expected: String, actual: SqlStatement) {
    assertEquals(expected, generateTestSql(actual))
}
