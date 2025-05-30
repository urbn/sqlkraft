package com.urbn.nu.sqlkraft

import java.lang.IllegalArgumentException

/**
 * Convert the given SqlStatement to a pair of SQL string and parameters.
 * Uses the "?" parameter token that java.sql expects. The SQL and parameters can be used to construct
 * a java.sql.PreparedStatement.
 * <pre>
 * val (sql, parameters) = generateJavaSql(sql("SELECT * FROM foo WHERE id = ${SqlParameter("my_id")}"))
 * val resultSet = connection.prepareStatement(sql).use { preparedStatement ->
 *     parameters.forEachIndexed { index, parameter -> preparedStatement.setObject(index + 1, parameter) }
 *     preparedStatement.executeQuery()
 * }
 * </pre>
 */
fun generateJavaSql(sqlStatement: SqlStatement): Pair<String, List<SqlParameter>> {
    data class Accumulator(
        val stringBuilder: StringBuilder = StringBuilder(),
        val parameters: MutableList<SqlParameter> = mutableListOf(),
    )

    val result = sqlStatement.tokens.fold(Accumulator()) { acc, token ->
        when (token) {
            is String -> acc.stringBuilder.append(token)
            is SqlParameter -> {
                acc.stringBuilder.append("?")
                acc.parameters.add(token)
            }
            is SqlStatement -> {
                val (nestedSql, nestedParams) = generateJavaSql(token)
                acc.stringBuilder.append(nestedSql)
                acc.parameters.addAll(nestedParams)
            }
            else -> throw IllegalArgumentException("Unsupported token: $token")
        }
        acc
    }
    return result.stringBuilder.toString() to result.parameters
}

/**
 * Convert the given SqlStatement to a Sql string for testing queries
 * Uses the `class` and `value` for parameters formatted as "class::value".
 * <pre>
 * val sql = generateTestSql(sql("SELECT * FROM foo WHERE id = ${SqlParameter("my_id")}"))
 * </pre>
 */
fun generateTestSql(sqlStatement: SqlStatement): String {
    val result = sqlStatement.tokens.fold(java.lang.StringBuilder()) { acc, token ->
        when (token) {
            is String -> acc.append(token)
            is SqlParameter.Primitive<*> -> acc.append("${token.clazz.name}::${token.value}")
            is SqlParameter.Array<*> -> acc.append("Array<${token.baseClazz.name}>::${token.value}")
            is SqlStatement -> acc.append(generateTestSql(token))
            else -> throw IllegalArgumentException("Unsupported token: $token")
        }
        acc
    }
    return result.toString()
}
