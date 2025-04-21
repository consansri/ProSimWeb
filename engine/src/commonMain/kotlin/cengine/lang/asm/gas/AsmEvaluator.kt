package cengine.lang.asm.gas

import cengine.psi.elements.PsiStatement

object AsmEvaluator {

    fun PsiStatement.Expr.Literal.String.evaluate(): String {
        return content.joinToString("") {
            when (it) {
                is PsiStatement.PsiStringElement.Basic -> it.value
                is PsiStatement.PsiStringElement.Escaped -> it.value
                is PsiStatement.PsiStringElement.Interpolated.InterpBlock -> throw Exception("Assembly doesn't support String interpolation!")
                is PsiStatement.PsiStringElement.Interpolated.InterpIdentifier -> throw Exception("Assembly doesn't support String interpolation!")
            }
        }
    }


}