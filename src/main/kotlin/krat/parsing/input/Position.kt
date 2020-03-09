package krat.parsing.input

abstract class Position : Comparable<Position> {
    abstract val line: Int
    abstract val column: Int
    abstract val lineContents: String
    val displayString by lazy {
        lineContents + "\n" + lineContents.take(column - 1).map{ if (it == '\t') '\t' else ' '} + "^"
    }

    override fun compareTo(other: Position): Int =
        compareValuesBy(this, other, { it.line }, { it.column })

    override fun toString() = "$line.$column"
}