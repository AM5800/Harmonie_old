package am5800.harmonie.app.model.sql

import java.io.Closeable

interface Cursor : Closeable {
  fun getString(index: Int): String?
  fun moveToNext(): Boolean
}

interface SqlDatabase {
  fun query(query: String): Cursor
  fun execute(query: String, vararg args: Any)
}

inline fun <reified T> valueFromCursor(index: Int, cursor: Cursor): T {
  val string = cursor.getString(index)
  if ("" is T) {
    return string as T
  }
  if (0 is T) {
    return string?.toInt() as T
  }
  if (0.0 is T) {
    return string?.toDouble() as T
  }
  if (0L is T) {
    return string?.toLong() as T
  }

  throw Exception("Unsupported type: ${T::class.qualifiedName}")
}

data class Tuple4<out T1, out T2, out T3, out T4>(val value1: T1, val value2: T2, val value3: T3, val value4: T4)
data class Tuple5<out T1, out T2, out T3, out T4, out T5>(val value1: T1, val value2: T2, val value3: T3, val value4: T4, val value5: T5)
data class Tuple6<out T1, out T2, out T3, out T4, out T5, out T6>(val value1: T1, val value2: T2, val value3: T3, val value4: T4, val value5: T5, val value6: T6)
data class Tuple3<out T1, out T2, out T3>(val value1: T1, val value2: T2, val value3: T3)

inline fun <reified T1> SqlDatabase.query1(query: String): List<T1> {
  this.query(query).use { cursor ->
    val result = mutableListOf<T1>()
    while (cursor.moveToNext()) {
      val value1 = valueFromCursor<T1>(0, cursor)
      result.add(value1)
    }

    return result
  }
}

inline fun <reified T1, reified T2> SqlDatabase.query2(query: String): List<Pair<T1, T2>> {
  this.query(query).use { cursor ->
    val result = mutableListOf<Pair<T1, T2>>()
    while (cursor.moveToNext()) {
      val value1 = valueFromCursor<T1>(0, cursor)
      val value2 = valueFromCursor<T2>(1, cursor)
      result.add(Pair(value1, value2))
    }
    return result
  }
}

inline fun <reified T1, reified T2, reified T3, reified T4> SqlDatabase.query4(query: String): List<Tuple4<T1, T2, T3, T4>> {
  this.query(query).use { cursor ->
    val result = mutableListOf<Tuple4<T1, T2, T3, T4>>()
    while (cursor.moveToNext()) {
      val value1 = valueFromCursor<T1>(0, cursor)
      val value2 = valueFromCursor<T2>(1, cursor)
      val value3 = valueFromCursor<T3>(2, cursor)
      val value4 = valueFromCursor<T4>(3, cursor)
      result.add(Tuple4(value1, value2, value3, value4))
    }
    return result
  }
}

inline fun <reified T1, reified T2, reified T3, reified T4, reified T5> SqlDatabase.query5(query: String): List<Tuple5<T1, T2, T3, T4, T5>> {
  this.query(query).use { cursor ->
    val result = mutableListOf<Tuple5<T1, T2, T3, T4, T5>>()
    while (cursor.moveToNext()) {
      val value1 = valueFromCursor<T1>(0, cursor)
      val value2 = valueFromCursor<T2>(1, cursor)
      val value3 = valueFromCursor<T3>(2, cursor)
      val value4 = valueFromCursor<T4>(3, cursor)
      val value5 = valueFromCursor<T5>(4, cursor)
      result.add(Tuple5(value1, value2, value3, value4, value5))
    }

    return result
  }
}

inline fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6> SqlDatabase.query6(query: String): List<Tuple6<T1, T2, T3, T4, T5, T6>> {
  this.query(query).use { cursor ->
    val result = mutableListOf<Tuple6<T1, T2, T3, T4, T5, T6>>()
    while (cursor.moveToNext()) {
      val value1 = valueFromCursor<T1>(0, cursor)
      val value2 = valueFromCursor<T2>(1, cursor)
      val value3 = valueFromCursor<T3>(2, cursor)
      val value4 = valueFromCursor<T4>(3, cursor)
      val value5 = valueFromCursor<T5>(4, cursor)
      val value6 = valueFromCursor<T6>(5, cursor)
      result.add(Tuple6(value1, value2, value3, value4, value5, value6))
    }

    return result
  }
}

inline fun <reified T1, reified T2, reified T3> SqlDatabase.query3(query: String): List<Tuple3<T1, T2, T3>> {
  this.query(query).use { cursor ->
    val result = mutableListOf<Tuple3<T1, T2, T3>>()
    while (cursor.moveToNext()) {
      val value1 = valueFromCursor<T1>(0, cursor)
      val value2 = valueFromCursor<T2>(1, cursor)
      val value3 = valueFromCursor<T3>(2, cursor)
      result.add(Tuple3(value1, value2, value3))
    }

    return result
  }
}

fun <T : Any?> sqlEq(value: T): String {
  if (value == null) return "IS NULL"
  if (value is String) {
    return "= '${value.toString()}'"
  }
  return "= ${value.toString()}"
}