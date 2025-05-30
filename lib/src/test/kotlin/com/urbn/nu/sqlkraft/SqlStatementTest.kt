package com.urbn.nu.sqlkraft

import kotlin.test.Test
import kotlin.test.assertEquals
import java.math.BigDecimal
import java.time.ZonedDateTime

class SqlStatementTest {
    @Test
    fun `Should parse SQL with parameters`() {
        val actual = sql(
            """
                SELECT * FROM foo
                    WHERE id = ${sqlParameter("my_id")}
                    AND timestamp > ${sqlParameter(100)}
                    AND array_foo IN UNNEST(${sqlParameter(listOf("1", "2", "3"))})
                    AND bool_foo = ${sqlParameter(true)}
                    AND float_foo = ${sqlParameter(1.0)}
                    AND numeric_foo = ${sqlParameter(BigDecimal("1.0"))}
                    AND created_at = ${sqlParameter(ZonedDateTime.parse("2021-04-22T00:00:00.00Z"))}
            """.trimIndent()
        )
        val expectedSql = """
            SELECT * FROM foo
                WHERE id = java.lang.String::my_id
                AND timestamp > java.lang.Integer::100
                AND array_foo IN UNNEST(Array<java.lang.String>::[1, 2, 3])
                AND bool_foo = java.lang.Boolean::true
                AND float_foo = java.lang.Double::1.0
                AND numeric_foo = java.math.BigDecimal::1.0
                AND created_at = java.time.ZonedDateTime::2021-04-22T00:00Z
        """.trimIndent()
        assertStatementsEqual(expectedSql, actual)
    }

    @Test
    fun `Should parse SQL with nested statements`() {
        val subQuery = sql(
            """
                SELECT id from bar WHERE baz = ${sqlParameter(100)}
            """.trimIndent()
        )
        val actual = sql(
            """
                SELECT * FROM foo
                    WHERE create_at > ${sqlParameter(10)}
                    AND id IN ($subQuery)
            """.trimIndent()
        )
        val expectedSql = """
            SELECT * FROM foo
                WHERE create_at > java.lang.Integer::10
                AND id IN (SELECT id from bar WHERE baz = java.lang.Integer::100)
        """.trimIndent()
        assertStatementsEqual(expectedSql, actual)
    }
}

private fun assertStatementsEqual(expected: String, actual: SqlStatement) {
    assertEquals(expected, generateTestSql(actual))
}
