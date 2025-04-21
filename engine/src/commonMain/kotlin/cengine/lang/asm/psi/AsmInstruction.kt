package cengine.lang.asm.psi

import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement
import cengine.psi.lexer.PsiToken
import cengine.psi.lexer.PsiTokenType

/**
 * Represents an assembly instruction (mnemonic + operands).
 * E.g., "mov %eax, %ebx"
 */
class AsmInstruction(
    override val type: AsmInstructionT,
    range: IntRange,
    vararg children: PsiElement,
) : PsiStatement(type, range, *children) {

    val mnemonic = children.filterIsInstance<PsiToken>().firstOrNull { it.type == PsiTokenType.KEYWORD }
    val exprs = children.filterIsInstance<Expr>()
    val regs = children.filterIsInstance<AsmRegister>()

}