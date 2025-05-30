package com.urbn.nu.sqlkraft

fun where(vararg clauses: SqlStatement): SqlStatement {
    return where(clauses.asList())
}

fun where(clauses: List<SqlStatement>): SqlStatement {
    return SqlStatement(clauses.mapIndexed { index, sqlStatement ->
        SqlStatement.of(
            if (index == 0) "WHERE " else " AND ",
            sqlStatement
        )
    })
}
