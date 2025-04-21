package cengine.psi.parser.pratt

data class Operator(
    val string: String,
    val type: OpType,
)