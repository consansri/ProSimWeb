package cengine.psi.parser.recursivedecent

import cengine.psi.core.PsiElementTypeDef
import cengine.psi.lexer.PsiTokenType
import cengine.psi.parser.PsiBuilder

interface RecursiveDescentSupport {

    /**
     * Parses a sequence of elements separated by a delimiter, enclosed by start/end tokens.
     *
     * @param separatorToken Nullable if no separator (e.g., just whitespace)
     * @param elementParser Function to parse one element, returns null on failure/end
     * @param elementType Type for the whole list marker
     * @param recoverUntil Tokens to skip to on element parse error
     */
    fun <T> PsiBuilder.parseDelimitedList(
        startToken: PsiTokenType,
        endToken: PsiTokenType,
        separatorToken: PsiTokenType?,
        elementParser: () -> T?,
        elementType: PsiElementTypeDef,
        allowEmpty: Boolean = true,
        recoverUntil: Set<PsiTokenType>? = null,

        ): PsiBuilder.Marker? {
        val listMarker = mark()
        if (!expect(startToken)) {
            listMarker.drop()
            return null
        }

        var first = true
        while (!isAtEnd() && !currentIs(endToken)) {
            if (!first && separatorToken != null) {
                if (!expect(separatorToken, "Expected separator '$separatorToken' or end token '$endToken'")) {
                    // Error recovery?
                    if (recoverUntil != null) {
                        error("Syntax error in list, attempting recovery.")
                        skipUntil(*recoverUntil.toTypedArray(), endToken) // Skip until recovery or end
                        if (currentIs(separatorToken)) advance()
                        continue
                    } else {
                        listMarker.drop()
                        return null
                    }
                }
            }
            first = false

            val elementResult = elementParser()
            if (elementResult == null) {
                // Element parser failed or decided to stop (e.g. lookahead check failed)
                // Check if we are at the end token, otherwise it's likely an error
                if (!currentIs(endToken)) {
                    error("Expected list element or end token '$endToken'")
                    // Recovery?
                    if (recoverUntil != null) {
                        skipUntil(*recoverUntil.toTypedArray(), endToken, separatorToken)
                        if (separatorToken != null && currentIs(separatorToken)) advance()
                        continue
                    } else {
                        listMarker.drop()
                        return null
                    }
                } else {
                    break // Valid end after failed element parse (e.g. trailing comma handled by elementParser returning null)
                }
            }
        }

        if (!expect(endToken, "Expected end token '$endToken'")) {
            // Error recovery? List might still be partially valid.
            // Decide whether to drop or doneWithError based on language rules.
            // For now, let's complete it even with error.
            listMarker.done(elementType) // Or a specific ErrorList type
            return listMarker // Return potentially partial list
        }

        if(!allowEmpty && first) { // 'first' is still true means no elements parsed
            error("List cannot be empty.")
            // Decide whether to drop or complete based on language rules
        }

        listMarker.done(elementType)
        return listMarker
    }

    // Helper for basic error recovery: advance until one of the target tokens is found
    fun PsiBuilder.skipUntil(vararg recoveryTokens: PsiTokenType?) {
        val recoverySet = recoveryTokens.filterNotNull().toSet()
        if (recoverySet.isEmpty()) return // Nothing to recover to

        val errorMarker = mark()
        while (!isAtEnd()) {
            if (recoverySet.contains(getTokenType())) {
                errorMarker.drop() // Drop the error marker, recovery point found
                return
            }
            advance()
        }
        errorMarker.drop() // Reached EOF without finding recovery token
    }

}