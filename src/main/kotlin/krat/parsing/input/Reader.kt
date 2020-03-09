package krat.parsing.input

abstract class Reader<out T> {
    open val source: CharSequence = throw IllegalStateException("Not a CharSequence Reader")
    open val offset: Int = throw IllegalStateException("Not a CharSequence Reader")

    abstract val first: T?
    abstract val rest: Reader<T>

    open fun drop(n: Int): Reader<T> {
        tailrec fun helper(count: Int, reader: Reader<T>): Reader<T> =
           if (count < 0) reader
           else helper(count - 1, reader.rest)

        return helper(n, this)
    }

    abstract val pos: Position
    abstract val atEnd: Boolean
}