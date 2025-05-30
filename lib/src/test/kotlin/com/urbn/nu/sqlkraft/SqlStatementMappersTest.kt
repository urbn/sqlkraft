package com.urbn.nu.sqlkraft

import kotlin.test.Test
import kotlin.test.assertEquals

class SqlStatementMappersTest {
    @Test
    fun `java_sql should work with SQL with no parameters`() {
        val statement = SqlStatement.of("SELECT * FROM foo")
        val (actualSql, actualParameters) = generateJavaSql(statement)
        val expected = "SELECT * FROM foo"
        assertEquals(expected, actualSql)
        assertEquals(emptyList<Any?>(), actualParameters)
    }

    @Test
    fun `java_sql should work with SQL with parameters`() {
        val statement = SqlStatement.of(
            "SELECT * FROM foo WHERE id = ",
            sqlParameter("my_id"),
            " AND timestamp > ",
            sqlParameter(100)
        )
        val (actualSql, actualParameters) = generateJavaSql(statement)
        assertEquals("SELECT * FROM foo WHERE id = ? AND timestamp > ?", actualSql)
        assertEquals(listOf(SqlParameter.Primitive("my_id"), SqlParameter.Primitive(100)), actualParameters)
    }

    @Test
    fun `java_sql should work with SQL with sub statements`() {
        val statement = SqlStatement.of(
            "SELECT * FROM foo WHERE create_at > ",
            sqlParameter(10),
            " AND id IN (",
            SqlStatement.of(
                "SELECT id from bar WHERE baz = ",
                sqlParameter(100)
            ),
            ")",
        )
        val (actualSql, actualParameters) = generateJavaSql(statement)
        assertEquals(
            "SELECT * FROM foo WHERE create_at > ? AND id IN (SELECT id from bar WHERE baz = ?)",
            actualSql
        )
        assertEquals(listOf(SqlParameter.Primitive(10), SqlParameter.Primitive(100)), actualParameters)
    }
}
