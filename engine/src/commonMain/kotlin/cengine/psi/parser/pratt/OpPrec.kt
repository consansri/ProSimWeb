package cengine.psi.parser.pratt

enum class OpPrec {
    LOWEST,                 // PLACEHOLDER
    ASSIGNMENT,             // =, +=, -=, etc.
    TERNARY_ARROW_ELVIS,    // ?: (ternary), =>, -> (non-member), ?: (elvis)
    LOGICAL_OR,             // ||
    LOGICAL_AND,            // &&
    BITWISE_OR,             // |
    BITWISE_XOR,            // ^
    BITWISE_AND,            // &
    EQUALITY,               // ==, !=
    COMPARISON,             // <, >, <=, >= (Maybe Range '..' fits here too?)
    COMPARISON_RANGE,       // <, >, <=, >=, ..
    BITWISE_SHIFT,          // <<, >>, >>>
    ADDITIVE,               // +, -
    MULTIPLICATIVE,         // *, /, %
    EXPONENTIATION,         // ** (if added)
    PREFIX_UNARY,           // +, -, !, ~, ++, -- (prefix), ... (spread)
    POSTFIX                 // ++, -- (postfix), (), [], ., ->, ?., ::
    ;

}