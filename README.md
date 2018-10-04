# Korm

[![](https://jitpack.io/v/IgniparousTempest/korm.svg)](https://jitpack.io/#IgniparousTempest/korm) [![Build Status](https://travis-ci.com/IgniparousTempest/korm.svg?branch=master)](https://travis-ci.com/IgniparousTempest/korm) [![codecov](https://codecov.io/gh/IgniparousTempest/korm/branch/master/graph/badge.svg)](https://codecov.io/gh/IgniparousTempest/korm)

A super simple Kotlin ORM. It uses prepared statements for everything and is therefore not vulnerable to SQL Injection attacks, unlike [PultusORM](https://github.com/s4kibs4mi/PultusORM). Another benefit it has over other ORMs is it uses Reflection to generate all sql statements, which provides many compile time benefits.

## Usage

##### Gradle Kotlin DSL

```gradle
allprojects {
    repositories {
        maven("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.IgniparousTempest:korm:v0.5.0")
}
```

See [Jitpack.io](https://jitpack.io/#IgniparousTempest/korm/v0.5.0) for including this library with different frameworks or for different versions.

## Create a new database

Databases are automatically created on initialisation.

```kotlin
// Creates a database in memory
val orm = Korm()

// Creates a database on the file system
val orm = Korm("/home/server/students.db")
```

## Insert a row

Tables are automatically created on any operation, so we can add a row without creating the table first.

The insert function returns the class with the primary keys and foreign keys matched to the database.

```kotlin
data class Student(
    val studentId: AutoPrimaryKey = AutoPrimaryKey(),
    val firstName: String,
    val surname: String,
    val maidenName: String?,
    val height: Float?,
    val age: Int
)

var student = Student(
    firstName = "Courtney",
    surname = "Pitcher",
    maidenName = null,
    height = 1.9f,
    age = 26
)

val orm = Korm()
student = orm.insert(student)  // Table is created automatically and the row is added
```

## Retrieve rows

```kotlin
val students = orm.find(Student::class)
for (st in students)
    println(st)
```

## Retrieve rows based on condition

```kotlin
// age < 12 && firstName == "Fred" || height >= 1.6f
val condition = (Student::age lt 12) and (Student::firstName eq "Fred") or (Student::height gte 1.6f)
val students = orm.find(Student::class, condition)
for (st in students)
    println(st)
```

## Update values

```kotlin
val condition = (Student::age lt 12) and (Student::firstName eq "Fred") or (Student::height gte 1.6f)
val updater = (Student::age set 21) and (Student::maidenName set null) onCondition condition  // not specifying onCondition will update the entire table 
orm.update(Student::class, updater)
```

## Delete rows

```kotlin
orm.delete(Student::class, Student::studentId eq 12)
```

## Custom data types

```kotlin
data class Degree(val name: String, val year: Int) {
    override fun toString(): String {
        return "$name,$year"
    }

    companion object {
        fun fromString(str: String): Degree {
            val params = str.split(",")
            return Degree(params.dropLast(1).joinToString(""), params.last().toInt())
        }
    }
}

data class Student (
    var studentId: AutoPrimaryKey = AutoPrimaryKey(),
    val surname: String,
    val degree: Degree
)

orm.addCoder(KormCoder(
    encoder = { ps, parameterIndex, x -> ps.setString(parameterIndex, x.toString())},
    decoder = { rs, columnLabel -> Degree.fromString(rs.getString(columnLabel))},
    dataType = "TEXT"
))
```

## Foreign Keys

```kotlin
data class Dog(
    val dogId: AutoPrimaryKey,
    val name: String,
    val ownerId: ForeignKey<Int>
)

val student = orm.insert(Student(/* Set parameters */))

val dog = orm.insert(Dog(
    name = "Baggins", 
    ownerId = ForeignKey(Student::studentId, student.studentId)
))
```

## Composite Keys

Composite keys require their type to be specified.

```kotlin
data class Person(
    val personId: PrimaryKey<Int>,
    val surname: PrimaryKey<String>,
    val firstName: String
)

data class Dog(
    val name: String,
    val ownerId: ForeignKey<Int>,
    val ownerName: ForeignKey<String>
)

val owner = orm.insert(Person(PrimaryKey(1), PrimaryKey("Pitcher"), "Courtney"))

val dog = orm.insert(Dog(
    name = "Baggins", 
    ownerId = ForeignKey(Person::personId, owner.personId),
    ownerName = ForeignKey(Person::surname, owner.surname)
))
```
