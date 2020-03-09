package krat.parsing.input

class OffsetPosition(source: CharSequence, private val offset: Int) : Position() {
    private val lineIndex: Array<Int> by lazy {
        val lineStarts = ArrayList<Int>()
        lineStarts.add(0)
        source.mapIndexed { index, c ->
            if (c == '\n'
                || (c == '\r' && (index == source.length - 1 || source[index + 1] != '\n'))) {
                lineStarts.add(index + 1)
            }
        }
        lineStarts.add(source.length)
        lineStarts.toTypedArray()
    }

    override val line by lazy { Math.abs(lineIndex.binarySearch(offset) + 1) }

    override val column by lazy { offset - lineIndex[line - 1] + 1 }

    override val lineContents by lazy {
        val start = lineIndex[line - 1]
        val end =lineIndex[line]
        val endIndex = if (start < end - 1 && source[end - 2] == '\r' && source[end - 1] == '\n') {
            end - 2
        } else if (start < end && (source[end - 1] == '\r' || source[end - 1] == '\n')) {
            end -1
        } else {
            end
        }
        source.subSequence(start, endIndex).toString()
    }

    override fun compareTo(other: Position) = when (other) {
        is OffsetPosition -> offset.compareTo(other.offset)
        else -> super.compareTo(other)
    }
}