package cengine.lang.asm.psi

import cengine.psi.core.NodeBuilderFn
import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiTokenType

interface AsmRegisterT : PsiStatement.PsiStatementTypeDef {
    val displayName: String get() = recognizable.first()
    val recognizable: List<String>
    val numericalValue: UInt

    override val typeName: String
        get() = displayName

    override val builder: NodeBuilderFn
        get() = { markerInfo, children, range ->
            if (
                children.size == 1
                && children[0].type == PsiTokenType.IDENTIFIER
            ) {
                AsmRegister(this@AsmRegisterT, range, *children)
            } else null
        }
}