package cengine.lang.asm.psi

import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiStatement

class AsmRegister(override val type: AsmRegisterT, range: IntRange, vararg children: PsiElement) : PsiStatement(type, range, *children) {

}