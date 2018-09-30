# Korm

A super simple Kotlin ORM. It uses prepared statements for everything and is therefore not vulnerable to SQL Injection attacks, unlike [PultusORM](https://github.com/s4kibs4mi/PultusORM). It uses Reflection to generate all sql statements, which provides many compile time benefits.

## Usage

##### Gradle

```gradle
allprojects {
    repositories {
        maven("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.IgniparousTempest:korm:v0.3.0")
}
```

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
    val studentId: PrimaryKey = PrimaryKey(),
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
        fun fromString(str: String): Discipline {
            val params = str.split(",")
            return Degree(params.dropLast(1).joinToString(""), params.last().toInt())
        }
    }
}

data class Student (
    var studentId: PrimaryKey = PrimaryKey(),
    val surname: String,
    val degree: Degree
)

val encoder: Encoder<Discipline> = { ps, parameterIndex, x -> ps.setString(parameterIndex, x.toString())}
val decoder: Decoder<Discipline> = { rs, columnLabel -> Discipline.fromString(rs.getString(columnLabel))}
orm.addCoder(encoder, decoder, "TEXT")
```

## Foreign Keys

```kotlin
data class Dog(
    val dogId: PrimaryKey,
    val name: String,
    val ownerId: ForeignKey
)

val student = orm.insert(Student(/* Set parameters */))

val dog = orm.insert(Dog(
    name = "Baggins", 
    ownerId = ForeignKey(Student::studentId, student.studentId)
))
```