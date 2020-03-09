package krat.parsing.combinator

import krat.parsing.input.Reader

abstract class Parser<out T, Elem> : (Reader<Elem>) -> ParseResult<T, Elem> {

    fun <U> flatMap(f: (T) -> Parser<U, Elem>): Parser<U, Elem> =
        Parser { this(it).flatMapWithNext(f) }

    fun <U> map(f: (T) -> U): Parser<U, Elem> = Parser { this(it).map(f) }

    fun filter(p: (T) -> Boolean): Parser<T, Elem> = withFilter(p)

    fun withFilter(p: (T) -> Boolean): Parser<T, Elem> =
        Parser { input ->
            this(input).filterWithError(p, { "Input doesn't match filter: $it" }, input)
        }

}

fun <T, Elem> Parser(f: (Reader<Elem>) -> ParseResult<T, Elem>) = object : Parser<T, Elem>() {
    override fun invoke(input: Reader<Elem>) = f(input)
}