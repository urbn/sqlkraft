package com.urbn.nu.sqlkraft

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Base64

/**
 * Parse a sql string into a SqlStatement. See parseSql for details.
 */
fun sql(sqlString: String): SqlStatement = SqlStatement(parseSql(sqlString))

/**
 * Create a SqlParameter with the correct type. If a primitive type is passed then this returns a SqlParameter.Primitive.
 * If a Collection is passed, then this returns a SqlParameter.Array.
 */
inline fun <reified T : Any> sqlParameter(parameter: T?): SqlParameter = SqlParameter.Primitive(parameter)
inline fun <reified T : Any> sqlParameter(parameter: Collection<T>?): SqlParameter = SqlParameter.Array(parameter)

/**
 * Parse a sql string into a list of "tokens". Valid tokens are raw SQL strings, SqlParameters, and SqlStatements.
 * Uses handlebars notation (i.e. {{my_variable}}) to parse out SqlParameters and SqlStatements.
 * This originally used a regex and the `String.split` method, but that was too slow (~100ms). See JMH benchmarks.
 */
fun parseSql(sqlString: String): List<Any> {
    val tokens = mutableListOf<Any>()
    var beginSubstringIndex = 0
    var endSubstringIndex = 0
    while (endSubstringIndex < sqlString.length) {
        if (sqlString[endSubstringIndex] == OPEN_CURLY && sqlString[endSubstringIndex + 1] == OPEN_CURLY) {
            if (endSubstringIndex > beginSubstringIndex) { // We have built up a regular string and need to add the token
                tokens.add(sqlString.substring(beginSubstringIndex, endSubstringIndex))
                // Reset beginning equal to end to start a new substring
                beginSubstringIndex = endSubstringIndex
            }
            beginSubstringIndex += 2 // Skip opening curly braces
            // Reset end equal to beginning and iterate until we reach a double closing curly bracket
            endSubstringIndex = beginSubstringIndex
            while (sqlString[endSubstringIndex] != CLOSE_CURLY || sqlString[endSubstringIndex + 1] != CLOSE_CURLY) {
                endSubstringIndex++
            }
            // Read substring and convert from base 64 to an object. This will fail if the object is not valid.
            val string = sqlString.substring(beginSubstringIndex, endSubstringIndex)
            tokens.add(base64ToObject(string) as Any)
            endSubstringIndex += 2 // Skip closing curly braces
            beginSubstringIndex = endSubstringIndex // Reset beginning equal to end to start a new substring
        } else {
            endSubstringIndex++ // Move to next character
        }
    }
    if (endSubstringIndex > beginSubstringIndex) { // We have built up a regular string and need to add the token
        tokens.add(sqlString.substring(beginSubstringIndex, endSubstringIndex))
    }
    return tokens
}

private const val OPEN_CURLY = '{'
private const val CLOSE_CURLY = '}'

/**
 * Holds a SQL statement. Valid tokens are raw SQL strings, SqlParameters, and SqlStatements.
 */
data class SqlStatement(val tokens: List<*>) : Serializable {

    companion object {

        /**
         * Convenience constructor using vararg
         */
        fun of(vararg tokens: Any): SqlStatement {
            return SqlStatement(tokens.toList())
        }
    }

    /**
     * Used by Kotlin string interpolation when writing this object to a string.
     * Allows the user to use Kotlin string interpolation without losing any information.
     */
    override fun toString(): String {
        return sqlTokenToString(this)
    }
}

/**
 * Union type for all of the possible sql parameters
 */
sealed interface SqlParameter {

    /**
     * A "primitive" sql type. Includes types such as Int, Long, Float, Double, BigDecimal, Date, etc.
     */
    data class Primitive<T : Any>(val value: Any?, val clazz: Class<T>) : SqlParameter, Serializable {

        companion object {

            inline operator fun <reified T : Any> invoke(value: T?) = Primitive(value, T::class.java)
        }

        /**
         * Used by Kotlin string interpolation when writing this object to a string.
         * Allows the user to use Kotlin string interpolation without losing any information.
         */
        override fun toString(): String {
            return sqlTokenToString(this)
        }
    }

    /**
     * A collection of "primitive" sql types.
     */
    data class Array<T : Any>(val value: Collection<T>?, val baseClazz: Class<T>) : SqlParameter, Serializable {

        companion object {

            inline operator fun <reified T : Any> invoke(value: Collection<T>?) = Array(value, T::class.java)
        }

        /**
         * Used by Kotlin string interpolation when writing this object to a string.
         * Allows the user to use Kotlin string interpolation without losing any information.
         */
        override fun toString(): String {
            return sqlTokenToString(this)
        }
    }
}

private fun sqlTokenToString(token: Serializable): String {
    return "{{${objectToBase64(token)}}}"
}

private fun objectToBase64(obj: Serializable): String {
    return ByteArrayOutputStream().use { byteArrayOutputStream ->
        ObjectOutputStream(byteArrayOutputStream).use { objectOutputStream ->
            objectOutputStream.writeObject(obj)
        }
        Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray())
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> base64ToObject(base64String: String): T {
    val data = Base64.getDecoder().decode(base64String)
    return ByteArrayInputStream(data).use { byteArrayInputStream ->
        ObjectInputStream(byteArrayInputStream).use { objectInputStream ->
            objectInputStream.readObject() as T
        }
    }
}
