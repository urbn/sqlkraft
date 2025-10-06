# SQLKraft
Composable, parameterized SQL statements

## SQL Composition
A `sql` function that will automatically create parameterized queries from a kotlin string template

### Usage

#### Without Parameters
The following are equivalent and both may be composed with other `SqlStatement`s in the same way
##### String
```kotlin
val sql = "SELECT * FROM foo"
```
##### SqlStatement
```kotlin
val sqlStatement = sql("SELECT * FROM foo")
```

#### With Parameters
```kotlin
val sqlStatement = sql("SELECT * FROM foo WHERE id = ${sqlParameter("myFoo")}")
```

#### With Nested Statements
```kotlin
val sqlStatement = sql("SELECT * FROM foo WHERE bar_id IN (${createIdSelect()})")

fun createIdSelect() = sql("SELECT id from bar WHERE baz > 10")
```

#### WHERE clause
A programatic way to add multiple `WHERE` clauses without having to worry about `WHERE` vs `AND`
```kotlin
val sqlStatement = sql("SELECT * FROM foo ${where("bar > 10", sql("baz = ${SqlParameter("myBaz")}"))}")
```

## SQL Statement Mappers
A set of functions for converting a SqlStatement into the corresponding sql and parameters for different libraries/drivers

### Java
```kotlin
    val (sql, parameters) = generateJavaSql(sqlStatement)
```

### Test
```kotlin
    val sqlWithParameters = generateTestSql(sqlStatement)
```

## Publishing

### Snapshots
You will need:
* A maven central account (See https://central.sonatype.org/register/central-portal/)
* A verified groupId of "com.urbn.nu" (See https://central.sonatype.org/register/namespace/)
* A GPG Key for signing artifacts (See https://central.sonatype.org/publish/requirements/gpg/)
* A portal token (See https://central.sonatype.org/publish/generate-portal-token/)

#### Steps
1. Ensure `version` property in lib/build.gradle ends with "-SNAPSHOT"
1. Setup credentials in `$HOME/.gradle/gradle.properties` (See below)
1. Run `./gradlew :lib:publish`

#### Credentials
```
# GPG Signing
signing.keyId=12345678 # Last 8 digits of GPG key id
signing.password=<insert gpg key passphrase>
signing.secretKeyRingFile=$HOME/.gnupg/secring.gpg

# Maven Central Auth
mavenCentralUsername=<insert portal token username>
mavenCentralPassword=<insert portal token password>
```

### Release
These are the steps to do an official release to maven central. Please test with SNAPSHOT releases before running these steps

#### Steps
1. Ensure `version` property in lib/build.gradle is properly set to next version
1. Run `./gradlew :lib:publish`
1. Run `cd lib/build/repos/releases/`
1. Run `tar -czvf gradle-publish-sqlkraft-<insert_version_number>.tar.gz com`
1. Upload tar file using publish portal (See https://central.sonatype.org/publish/publish-portal-upload/)
