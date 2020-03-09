package krat.parsing.combinator

import krat.parsing.input.Reader

sealed class ParseResult<out T, Elem> {
    abstract fun <U> map(f: (T) -> U): ParseResult<U, Elem>

    abstract fun <U> flatMapWithNext(
        f: (@UnsafeVariance T) -> (Reader<Elem>) -> ParseResult<U, Elem>
    ): ParseResult<U, Elem>

    abstract fun filterWithError(
        p: (@UnsafeVariance T) -> Boolean,
        error: (@UnsafeVariance T) -> String,
        position: Reader<Elem>
    ): ParseResult<T, Elem>

    abstract val successful: Boolean

    val isEmpty by lazy { !successful }

    abstract val next: Reader<Elem>

    abstract fun get(): T
}

fun <D, Elem, T : D> ParseResult<T, Elem>.getOrElse(default: () -> D): D =
    if (isEmpty) default() else get()

fun <U, Elem, T : U> ParseResult<T, Elem>.append(
    a: () -> ParseResult<U, Elem>
): ParseResult<U, Elem> = when (this) {
    is Success -> this
    is Failure -> {
        when (val alt = a()) {
            is Success -> alt
            is NoSuccess -> if (alt.next.pos < next.pos) this else alt
        }
    }
    is Error -> this
}

data class Success<out T, Elem>(
    val result: T,
    override val next: Reader<Elem>
) : ParseResult<T, Elem>() {
    override fun <U> map(f: (T) -> U) = Success(f(result), next)

    override fun <U> flatMapWithNext(
        f: (@UnsafeVariance T) -> (Reader<Elem>) -> ParseResult<U, Elem>
    ) = f(result)(next)

    override fun filterWithError(
        p: (T) -> Boolean,
        error: (T) -> String,
        position: Reader<Elem>
    ): ParseResult<T, Elem> = if (p(result)) this else Failure(error(result), position)

    override fun get() = result

    override val successful = true

    override fun toString() = "[${next.pos}] parsed: $result"
}

sealed class NoSuccess<Elem>(val msg: String, override val next: Reader<Elem>) :
    ParseResult<Nothing, Elem>() {
    override val successful = false

    override fun <U> map(f: (Nothing) -> U) = this

    override fun <U> flatMapWithNext(f: (Nothing) -> (Reader<Elem>) -> ParseResult<U, Elem>) = this

    override fun filterWithError(
        p: (Nothing) -> Boolean,
        error: (Nothing) -> String,
        position: Reader<Elem>
    ): ParseResult<Nothing, Elem> = this

    override fun get() = throw IllegalStateException("No result when parsing fails")
}

class Failure<Elem>(msg: String, next: Reader<Elem>) : NoSuccess<Elem>(msg, next) {
    override fun toString() = "[${next.pos}] failure: $msg\n\n${next.pos.displayString}"
}

class Error<Elem>(msg: String, next: Reader<Elem>) : NoSuccess<Elem>(msg, next) {
    override fun toString() = "[${next.pos}] error: $msg\n\n${next.pos.displayString}"
}