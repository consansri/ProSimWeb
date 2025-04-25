package cengine.psi.lexer

import cengine.editor.annotation.Annotation
import cengine.psi.core.PsiElement
import cengine.psi.feature.Formatable
import cengine.psi.visitor.PsiElementVisitor

/**
 * Interface representing a token in the source code.
 *
 * @param type The [PsiTokenType].
 * @param value The value of the token.
 * @param range The range of the token.
 */
class PsiToken(val value: String, override val type: PsiTokenType, override var range: IntRange) : PsiElement(type), Formatable {

    override val additionalInfo: String
        get() = ""

    override val annotations: MutableList<Annotation> = if (type == PsiTokenType.ERROR) {
        mutableListOf(Annotation.error(this, value))
    } else {
        mutableListOf()
    }

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitElement(this)
    }

    override fun format(): String = value

    override fun print(prefix: String): String = "$prefix{${type}:${if(value == "\n") "\\n" else value}}"

    override fun equals(other: Any?): Boolean {
        if (other !is PsiToken) return false

        if (other.type != type) return false
        if (other.value != value) return false
        if (other.range != range) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + range.hashCode()
        return result
    }

    override fun toString(): String {
        return "[$type:$value:$range]"
    }

}
