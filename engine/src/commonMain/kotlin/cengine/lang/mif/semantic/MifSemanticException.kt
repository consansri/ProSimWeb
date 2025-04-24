package cengine.lang.mif.semantic

import cengine.psi.core.PsiElement

/**
 * Custom Exception for Semantic Errors during MifData creation from PSI.
 */
class MifSemanticException(
    message: String,
    val element: PsiElement? = null, // Optional PSI range for error highlighting
    cause: Throwable? = null,
) : Exception(message, cause) {
    init {
        element?.addError(message)
    }
}