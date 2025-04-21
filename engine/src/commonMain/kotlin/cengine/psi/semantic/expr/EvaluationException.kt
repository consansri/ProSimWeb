package cengine.psi.semantic.expr

import cengine.psi.core.PsiElement

/**
 * Exception thrown during expression evaluation.
 *
 * @param message The error message.
 * @param element The PSI element associated with the error, if available.
 * @param cause The underlying cause, if any.
 */
class EvaluationException(
    message: String,
    val element: PsiElement? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause){
    init {
        element?.addError(message)
    }
}