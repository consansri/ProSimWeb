package cengine.lang.asm.psi

import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement
import cengine.psi.feature.Highlightable
import cengine.psi.style.CodeStyle

class AsmRegister(override val type: AsmRegisterT, range: IntRange, vararg children: PsiElement) : PsiStatement(type, range, *children), Highlightable {
    override val style: CodeStyle get() = CodeStyle.MAGENTA

}