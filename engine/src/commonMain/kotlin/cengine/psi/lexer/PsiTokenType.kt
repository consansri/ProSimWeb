package cengine.psi.lexer

import cengine.psi.core.PsiElementType
import cengine.psi.style.CodeStyle

/**
 * Interface representing a token type.
 * This allows for easy extension for different languages.
 */
sealed interface PsiTokenType : PsiElementType {
    val style: CodeStyle?
        get() = null

    sealed interface LITERAL : PsiTokenType {
        override val typeName: String get() = "Literal"

        sealed interface INTEGER : LITERAL {
            override val typeName: String get() = "Integer"
            override val style: CodeStyle get() = CodeStyle.number

            data object Hex : INTEGER
            data object Dec : INTEGER
            data object Bin : INTEGER
            data object Oct : INTEGER
        }

        sealed interface FP : LITERAL {
            override val typeName: String get() = "FP"
            override val style: CodeStyle get() = CodeStyle.number

            data object FLOAT : FP {
                override val typeName: String get() = "Float"
            }

            data object DOUBLE : FP {
                override val typeName: String get() = "Double"
            }
        }

        data object CHAR : LITERAL {
            override val typeName: String = "Char"
            override val style: CodeStyle get() = CodeStyle.char
        }

        sealed interface STRING : LITERAL {
            override val typeName: String get() = "String"
            override val style: CodeStyle get() = CodeStyle.string

            data object SlStart : STRING {
                override val typeName: String
                    get() = super.typeName + "SlStart"
            }

            data object SlEnd : STRING {
                override val typeName: String
                    get() = super.typeName + "SlEnd"
            }

            sealed interface CONTENT : STRING {
                override val typeName: String
                    get() = super.typeName + "Content"

                data object Basic : CONTENT


                data object Escaped : CONTENT {
                    override val typeName: String
                        get() = super.typeName + "Escaped"
                    override val style: CodeStyle get() = CodeStyle.escape
                }
            }

            sealed interface INTERP : STRING {
                override val typeName: String get() = "Interp"
                override val style: CodeStyle get() = CodeStyle.keyWord

                data object Single : INTERP{
                    override val typeName: String = super.typeName + "Single"
                }
                data object BlockStart : INTERP{
                    override val typeName: String = super.typeName + "BlockStart"
                }
                data object BlockEnd : INTERP{
                    override val typeName: String = super.typeName + "BlockEnd"
                }
            }

            data object MlStart : STRING {
                override val typeName: String
                    get() = super.typeName + "MlStart"
            }

            data object MlEnd : STRING {
                override val typeName: String
                    get() = super.typeName + "MlEnd"
            }
        }
    }

    data object IDENTIFIER : PsiTokenType {
        override val typeName: String = "Identifier"
    }

    data object KEYWORD : PsiTokenType {
        override val typeName: String = "Keyword"
        override val style: CodeStyle get() = CodeStyle.keyWord
    }

    data object OPERATOR : PsiTokenType {
        override val typeName: String = "Operator"
    }

    data object PUNCTUATION : PsiTokenType {
        override val typeName: String get() = "Punctuation"
    }

    data object WHITESPACE : PsiTokenType {
        override val typeName: String = "Whitespace"
    }

    data object LINEBREAK : PsiTokenType {
        override val typeName: String = "Linebreak"
    }

    data object COMMENT : PsiTokenType {
        override val typeName: String = "Comment"
        override val style: CodeStyle get() = CodeStyle.comment
    }

    data object EOF : PsiTokenType {
        override val typeName: String = "EOF"
    }

    data object ERROR : PsiTokenType {
        override val typeName: String = "Error"
        override val style: CodeStyle get() = CodeStyle.error
    }


}