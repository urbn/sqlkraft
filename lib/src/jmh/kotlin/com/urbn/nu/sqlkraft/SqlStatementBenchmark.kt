package com.urbn.nu.sqlkraft

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
open class SampleBenchmark {

    val simpleSql = """
       SELECT * FROM foo
    """.trimIndent()
    val parameterizedSql = """
       SELECT * FROM foo WHERE id = ${sqlParameter("1")}
    """.trimIndent()
    val composedSql = """
       SELECT * FROM foo WHERE IN (${sql("SELECT id FROM bar")})
    """.trimIndent()
    val realWorldSql = """
       SELECT
            *
        FROM
            foo
        ${
        where(
            sql("bar1 = ${sqlParameter("1")}"),
            sql("bar2 = ${sqlParameter("2")}"),
            sql("bar3 = ${sqlParameter("3")}"),
            sql("bar4 = ${sqlParameter("4")}"),
            sql("bar5 = ${sqlParameter("5")}"),
            sql("bar6 = ${sqlParameter("6")}"),
        )
    }
        ORDER BY
            ${sql("created_at ASC")}
        LIMIT
            ${sqlParameter(10)}
        OFFSET
            ${sqlParameter(10)}
    """.trimIndent()

    @Benchmark
    fun parseSqlSimpleBenchmark(bh: Blackhole) {
        bh.consume(parseSql(simpleSql));
    }

    @Benchmark
    fun parseSqlParameteredBenchmark(bh: Blackhole) {
        bh.consume(parseSql(parameterizedSql));
    }

    @Benchmark
    fun parseSqlComposedBenchmark(bh: Blackhole) {
        bh.consume(parseSql(composedSql));
    }

    @Benchmark
    fun parseSqlRealWorldBenchmark(bh: Blackhole) {
        bh.consume(parseSql(realWorldSql));
    }
}
