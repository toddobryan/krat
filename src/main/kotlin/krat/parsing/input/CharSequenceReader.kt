package krat.parsing.input

class CharSequenceReader(
    override val source: CharSequence,
    override val offset: Int
): Reader<Char>() {
    constructor(source: CharSequence) : this(source, 0)

    override val first: Char? by lazy { if (!atEnd) source[offset] else null }
    override val rest: CharSequenceReader by lazy {
        if (!atEnd) CharSequenceReader(source, offset + 1) else this
    }

    override val pos = OffsetPosition(source, offset)
    override val atEnd = offset >= source.length

    override fun drop(n: Int) = CharSequenceReader(source, Math.min(source.length, offset + n))

    override fun toString() = "CharSequenceReader(${if (atEnd) "" else "'$first"})"

}